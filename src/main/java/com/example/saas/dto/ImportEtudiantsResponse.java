package com.example.saas.dto;

import java.util.List;

public record ImportEtudiantsResponse(int total, int ajoutes, List<String> erreurs) {}
