package com.example.saas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.Map;

public record EtablissementRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String nom,
        @NotBlank @Pattern(regexp = "ACTIF|INACTIF") String statut,
        Map<String, String> parametres,
        @Valid AdminEtablissementRequest administrateur) {}