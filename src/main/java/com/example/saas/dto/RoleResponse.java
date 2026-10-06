package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

public record RoleResponse(UUID id, String code, String description, boolean actif,
                           Instant createdAt, Instant updatedAt) {}
