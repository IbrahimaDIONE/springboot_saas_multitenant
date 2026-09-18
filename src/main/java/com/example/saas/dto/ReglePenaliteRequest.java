package com.example.saas.dto;

import jakarta.validation.constraints.*;

public record ReglePenaliteRequest(
        @Min(value = 0, message = "Les jours de tolérance doivent être positifs")
        int joursTolérance,

        @NotBlank(message = "Le type de conséquence est obligatoire")
        String typeConsequence,

        String description
) {}