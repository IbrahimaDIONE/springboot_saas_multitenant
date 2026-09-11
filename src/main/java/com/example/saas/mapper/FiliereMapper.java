package com.example.saas.mapper;

import com.example.saas.domain.Filiere;
import com.example.saas.dto.FiliereResponse;
import org.springframework.stereotype.Component;

@Component
public class FiliereMapper {
    public FiliereResponse toResponse(Filiere filiere) {
        return new FiliereResponse(filiere.getId(), filiere.getNom());
    }
}