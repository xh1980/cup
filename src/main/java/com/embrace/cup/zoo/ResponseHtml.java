package com.embrace.cup.zoo;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseHtml implements ResponseWeb {

    private final String html;
    private final int status;

    public ResponseHtml(String html) {
        this(html, 200);
    }

    public ResponseHtml(String html, int status) {
        this.html = html;
        this.status = status;
    }

    public String getHtml() {
        return html;
    }

    public int getStatus() {
        return status;
    }

    public static ResponseHtml fromFile(String path) {
        try (InputStream in = ResponseHtml.class
                .getClassLoader()
                .getResourceAsStream(path)) {

            if (in == null) {
                return new ResponseHtml("<h1>404 Not Found</h1>", 404);
            }

            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return new ResponseHtml(html);

        } catch (Exception e) {
            return new ResponseHtml("<h1>500 Server Error</h1>", 500);
        }
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setStatus(getStatus());
        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().write(getHtml());
    }
}



