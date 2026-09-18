package com.example.saas.mapper;

import com.example.saas.domain.Penalite;
import com.example.saas.dto.PenaliteResponse;
import org.springframework.stereotype.Component;

@Component
public class PenaliteMapper {
    public PenaliteResponse toResponse(Penalite penalite) {
        return new PenaliteResponse(
                penalite.getId(),
                penalite.getEtudiant().getId(),
                penalite.getEmprunt().getId(),
                penalite.getMotif(),
                penalite.getDateApplication(),
                penalite.getConsequence());
    }
}
