package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID    id,
        UUID    etudiantId,
        String  message,
        String  type,
        boolean lu,
        Instant createdAt
) {}