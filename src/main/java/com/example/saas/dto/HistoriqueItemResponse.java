package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

public record HistoriqueItemResponse(
        String typeAction,
        String typeRessource,
        UUID ressourceId,
        String titre,
        String statut,
        Instant date) {}