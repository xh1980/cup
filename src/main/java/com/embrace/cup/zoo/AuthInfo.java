package com.embrace.cup.zoo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

@Data
public class AuthInfo {
    private Boolean authenticated;
    private String username;
    private String userEmail;
    private String userFullName;
    private List<String> roles;
    private List<String> perms;
    private String employeeNo;



    public static AuthInfo login(
        Boolean authenticated,
        String username, String userEmail, String userFullName, 
        List<String> roles, List<String> perms,
        String employeeNo
    ) {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setAuthenticated(authenticated);
        authInfo.setUsername(username);
        authInfo.setUserEmail(userEmail);
        authInfo.setUserFullName(userFullName);
        authInfo.setRoles(roles);
        authInfo.setPerms(perms);
        authInfo.setEmployeeNo(employeeNo);

        Context ctx = ContextHolder.get();
        HttpServletResponse resp = (HttpServletResponse) ctx.getHttpResponse();
           
        String sessionId = ConfigHolder.SESSION_MANAGER.saveSessionData(
            authInfo.toMap(), ConfigHolder.SESSION_AGE, null
        );
        
        UtilWeb.setCookie(
            resp, ConfigHolder.SESSION_COOKIE_NAME, 
            sessionId, ConfigHolder.SESSION_AGE
        );

        return authInfo;
    }

    public static void logout() {
        Context ctx = ContextHolder.get();
        HttpServletResponse resp = ctx.getHttpResponse();
        String sessionId = ctx.getSessionId();
        ConfigHolder.SESSION_MANAGER.clearSession(sessionId);
        UtilWeb.deleteCookie(resp, ConfigHolder.SESSION_COOKIE_NAME);
    }

    @SuppressWarnings("unchecked")
    public void fromMap(Map<String, Object> map) {
        this.authenticated = Boolean.TRUE.equals(map.get("authenticated"));
        this.username = String.valueOf(map.get("username"));
        this.userEmail = String.valueOf(map.get("userEmail"));
        this.userFullName = String.valueOf(map.get("userFullName"));
        this.roles = (List<String>) map.get("roles");
        this.perms = (List<String>) map.get("perms");
        this.employeeNo = String.valueOf(map.get("employeeNo"));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("authenticated", this.authenticated);
        retMap.put("username", this.username);
        retMap.put("userEmail", this.userEmail);
        retMap.put("userFullName", this.userFullName);
        retMap.put("roles", this.roles);
        retMap.put("perms", this.perms);
        retMap.put("employeeNo", this.employeeNo);

        return retMap;
    }
}
