package com.embrace.cup.zoo;

import jakarta.servlet.http.*;

public class ResponseRedirect implements ResponseWeb {

    private final String location;

    public ResponseRedirect(String location) {
        this.location = location;
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.sendRedirect(location);
    }
}