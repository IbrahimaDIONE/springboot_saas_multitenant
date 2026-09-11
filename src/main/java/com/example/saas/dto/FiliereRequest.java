package com.example.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FiliereRequest(@NotBlank @Size(max = 100) String nom) {}