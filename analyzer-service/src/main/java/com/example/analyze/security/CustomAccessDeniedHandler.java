package com.example.analyze.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 403: sudah login, tapi role-nya tidak diizinkan */
@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Forbidden {} {}: {}", request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());
        SecurityResponseWriter.tulis(response, HttpServletResponse.SC_FORBIDDEN,
                "Forbidden: role kamu tidak punya akses ke endpoint ini");
    }
}
