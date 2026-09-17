package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

public record EmpruntResponse(
        UUID    id,
        UUID    ouvrageId,
        String  titreOuvrage,
        String  auteurOuvrage,
        Instant dateEmprunt,
        Instant dateExpiration,
        String  statut
) {}