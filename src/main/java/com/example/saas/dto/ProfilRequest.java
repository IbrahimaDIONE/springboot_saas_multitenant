package com.example.saas.dto;

import jakarta.validation.constraints.*;

public record ProfilRequest(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @NotBlank @Email @Size(max = 150) String email) {}