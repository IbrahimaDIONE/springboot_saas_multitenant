package com.example.saas.dto;

import jakarta.validation.constraints.*;

public record AdminEtablissementRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @NotBlank @Email @Size(max = 150) String email) {}