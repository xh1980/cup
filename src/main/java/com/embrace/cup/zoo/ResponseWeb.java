package com.embrace.cup.zoo;

import jakarta.servlet.http.*;

public interface ResponseWeb {
    void render(HttpServletRequest req, HttpServletResponse resp) throws Exception;
}
