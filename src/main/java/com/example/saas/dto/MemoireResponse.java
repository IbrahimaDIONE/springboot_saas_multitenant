package com.example.saas.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MemoireResponse(
        UUID id,
        String titre,
        String auteur,
        String encadreur,
        String resume,
        int annee,
        FiliereResponse filiere,
        NiveauResponse niveau,
        boolean actif,
        List<FichierResponse> fichiers,
        Instant createdAt,
        Instant updatedAt) {}