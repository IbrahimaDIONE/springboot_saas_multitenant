package com.example.saas.dto;

import java.util.UUID;

public record EtudiantResponse(
        UUID id,
        UUID utilisateurId,
        String username,
        String nom,
        String prenom,
        String email,
        boolean enabled,
        UUID filiereId,
        UUID niveauId) {}