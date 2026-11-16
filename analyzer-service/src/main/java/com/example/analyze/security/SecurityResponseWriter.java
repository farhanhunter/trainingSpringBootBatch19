package com.example.analyze.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Error dari Spring Security terjadi SEBELUM masuk controller,
 * jadi tidak ditangkap GlobalExceptionHandler. Bentuk JSON-nya
 * ditulis manual supaya tetap sama dengan BaseResponse.
 */
public final class SecurityResponseWriter {
    private SecurityResponseWriter() {}

    public static void tulis(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":\"failed\",\"message\":\"" + message + "\",\"data\":null}");
    }
}
