package com.example.saas.dto;

public record DashboardEtablissementResponse(
        long nombreEtudiants,
        long nombreRessources,
        long empruntsEnCours,
        long empruntsExpires,
        long empruntsRetardes) {}
