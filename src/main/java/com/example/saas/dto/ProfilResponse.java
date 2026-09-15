package com.example.saas.dto;

public record ProfilResponse(
        String username, String nom, String prenom, String email, String role, String tenantId) {}