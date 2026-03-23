package com.embrace.cup.zoo;


import jakarta.servlet.http.*;

import java.io.InputStream;
import java.nio.file.*;

public class ResponseFile implements ResponseWeb {

    private final Path file;
    private final InputStream inputStream;
    private final boolean useStream;

    public ResponseFile(Path file) {
        this.file = file;
        this.inputStream = null;
        this.useStream = false;
    }

    public ResponseFile(InputStream inputStream) {
        this.file = null;
        this.inputStream = inputStream;
        this.useStream = true;
    }

    @Override
    public void render(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/octet-stream");
        
        if (useStream && inputStream != null) {
            try (InputStream is = inputStream) {
                is.transferTo(resp.getOutputStream());
            }
        } else if (file != null) {
            Files.copy(file, resp.getOutputStream());
        } else {
            throw new IllegalStateException("No valid resource available");
        }
    }
}