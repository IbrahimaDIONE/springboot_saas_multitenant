package com.example.saas.exception;

import java.time.Instant;
import java.util.Map;

/** Format d'erreur uniforme consommable par tous les frontends. */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        Map<String, String> validationErrors) {}
