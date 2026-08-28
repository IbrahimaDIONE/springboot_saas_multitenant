package com.example.saas.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/** tenantId absent : il est pris dans le JWT. */
public record ProductRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @PositiveOrZero int stock,
        @NotBlank
                @Size(max = 2048)
                @Pattern(regexp = "https?://.+", message = "L'image doit être une URL HTTP(S)")
                String imageUrl,
        @NotNull UUID categoryId) {}
