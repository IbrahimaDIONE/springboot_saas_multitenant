package com.example.saas.mapper;

import com.example.saas.domain.Ouvrage;
import com.example.saas.dto.*;
import org.springframework.stereotype.Component;

@Component
public class OuvrageMapper {
    public OuvrageResponse toResponse(Ouvrage ouvrage) {
        return new OuvrageResponse(ouvrage.getId(), ouvrage.getTitre(), ouvrage.getAuteur(), ouvrage.getResume(),
                new FiliereResponse(ouvrage.getFiliere().getId(), ouvrage.getFiliere().getNom()),
                new NiveauResponse(ouvrage.getNiveau().getId(), ouvrage.getNiveau().getNom()),
                ouvrage.getCreatedAt(), ouvrage.getUpdatedAt());
    }
}