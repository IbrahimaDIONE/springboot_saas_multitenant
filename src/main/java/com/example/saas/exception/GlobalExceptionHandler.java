package com.example.saas.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Traduit centralement les exceptions en contrat HTTP. Les détails inattendus sont journalisés mais
 * jamais exposés au client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return build(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                request,
                Map.of());
    }

    @ExceptionHandler({InvalidTokenException.class, AuthenticationException.class})
    ResponseEntity<ApiError> unauthorized(RuntimeException exception, HttpServletRequest request) {
        return build(
                HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(AccessDeniedException exception, HttpServletRequest request) {
        return build(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "Accès refusé pour ce rôle",
                request,
                Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        return build(
                HttpStatus.CONFLICT,
                "DATA_CONFLICT",
                "La ressource existe déjà ou est encore utilisée",
                request,
                Map.of());
    }

    @ExceptionHandler(InvalidTenantException.class)
    ResponseEntity<ApiError> invalidTenant(
            InvalidTenantException exception, HttpServletRequest request) {
        return build(
                HttpStatus.BAD_REQUEST,
                "INVALID_TENANT",
                exception.getMessage(),
                request,
                Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return build(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Les données envoyées sont invalides",
                request,
                fields);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Erreur inattendue sur {}", request.getRequestURI(), exception);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Une erreur interne est survenue",
                request,
                Map.of());
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> fields) {
        return ResponseEntity.status(status)
                .body(
                        new ApiError(
                                Instant.now(),
                                status.value(),
                                code,
                                message,
                                request.getRequestURI(),
                                fields));
    }
}
