package com.embrace.cup.zoo;

import jakarta.servlet.http.*;

public class ResponseRedirect implements ResponseWeb {

    private final String location;

    private ResponseRedirect(String location) {
        this.location = location;
    }

    public static ResponseRedirect to(String url) {
        return new ResponseRedirect(url);
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.sendRedirect(location);
    }
}