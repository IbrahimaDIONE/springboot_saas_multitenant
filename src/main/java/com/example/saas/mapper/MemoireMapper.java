package com.example.saas.mapper;

import com.example.saas.domain.Memoire;
import com.example.saas.dto.*;
import com.example.saas.repository.FichierRepository;
import org.springframework.stereotype.Component;

@Component
public class MemoireMapper {
    private final FichierRepository fichierRepository;
    private final FichierMapper fichierMapper;

    public MemoireMapper(FichierRepository fichierRepository, FichierMapper fichierMapper) {
        this.fichierRepository = fichierRepository;
        this.fichierMapper = fichierMapper;
    }

    public MemoireResponse toResponse(Memoire memoire) {
        var fichiers = fichierRepository.findAllByMemoireIdAndTenantId(memoire.getId(), memoire.getTenantId())
                .stream().map(fichierMapper::toResponse).toList();
        return new MemoireResponse(
                memoire.getId(),
                memoire.getTitre(),
                memoire.getAuteur(),
                memoire.getEncadreur(),
                memoire.getResume(),
                memoire.getAnnee(),
                new FiliereResponse(memoire.getFiliere().getId(), memoire.getFiliere().getNom()),
                new NiveauResponse(memoire.getNiveau().getId(), memoire.getNiveau().getNom()),
                memoire.isActif(),
                fichiers,
                memoire.getCreatedAt(),
                memoire.getUpdatedAt());
    }
}