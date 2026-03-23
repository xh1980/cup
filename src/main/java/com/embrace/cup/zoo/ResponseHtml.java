package com.embrace.cup.zoo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseHtml implements ResponseWeb {

    private final String html;
    private final int status;

    private ResponseHtml(String html, int status) {
        this.html = html;
        this.status = status;
    }

    public static ResponseHtml fromString(String html) {
        return new ResponseHtml(html, 200);
    }

    public static ResponseHtml fromString(String html, int status) {
        return new ResponseHtml(html, status);
    }

    public static ResponseHtml fromPath(Path path) {
        return fromPath(path, 200);
    }

    public static ResponseHtml fromPath(Path path, int status) {
        try {
            String html = Files.readString(path, StandardCharsets.UTF_8);
            return new ResponseHtml(html, status);
        } catch (IOException e) {
            return new ResponseHtml("<h1>404 Not Found</h1>", 404);
        }
        
        
    }

    public static ResponseHtml fromStream(InputStream inputStream) {
        return fromStream(inputStream, 200);
    }

    public static ResponseHtml fromStream(InputStream inputStream, int status) {
        try (InputStream is = inputStream) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new ResponseHtml(html, status);
        } catch (IOException e) {
            return new ResponseHtml("<h1>404 Not Found</h1>", 404);
        }
    }

    public static ResponseHtml fromResource(String path) {
        try (InputStream in = ResponseHtml.class
                .getClassLoader()
                .getResourceAsStream(path)) {

            if (in == null) {
                return new ResponseHtml("<h1>404 Not Found</h1>", 404);
            }

            String html = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return new ResponseHtml(html, 200);

        } catch (Exception e) {
            return new ResponseHtml("<h1>500 Server Error</h1>", 500);
        }
    }

    public String getHtml() {
        return html;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setStatus(getStatus());
        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().write(getHtml());
    }
}