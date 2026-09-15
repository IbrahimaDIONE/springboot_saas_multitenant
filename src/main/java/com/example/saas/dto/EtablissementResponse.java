package com.example.saas.dto;

import java.util.*;

public record EtablissementResponse(
        UUID id, String code, String nom, String statut, Map<String, String> parametres) {}