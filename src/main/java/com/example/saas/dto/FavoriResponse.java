package com.example.saas.dto;

import com.example.saas.domain.Favori.TypeRessource;
import java.time.Instant;
import java.util.UUID;

public record FavoriResponse(
        UUID id,
        TypeRessource typeRessource,
        UUID ressourceId,
        String titre,
        Instant createdAt) {}