package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

public record PenaliteResponse(
        UUID id,
        UUID etudiantId,
        UUID empruntId,
        String motif,
        Instant dateApplication,
        String consequence) {
}
