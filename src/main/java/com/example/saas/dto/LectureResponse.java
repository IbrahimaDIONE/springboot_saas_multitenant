package com.example.saas.dto;

import java.util.UUID;

public record LectureResponse(
        UUID   empruntId,
        UUID   ouvrageId,
        String titreOuvrage,
        String urlFichier
) {}