package com.embrace.cup.zoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Dispatcher extends HttpServlet {

    // private static final ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    // private static final ConcurrentMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Handler> HANDLER_CACHE = new ConcurrentHashMap<>();
    private static final String LOGTAG = "Dispatcher";
    private static final ObjectMapper JSONMAPPER = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {

        try {
            Log.setRequestId();
            Context ctx = new Context();
            ContextHolder.set(ctx);
            ctx.setHttpRequest(req);
            ctx.setHttpResponse(resp);

            var allowed = List.of("GET", "POST");
            String httpMethod = req.getMethod();
            if (!allowed.contains(httpMethod)) 
                { send405(req, resp); return; }
                
            setPathToContext(req, resp);
            String classFullName = ctx.getClassFullName();
            if (classFullName == null || classFullName.isBlank()) 
                { send404(req, resp); return; }

            Handler handler = getHandler(classFullName);
            
            if (handler == null)  { send404(req, resp); return; }

            Map<String, Object> paramMap = buildParamMap(req);
            ctx.setParameters(paramMap);

            sessionToContext(req, resp);

            if (!checkLogin()) { send401(req, resp); return; }
            if (!checkPermission()) { send403(req, resp); return; }
            
            Log.info(LOGTAG, "before " + classFullName);
            ResponseWeb result = handler.handle(ctx);
            Log.info(LOGTAG, "after  " + classFullName);
            
            if (result != null) {
                result.render(req, resp);
            } else {
                Log.error(
                    LOGTAG, 
                    handler.getClass().getSimpleName() + " handler return null");
                send500(req, resp);
            }

        } catch (ClassNotFoundException e) {
            send404(req, resp);
        } catch (ErrorJson ej) {
            sendErrorJson(req, resp, ej);
        } catch (Exception e) {
            send500(req, resp);
            e.printStackTrace();
        } finally {
            Log.clearRequestId();
            ContextHolder.clear();
        }
    }


    private void setPathToContext(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        Log.info(LOGTAG, "uri:" + uri);

        Context ctx = ContextHolder.get();
        ctx.setUri(uri);

        String[] parts = uri.substring(1).split("/");
        if (parts.length < 2) return;

        String packageName = parts[0];
        String className = parts[1];
        ctx.setPackageName(packageName);
        ctx.setClassName(className);
        
        String classFullName = ConfigHolder.APP_PACKAGE 
                            + "." + packageName 
                            + "." + className;
        ctx.setClassFullName(classFullName);

    }

       
    private Handler getHandler(String classFullName) {

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
                    throw new Exception("Class is not Handler");
                }
            } catch (Exception e) {
                Log.error(LOGTAG, classFullName + "handler create error");
                e.printStackTrace();
            }
        }   

        return h;
    }

    private void sessionToContext(HttpServletRequest req, HttpServletResponse resp) {

        Context ctx = ContextHolder.get();
        AuthInfo authInfo = new AuthInfo();
        authInfo.setAuthenticated(false);
        ctx.setAuthInfo(authInfo);

        String sessionId = UtilWeb.getCookie(req, ConfigHolder.SESSION_COOKIE_NAME);
        if (sessionId == null) return;
        
        SessionManager manager = ConfigHolder.SESSION_MANAGER;
        Map<String, Object> sessData = manager.getSessionData(sessionId);
        if (sessData == null) return;

        authInfo.fromMap(sessData);
        sessionId = manager.saveSessionData(sessData, 
            ConfigHolder.SESSION_AGE, sessionId
        );
        ctx.setSessionId(sessionId);

        UtilWeb.setCookie(
            resp, ConfigHolder.SESSION_COOKIE_NAME, 
            sessionId, ConfigHolder.SESSION_AGE
        );
    }

    private boolean checkLogin() {
        Context ctx = ContextHolder.get();

        String className = ctx.getClassName();
        if (ConfigHolder.AUTH_EXCLUDE_LIST.contains(className)) return true;

        AuthInfo authInfo = ctx.getAuthInfo();
        if (authInfo.getAuthenticated()) return true;
        
        return false;
    }

    private boolean checkPermission() {
        Context ctx = ContextHolder.get();
        AuthInfo authInfo = ctx.getAuthInfo();

        List<String> perms = authInfo.getPerms();
        String className = ctx.getClassFullName();
        
        if (className == null) return false;

        if (ConfigHolder.AUTH_EXCLUDE_LIST.contains(className)) return true;
        if (ConfigHolder.AUTH_DEFAULT_PERM_LIST.contains(className)) return true;
        
        if (perms == null) return false;
        if (perms.contains(className)) return true;
        
        return false;
    }

    private void send401(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(401);
        resp.setContentType("application/json;charset=UTF-8");

        resp.setContentType("text/html;charset=UTF-8");
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

    private void send403(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(403);
        resp.setContentType("application/json;charset=UTF-8");

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

    private void send404(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(404);
        resp.setContentType("application/json;charset=UTF-8");

        resp.setContentType("text/html;charset=UTF-8");
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
    
    private void send405(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(404);
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
    
    private void send500(HttpServletRequest req, HttpServletResponse resp){
        resp.setStatus(500);
        resp.setContentType("application/json;charset=UTF-8");

        resp.setContentType("text/html;charset=UTF-8");
        try {
            PrintWriter out = resp.getWriter();
            out.write("<!DOCTYPE html><html><head><title>Error</title></head><body>");
            out.write("<h1>HTTP Status " + resp.getStatus() + "</h1>");
            out.write("<p>Server Error</p>");
            out.write("</body></html>");
            out.flush();
        } catch (IOException ie) {
            Log.error(LOGTAG, "send404 io error");
            ie.printStackTrace();
        }
        
    }
    
    private void sendErrorJson(HttpServletRequest req, HttpServletResponse resp, ErrorJson err) {
        String errMsg = "%s[%s:%s]".formatted(
            err.getClass().getSimpleName(),
            err.code,
            err.getMessage()
        );
        Log.error(LOGTAG, errMsg);
        resp.setStatus(200);
        resp.setContentType("application/json;charset=utf-8");
        try {
            JSONMAPPER.writeValue(
                resp.getOutputStream(),
                Map.of("code", err.code, "message", err.getMessage())
            );
        } catch (Exception jsonEx) {
            Log.error(LOGTAG, jsonEx.getMessage());
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
        if (contentType.startsWith("application/json")) {
            String body = req.getReader().lines().reduce("", (a, b) -> a + b);
            // map.put("_rawBody", body);

            if (!body.isBlank()) {
                Map<String, Object> json = JSONMAPPER.readValue(body, Map.class);
                
                map.putAll(json);
            }
        }

        // 文件上传 multipart/form-data
        if (contentType.startsWith("multipart/form-data")) {
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
                }
                // 普通字段
                else {
                    String value = new String(part.getInputStream().readAllBytes(), "UTF-8");
                    map.put(name, value);
                }
            }
        }

        return map;
    }

// end of class
}
