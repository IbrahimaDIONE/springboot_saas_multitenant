package com.example.saas.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        int stock,
        String imageUrl,
        CategoryResponse category,
        Instant createdAt,
        Instant updatedAt) {}
