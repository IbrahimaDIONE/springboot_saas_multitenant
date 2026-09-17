package com.example.saas.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record MemoireRequest(
        @NotBlank @Size(max = 255) String titre,
        @NotBlank @Size(max = 150) String auteur,
        @NotBlank @Size(max = 150) String encadreur,
        @NotBlank String resume,
        @Min(2000) int annee,
        @NotNull UUID filiereId,
        @NotNull UUID niveauId) {}