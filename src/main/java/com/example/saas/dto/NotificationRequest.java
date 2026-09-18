package com.example.saas.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record NotificationRequest(
        @NotNull(message = "L'identifiant de l'étudiant est obligatoire")
        UUID etudiantId,

        @NotBlank(message = "Le message est obligatoire")
        String message,

        @NotBlank(message = "Le type est obligatoire")
        String type
) {}