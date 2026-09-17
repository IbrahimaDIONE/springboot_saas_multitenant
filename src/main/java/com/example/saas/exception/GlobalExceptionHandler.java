package com.example.saas.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;

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
                                HttpStatus.UNAUTHORIZED,
                                "UNAUTHORIZED",
                                "Identifiants invalides ou session non autorisée",
                                request,
                                Map.of());
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

        @ExceptionHandler(IllegalStateException.class)
        ResponseEntity<ApiError> stateConflict(
                        IllegalStateException exception, HttpServletRequest request) {
                return build(
                                HttpStatus.CONFLICT,
                                "BUSINESS_CONFLICT",
                                exception.getMessage(),
                                request,
                                Map.of());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        ResponseEntity<ApiError> badRequest(
                        IllegalArgumentException exception, HttpServletRequest request) {
                return build(
                                HttpStatus.BAD_REQUEST,
                                "INVALID_REQUEST",
                                exception.getMessage(),
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
                .forEach(error -> fields.putIfAbsent(error.getField(), frenchValidationMessage(error)));
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

        private String frenchValidationMessage(FieldError error) {
                String message = error.getDefaultMessage();
                if (message != null && !message.startsWith("must ") && !message.startsWith("size must")) {
                        return message;
                }
                String[] codes = error.getCodes() == null ? new String[0] : error.getCodes();
                for (String code : codes) {
                        if (code.startsWith("NotBlank") || code.startsWith("NotNull")) {
                                return "Ce champ est obligatoire";
                        }
                        if (code.startsWith("Email")) {
                                return "L'adresse e-mail est invalide";
                        }
                        if (code.startsWith("Size")) {
                                return "La longueur de ce champ est invalide";
                        }
                        if (code.startsWith("Pattern")) {
                                return "Le format de ce champ est invalide";
                        }
                }
                return "La valeur fournie est invalide";
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
