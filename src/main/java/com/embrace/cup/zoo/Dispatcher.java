package com.embrace.cup.zoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Dispatcher extends HttpServlet {

    // private static final ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    // private static final ConcurrentMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Handler> HANDLER_CACHE = new ConcurrentHashMap<>();
    private static final String LOGTAG = "Dispatcher";
    private static final String APP_PACKAGE = Config.get("app.package");
    private static final ObjectMapper JSONMAPPER = new ObjectMapper();
    private static final String SESSION_COOKIE_NAME = "ctxid";
    private static final int SESSION_AGE = 60*60*6;
    private static final SessionManager sessManager = new SessionManager();
    private static final String[] PACKAGE_ARRAY;

    static {
        String cps = Config.get("controller.packages");
        String[] arr = cps.split(",");
        PACKAGE_ARRAY = Arrays.stream(arr)
            .filter(s -> s != null && !s.trim().isEmpty())
            .toArray(String[]::new);
        if (PACKAGE_ARRAY.length <= 0)
            throw new RuntimeException("controller.packages error! check application.yml");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        String classFullName = "";
        try {
            Log.setRequestId();
            RequestContext.set(new HashMap<>());

            classFullName = getClassFullName(req, resp);
            if (classFullName == null) { send404(req, resp); return; }

            Handler handler = getHandler(classFullName);
            
            if (handler == null)  { send404(req, resp); return; }

            Map<String, Object> paramMap = buildParamMap(req); // construct parameters Map
            
            getSession(req, resp); // session to RequestContext
            
            if (!checkLogin()) { send401(req, resp); return; }

            if (!checkPermission()) { send403(req, resp); return; }
            
            Log.info(LOGTAG, "before " + classFullName);
            ResponseWeb result = handler.handle(paramMap);
            Log.info(LOGTAG, "after  " + classFullName);
            
            if (result != null) {
                result.render(req, resp);
            } else {
                Log.error(LOGTAG, handler.getClass().getSimpleName() + " handler return null");
                send500(req, resp);
            }

        } catch (ClassNotFoundException e) {
            send404(req, resp);
        } catch (NoSuchMethodException e) {
            send404(req, resp);
        } catch (InvocationTargetException  e) {
            
        } catch (ErrorJson ej) {
            sendErrorJson(req, resp, ej);
        } catch (Exception e) {
            send500(req, resp);
            e.printStackTrace();

        } finally {
            Log.clearRequestId();
            RequestContext.clear();
        }
    }


    private String getClassFullName(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        Log.info(LOGTAG, "uri:" + uri);

        var ctxData = RequestContext.get();
        // String context_path = req.getContextPath();
        // String servlet_path = req.getServletPath();
        // String path = req.getPathInfo();
        ctxData.put("uri", uri);

        // split uri
        String[] parts = uri.substring(1).split("/");
        if (parts.length < 2) return null;

        String packageName = parts[0];
        String className = parts[1];
        
        if (!Arrays.asList(PACKAGE_ARRAY).contains(packageName)) return null;
        
        String classFullName = APP_PACKAGE + "." + packageName + "." + className;
        ctxData.put("classFullName", classFullName);
        ctxData.put("className", className);

        return classFullName;
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

    // private Class<?> getActionClass(String classFullName) 
    // throws ClassNotFoundException {
    //     Class<?> clazz = CLASS_CACHE.get(classFullName);
    //     if (clazz == null) {
    //         clazz = Class.forName(classFullName);
    //         Class<?> existing = CLASS_CACHE.putIfAbsent(classFullName, clazz);
    //         if (existing != null) {
    //             clazz = existing; // 另一线程已经放进去
    //         }
    //     }
    //     return clazz;
    // }

    // private Method getHandlerMethod(String classFullName, String methodName) 
    // throws ClassNotFoundException, NoSuchMethodException{
        
    //     String key = classFullName + "#" + methodName;
    //     Method m = METHOD_CACHE.get(key);
    //     if (m == null) {
    //         Class<?> clazz = getActionClass(classFullName);
    //         m = clazz.getMethod(methodName, Map.class);
    //         Method existing = METHOD_CACHE.putIfAbsent(key, m);
    //         if (existing != null) {
    //             m = existing;
    //         }
    //     }
        
    //     return m;
    // }

    private void getSession(HttpServletRequest req, HttpServletResponse resp) {
        // get cookie set RequestContext
        String sessionId = WebUtil.getCookie(req, SESSION_COOKIE_NAME);
        if (sessionId == null) return;
        
        Map<String, Object> ctxData = RequestContext.get();
        ctxData.put("sessionId", sessionId);
        
        Map<String, Object> sessData = sessManager.getSessionData(sessionId);
        if (sessData == null) return;

        ctxData.putAll(sessData);
        sessionId = sessManager.saveSessionData(sessData, SESSION_AGE, sessionId);
        WebUtil.setCookie(resp, SESSION_COOKIE_NAME, sessionId, SESSION_AGE);
    }

    private boolean checkLogin() {
        Map<String,Object> ctxData = RequestContext.get();
        Boolean loginRequired = Boolean.TRUE.equals(ctxData.get("loginRequired"));
        Boolean authenticated =  Boolean.TRUE.equals(ctxData.get("authenticated"));
        if (loginRequired == true) {
            if (authenticated == false) return false;
        }
        return true;
    }

    private boolean checkPermission() {
        Map<String,Object> ctxData = RequestContext.get();
        Boolean loginRequired = Boolean.TRUE.equals(ctxData.get("loginRequired"));
        String[] perms = (String[])ctxData.get("permissions");
        String className = (String)ctxData.get("className");
        if (loginRequired == true) {
            if (perms == null) return false;
            if (!Arrays.asList(perms).contains(className)) return false;
        }
        
        return true;
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
        resp.setStatus(err.status);
        resp.setContentType("application/json;charset=utf-8");
        try {
            JSONMAPPER.writeValue(
                resp.getOutputStream(),
                Map.of("code",err.code, "message",err.getMessage())
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

    // private void forwardTo404(HttpServletRequest req, HttpServletResponse resp) {
    //     RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
    //     try {
    //             dispatcher.forward(req, resp);
    //     } catch (ServletException | IOException e1) {
    //         e1.printStackTrace();
    //         resp.setStatus(404);
    //     }
    // }

// end of class
}
