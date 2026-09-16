package com.example.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NiveauRequest(@NotBlank @Size(max = 100) String nom) {}