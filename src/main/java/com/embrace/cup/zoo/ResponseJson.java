package com.embrace.cup.zoo;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;

public class ResponseJson implements ResponseWeb {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final Object data;

    private ResponseJson(Object data) {
        this.data = data;
    }

    public static ResponseJson ok(Object data) {
        return new ResponseJson(data);
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=utf-8");
        mapper.writeValue(resp.getOutputStream(), data);
    }
}
