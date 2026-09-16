package com.example.saas.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EmpruntRequest(
        @NotNull(message = "L'identifiant de l'ouvrage est obligatoire")
        UUID ouvrageId
) {}