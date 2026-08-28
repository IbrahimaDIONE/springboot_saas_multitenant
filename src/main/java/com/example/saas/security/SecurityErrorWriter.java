package com.example.saas.security;

import com.example.saas.exception.ApiError;

import jakarta.servlet.http.*;

import org.springframework.http.*;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/** Écrit les erreurs 401/403 avec le même contrat JSON que GlobalExceptionHandler. */
@Component
public class SecurityErrorWriter {
    private final JsonMapper jsonMapper;

    public SecurityErrorWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        if (status == HttpStatus.UNAUTHORIZED) {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"saas-demo\"");
        }
        jsonMapper.writeValue(
                response.getOutputStream(),
                new ApiError(
                        Instant.now(),
                        status.value(),
                        code,
                        message,
                        request.getRequestURI(),
                        Map.of()));
    }
}
