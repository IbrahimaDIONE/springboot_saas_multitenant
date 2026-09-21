package com.example.saas.service;

import com.example.saas.domain.Consultation.TypeRessource;
import java.util.UUID;

public interface ConsultationService {
    void enregistrer(TypeRessource typeRessource, UUID ressourceId);
}