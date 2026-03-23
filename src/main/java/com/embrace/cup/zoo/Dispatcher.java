package com.embrace.cup.zoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Dispatcher extends HttpServlet {

    private static final ConcurrentMap<String, Handler> HANDLER_CACHE = new ConcurrentHashMap<>();
    private static final String LOGTAG = "Dispatcher";
    private static final ObjectMapper JSONMAPPER = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        Log.setRequestId();
        Context ctx = new Context();
        ContextHolder.set(ctx);
        try {
            ctx.setHttpRequest(req);
            ctx.setHttpResponse(resp);
            ctx.setServletContext(getServletContext());
            ctx.setWebAppRoot(ctx.getServletContext().getRealPath("/"));
                
            setPathToContext(ctx);

            if (ctx.handlerPackageError()) { fallback(ctx); return; }

            setHandler(ctx);

            if (ctx.handlerError()) { send404(ctx); return; }

            if (ctx.httpMethodError()) { send405(ctx); return; }

            ctx.setParameters(buildParamMap(req));

            setSessionToContext(ctx);

            if (ctx.authError()) { send401(ctx); return; }
            if (ctx.permissionError()) { send403(ctx); return; }
            
            Log.info(LOGTAG, "before " + ctx.getClassFullName());
            ctx.handle();
            Log.info(LOGTAG, "after  " + ctx.getClassFullName());
            
            if (ctx.resultError()) { 
                throw new Exception(ctx.getClassFullName() + " handler return null");
            }
            setResponseCookie(ctx);

            ctx.render();

        } catch (HandlerJsonError ej) {
            sendHandlerJsonError(ctx, ej);
        } catch (HandlerTextError et) {
            sendHandlerTextError(ctx, et);
        } catch (Exception e) {
            send500(ctx);
            e.printStackTrace();
        } finally {
            Log.clearRequestId();
            ContextHolder.clear();
        }
    }


    private void setPathToContext(Context ctx) {
        HttpServletRequest req = ctx.getHttpRequest();
        String uri = req.getRequestURI();
        Log.info(LOGTAG, "uri:" + uri);

        ctx.setUri(uri);
        String uriStrip = uri.replaceAll("^/*|/*$", "");
        //String[] parts = uri.substring(1).split("/");
        String[] parts = uriStrip.split("/");
        if (parts.length != 2)  return;

        String packageName = parts[0];
        String className = parts[1];
        ctx.setPackageName(packageName);
        ctx.setClassName(className);
        
        String classFullName = ConfigHolder.APP_PACKAGE 
                            + "." + packageName
                            + "." + className;
        ctx.setClassFullName(classFullName);
        Log.info(LOGTAG, classFullName);

    }
       
    private void setHandler(Context ctx) {
        String classFullName = ctx.getClassFullName();
        if (classFullName == null || classFullName.isBlank()) return;

        Handler h = HANDLER_CACHE.get(classFullName);
        if (h == null) {
            try {
                Class<?> clazz = Class.forName(classFullName);
                Object obj = clazz.getDeclaredConstructor().newInstance();
                if (obj instanceof Handler handler) {
                    h = handler;
                    Handler existing = HANDLER_CACHE.putIfAbsent(classFullName, h);
                    if (existing != null) h = existing;
                } else {
                    Log.error(LOGTAG, classFullName + "Class is not Handler ");
                }
            } catch (Exception e) {
                Log.error(LOGTAG, classFullName + " handler create error");
                e.printStackTrace();
            }
        }
        ctx.setHandler(h); 
    }

    private void setSessionToContext(Context ctx) {
        HttpServletRequest req = ctx.getHttpRequest();
        AuthInfo authInfo = new AuthInfo();
        authInfo.setAuthenticated(false);
        ctx.setAuthInfo(authInfo);

        String sessionId = UtilWeb.getCookie(req, ConfigHolder.SESSION_COOKIE_NAME);
        if (sessionId != null) {
            ctx.setHasCookie(true);
        } else {
            sessionId = UtilWeb.getAuthrizationHeader(req);
        }
        
        if (sessionId == null) return;
        
        SessionManager manager = ConfigHolder.SESSION_MANAGER;
        Map<String, Object> sessData = manager.getSessionData(sessionId);
        if (sessData == null) return;

        authInfo.fromMap(sessData);
        
    }

    private void setResponseCookie(Context ctx) {
        HttpServletResponse resp = ctx.getHttpResponse();
        String sessionId = ctx.getSessionId();
        if (sessionId == null) return;
        SessionManager manager = ConfigHolder.SESSION_MANAGER;
        Map<String, Object> sessData = manager.getSessionData(sessionId);
        if (sessData == null) return;
        
        if (ctx.getHasCookie()){
            int sessionAge = ConfigHolder.SESSION_AGE;
            if (ctx.getLogout()) sessionAge = 0;

            sessionId = manager.saveSessionData(
                sessData, sessionAge, sessionId
            );
            UtilWeb.setCookie(
                resp, ConfigHolder.SESSION_COOKIE_NAME, sessionId, sessionAge
            );
        }
    }

    private void send401(Context ctx) {
        
        ctx.setReturnBy("send401");
        HttpServletResponse resp = ctx.getHttpResponse();
        resp.setStatus(401);
        resp.setContentType("text/html;charset=UTF-8");
        // resp.setContentType("application/json;charset=UTF-8");

        try {
            PrintWriter out = resp.getWriter();
            out.write("<!DOCTYPE html><html><head><title>Error</title></head><body>");
            out.write("<h1>HTTP Status " + resp.getStatus() + "</h1>");
            out.write("<p>Authorization Error</p>");
            out.write("</body></html>");
            out.flush();
        } catch (IOException ie) {
            Log.error(LOGTAG, "send401 io error");
            ie.printStackTrace();
        }
    }

    private void send403(Context ctx) {
        
        ctx.setReturnBy("send403");
        HttpServletResponse resp = ctx.getHttpResponse();
        resp.setStatus(403);
        resp.setContentType("text/html;charset=UTF-8");
        try {
            PrintWriter out = resp.getWriter();
            out.write("<!DOCTYPE html><html><head><title>Error</title></head><body>");
            out.write("<h1>HTTP Status " + resp.getStatus() + "</h1>");
            out.write("<p>Permission Error</p>");
            out.write("</body></html>");
            out.flush();
        } catch (IOException ie) {
            Log.error(LOGTAG, "send403 io error");
            ie.printStackTrace();
        }
    }

    private void send404(Context ctx) {
        ctx.setReturnBy("send404");
        HttpServletResponse resp = ctx.getHttpResponse();
        resp.setStatus(404);
        resp.setContentType("text/html;charset=UTF-8");
        // resp.setContentType("application/json;charset=UTF-8");

        try {
            PrintWriter out = resp.getWriter();
            out.write("<!DOCTYPE html><html><head><title>Error</title></head><body>");
            out.write("<h1>HTTP Status " + resp.getStatus() + "</h1>");
            out.write("<p>Resource not found</p>");
            out.write("</body></html>");
            out.flush();
        } catch (IOException ie) {
            Log.error(LOGTAG, "send404 io error");
            ie.printStackTrace();
        }
    }
    
    private void send405(Context ctx) {
        ctx.setReturnBy("send405");
        HttpServletResponse resp = ctx.getHttpResponse();
        resp.setStatus(405);
        resp.setContentType("text/html;charset=UTF-8");
        try {
            PrintWriter out = resp.getWriter();
            out.write("<!DOCTYPE html><html><head><title>Error</title></head><body>");
            out.write("<h1>HTTP Status " + resp.getStatus() + "</h1>");
            out.write("<p>Resource not found</p>");
            out.write("</body></html>");
            out.flush();
        } catch (IOException ie) {
            Log.error(LOGTAG, "send405 io error");
            ie.printStackTrace();
        }
    }
    
    private void send500(Context ctx) {
        ctx.setReturnBy("send500");
        HttpServletResponse resp = ctx.getHttpResponse();
        resp.setStatus(500);
        resp.setContentType("text/html;charset=UTF-8");
        try {
            PrintWriter out = resp.getWriter();
            out.write("<!DOCTYPE html><html><head><title>Error</title></head><body>");
            out.write("<h1>HTTP Status " + resp.getStatus() + "</h1>");
            out.write("<p>Server Error</p>");
            out.write("</body></html>");
            out.flush();
        } catch (IOException ie) {
            Log.error(LOGTAG, "send500 io error");
            ie.printStackTrace();
        }
        
    }
    
    private void sendHandlerJsonError(Context ctx, HandlerJsonError err) {
        ctx.setReturnBy("sendHandlerJsonError");
        HttpServletResponse resp = ctx.getHttpResponse();
        String errMsg = "%s[%s][%s:%s]".formatted(
            err.getClass().getSimpleName(),
            err.getStatus(),
            err.getCode(),
            err.getMessage()
        );
        Log.error(LOGTAG, errMsg);
        resp.setStatus(err.getStatus());
        resp.setContentType("application/json;charset=utf-8");
        try {
            JSONMAPPER.writeValue(
                resp.getOutputStream(),
                Map.of("code", err.getCode(), "message", err.getMessage())
            );
        } catch (Exception e) {
            Log.error(LOGTAG, e.getMessage());
        }
    }
    
    private void sendHandlerTextError(Context ctx, HandlerTextError err) {
        ctx.setReturnBy("sendHandlerTextError");
        HttpServletResponse resp = ctx.getHttpResponse();
        String errMsg = "%s[%s][%s:%s]".formatted(
            err.getClass().getSimpleName(),
            err.getStatus(),
            err.getCode(),
            err.getMessage()
        );
        Log.error(LOGTAG, errMsg);
        resp.setStatus(err.getStatus());
        resp.setContentType("text/plain;charset=utf-8");
        try {
            resp.getWriter().write(err.getCode() + ":" + err.getMessage());
        } catch (Exception e) {
            Log.error(LOGTAG, e.getMessage());
        }
    }

    private void fallback(Context ctx) {
        ctx.setReturnBy("fallback");
        HttpServletResponse resp = ctx.getHttpResponse();
        resp.setStatus(200);
        resp.setContentType("text/html;charset=UTF-8");
        try {
            Path path = Path.of(getServletContext().getRealPath("/"), "index.html");
            String html = Files.readString(path, StandardCharsets.UTF_8);
            PrintWriter out = resp.getWriter();
            out.write(html);
            out.flush();
        } catch (IOException ie) {
            Log.error(LOGTAG, "send404 io error");
            ie.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildParamMap(HttpServletRequest req) 
            throws ServletException, IOException{
        Map<String, Object> map = new HashMap<>();

        // query + form 参数
        req.getParameterMap().forEach((k, v) -> {
            if (v.length == 1) map.put(k, v[0]);
            else map.put(k, List.of(v));
        });

        String contentType = req.getContentType();
        if (contentType == null) return map;

        // JSON body
        if (contentType.contains("json")) {
            String body = req.getReader().lines().reduce("", (a, b) -> a + b);
            // map.put("_rawBody", body);

            if (!body.isBlank()) {
                Map<String, Object> json = JSONMAPPER.readValue(body, Map.class);
                
                map.putAll(json);
            }
        } else if (contentType.contains("application/x-www-form-urlencoded")){
            Map<String, String[]> paramMap = req.getParameterMap();
        
            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                String[] values = entry.getValue();
                map.put(entry.getKey(), values.length == 1 ? values[0] : values);
            }
        } else if (contentType.contains("multipart/form-data")) {
            for (Part part : req.getParts()) {

                String name = part.getName();

                // 文件
                if (part.getSubmittedFileName() != null) {
                    Object existing = map.get(name);

                    if (existing == null) {
                        map.put(name, part);
                    } else if (existing instanceof Part) {
                        map.put(name, new ArrayList<>(List.of((Part) existing, part)));
                    } else if (existing instanceof List<?> list) {
                        ((List<Part>) list).add(part);
                    }
                } else {
                    String value = new String(part.getInputStream().readAllBytes(), "UTF-8");
                    map.put(name, value);
                }
            }
        }

        return map;
    }

// end of class
}
