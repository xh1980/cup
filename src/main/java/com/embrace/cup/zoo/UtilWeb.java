package com.embrace.cup.zoo;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UtilWeb {

    public static Map<String, Object> getCookies(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        Map<String, Object> map = new HashMap<>();

        for (Cookie cookie : cookies) {
            map.put(cookie.getName(), cookie.getValue());
        }
        return map;
    }

    public static String getCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void setCookie(HttpServletResponse resp, String name, String value, int ageSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(ageSeconds);
        cookie.setPath("/");
        cookie.setHttpOnly(true); 
        resp.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletResponse resp, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true); 
        resp.addCookie(cookie);
    }

    public static String getAuthrizationHeader(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth == null) return null;

        if (auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            return token;
        } else {
            return auth;
        }
    }
}
