package com.embrace.cup.zoo;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;

public class ResponseJson implements ResponseWeb {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final Object data;

    public ResponseJson(Object data) {
        this.data = data;
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) 
    throws Exception {
        resp.setContentType("application/json;charset=utf-8");
        mapper.writeValue(
            resp.getOutputStream(), 
            Map.of("code", "", "message", "", "data", this.data)
        );
    }
}
