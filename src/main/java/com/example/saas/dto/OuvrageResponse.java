package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

public record OuvrageResponse(
        UUID id,
        String titre,
        String auteur,
        String resume,
        FiliereResponse filiere,
        NiveauResponse niveau,
        Instant createdAt,
        Instant updatedAt) {}