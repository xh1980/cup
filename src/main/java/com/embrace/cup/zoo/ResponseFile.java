package com.embrace.cup.zoo;


import jakarta.servlet.http.*;
import java.nio.file.*;

public class ResponseFile implements ResponseWeb {

    private final Path file;

    public ResponseFile(Path file) {
        this.file = file;
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/octet-stream");
        Files.copy(file, resp.getOutputStream());
    }
}

