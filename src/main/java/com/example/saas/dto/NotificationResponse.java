package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID    id,
        UUID    etudiantId,
        String  etudiantPrenom,
        String  etudiantNom,
        String  etudiantUsername,
        String  message,
        String  type,
        boolean lu,
        Instant createdAt
) {}