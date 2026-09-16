package com.example.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OuvrageRequest(
        @NotBlank @Size(max = 200) String titre,
        @NotBlank @Size(max = 150) String auteur,
        @NotNull String resume,
        @NotNull UUID filiereId,
        @NotNull UUID niveauId) {}