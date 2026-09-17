package com.example.saas.mapper;

import com.example.saas.domain.Fichier;
import com.example.saas.dto.FichierResponse;
import org.springframework.stereotype.Component;

@Component
public class FichierMapper {
    public FichierResponse toResponse(Fichier fichier) {
        return new FichierResponse(
                fichier.getId(),
                fichier.getNomOriginal(),
                fichier.getTailleOctets(),
                fichier.getTypeMime(),
                fichier.isDisponible(),
                fichier.getCreatedAt());
    }
}