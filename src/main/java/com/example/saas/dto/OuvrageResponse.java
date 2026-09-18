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
        CategorieResponse categorie,
        boolean actif,
        Instant createdAt,
        Instant updatedAt) {}