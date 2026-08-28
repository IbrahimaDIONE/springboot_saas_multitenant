package com.example.saas.dto;

public record AuthResponse(
        String tokenType, String accessToken, long expiresIn, String refreshToken) {}
