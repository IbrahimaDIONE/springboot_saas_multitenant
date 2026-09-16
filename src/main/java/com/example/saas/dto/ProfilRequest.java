package com.example.saas.dto;

public record ProfilRequest(String nom, String prenom, String email) {}
import jakarta.validation.constraints.*;

public record ProfilRequest(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @NotBlank @Email @Size(max = 150) String email) {}
