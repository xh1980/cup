package com.embrace.cup.zoo;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

@Data
public class Context {
    private Map<String, Object> parameters;
    private AuthInfo authInfo;

    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    private ServletContext servletContext;
    private String webAppRoot;
    private String packageName;
    private String className;
    private String classFullName;
    private String uri;
    private Handler handler;
    private String sessionId;
    private Boolean hasCookie = false;
    private String returnBy;
    private ResponseWeb result;
    private Boolean logout = false;
    
    public Boolean stopped() {
        return returnBy != null && !returnBy.isEmpty();
    }
    
    public Boolean handlerPackageError() {
        List<String> hPackages = ConfigHolder.APP_HANDLER_PACKAGES;
        if (hPackages.contains(packageName)) return false;
        return true;
    }

    public Boolean handlerError() {
        return handler == null || !(handler instanceof Handler);
    }

    public Boolean httpMethodError() {
        if (handlerError()) return true;
        if (handler.allowedMethods().contains(httpRequest.getMethod())) return false;
        return true;
    }

    public Boolean authError() {
        if (handlerError()) return true;

        if (handler.LoginRequired() || handler.PermissionRequired()) {
            if (authInfo.getAuthenticated()) return false;
            return true;
        } else {
            return false;
        }
    }

    public Boolean permissionError() {
        if (handlerError()) return true;

        if (handler.PermissionRequired()) {
            List<String> perms = authInfo.getPerms();
            String permName = packageName + "/" + className;
            if (perms.contains(permName)) return false;
            return true;
        } else {
            return false;
        }
    }

    public Boolean resultError() {
        return result == null;
    }

    public void handle() {
        ResponseWeb result = handler.handle(this);
        this.result = result;
    }

    public void render() throws Exception{
        result.render(this.httpRequest, this.httpResponse);
    }
    public ResponseJson renderJson(Object data) {
        return new ResponseJson(data);
    }

    public ResponseFile renderFile(Path path) {
        return new ResponseFile(Path.of(webAppRoot, path.toString()));
    }
    public ResponseFile renderFile(InputStream inputStream) {
        return new ResponseFile(inputStream);
    }

    public ResponseHtml renderHtml(String html) {
        return ResponseHtml.fromString(html);
    }
    public ResponseHtml renderHtml(String html, Integer status) {
        return ResponseHtml.fromString(html, status);
    }
    public ResponseHtml renderHtml(Path path) {
        return ResponseHtml.fromPath(Path.of(webAppRoot, path.toString()));
    }
    public ResponseHtml renderHtml(Path path, Integer status) {
        return ResponseHtml.fromPath(Path.of(webAppRoot, path.toString()), status);
    }
    public ResponseHtml renderHtml(InputStream inputStream) {
        return ResponseHtml.fromStream(inputStream);
    }
    public ResponseHtml renderHtml(InputStream inputStream, Integer status) {
        return ResponseHtml.fromStream(inputStream, status);
    }

    public ResponseRedirect renderRedirect(String location) {
        return new ResponseRedirect(location);
    }

}
