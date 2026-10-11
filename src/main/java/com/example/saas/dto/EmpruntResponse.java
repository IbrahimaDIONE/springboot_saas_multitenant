package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

public record EmpruntResponse(
        UUID    id,
        UUID    etudiantId,
        UUID    ouvrageId,
        String  titreOuvrage,
        String  auteurOuvrage,
        Instant dateEmprunt,
        Instant dateExpiration,
        Instant dateRetour,
        String  statut
) {}