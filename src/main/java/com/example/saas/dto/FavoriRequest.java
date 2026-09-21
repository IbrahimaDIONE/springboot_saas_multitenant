package com.example.saas.dto;

import com.example.saas.domain.Favori.TypeRessource;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FavoriRequest(
        @NotNull TypeRessource typeRessource,
        @NotNull UUID ressourceId) {}