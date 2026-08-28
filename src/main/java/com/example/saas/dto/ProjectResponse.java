package com.example.saas.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrat de sortie. tenantId n'est volontairement pas exposé : il provient du contexte sécurisé,
 * jamais du corps envoyé par le client.
 */
public record ProjectResponse(UUID id, String name, Instant createdAt, Instant updatedAt) {}
