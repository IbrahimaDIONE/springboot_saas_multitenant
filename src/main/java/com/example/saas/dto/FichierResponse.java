package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

public record FichierResponse(
        UUID id,
        String nomOriginal,
        long tailleOctets,
        String typeMime,
        boolean disponible,
        Instant createdAt) {}