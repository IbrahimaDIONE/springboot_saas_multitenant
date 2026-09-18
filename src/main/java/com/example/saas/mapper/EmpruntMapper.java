package com.example.saas.mapper;

import com.example.saas.domain.Emprunt;
import com.example.saas.dto.EmpruntResponse;
import org.springframework.stereotype.Component;

@Component
public class EmpruntMapper {

    public EmpruntResponse toResponse(Emprunt e) {
        return new EmpruntResponse(
                e.getId(),
                e.getOuvrage().getId(),
                e.getOuvrage().getTitre(),
                e.getOuvrage().getAuteur(),
                e.getDateEmprunt(),
                e.getDateExpiration(),
                e.getDateRetour(),
                e.getStatut().name()
        );
    }
}