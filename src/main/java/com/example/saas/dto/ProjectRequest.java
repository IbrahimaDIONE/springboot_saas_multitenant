package com.example.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contrat d'entrée distinct de l'entité JPA. */
public record ProjectRequest(
        @NotBlank(message = "Le nom est obligatoire")
                @Size(max = 150, message = "Le nom ne doit pas dépasser 150 caractères")
                String name) {}
