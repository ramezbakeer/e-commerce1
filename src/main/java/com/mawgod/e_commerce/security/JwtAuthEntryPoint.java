package com.mawgod.e_commerce.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns a JSON 401 response instead of the default Spring Security redirect,
 * so REST clients receive a consistent error envelope.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String body = """
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "Authentication required. Please provide a valid Bearer token.",
                  "timestamp": "%s",
                  "fieldErrors": null
                }
                """.formatted(LocalDateTime.now());

        response.getWriter().write(body);
    }
}