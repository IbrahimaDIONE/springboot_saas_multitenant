package com.example.saas.dto;

import java.util.List;

/** Informations non sensibles permettant au frontend de connaître la session courante. */
public record SessionResponse(String username, String tenantId, List<String> authorities) {}
