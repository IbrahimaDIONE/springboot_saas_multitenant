package com.example.saas.service.impl;

import com.example.saas.domain.*;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.EmpruntMapper;
import com.example.saas.repository.*;
import com.example.saas.service.EmpruntService;
import com.example.saas.tenant.TenantContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class EmpruntServiceImpl implements EmpruntService {

    private static final int DUREE_EMPRUNT_JOURS = 14;

    private final EmpruntRepository  empruntRepository;
    private final OuvrageRepository  ouvrageRepository;
    private final EtudiantRepository etudiantRepository;
    private final EmpruntMapper      mapper;

    public EmpruntServiceImpl(
            EmpruntRepository empruntRepository,
            OuvrageRepository ouvrageRepository,
            EtudiantRepository etudiantRepository,
            EmpruntMapper mapper) {
        this.empruntRepository  = empruntRepository;
        this.ouvrageRepository  = ouvrageRepository;
        this.etudiantRepository = etudiantRepository;
        this.mapper             = mapper;
    }

        /** Récupère le username de l'utilisateur connecté depuis le JWT. */
        private String currentUsername() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
                return jwt.getSubject();
    }

    @Override
    public EmpruntResponse emprunter(EmpruntRequest request) {
        String tenantId = TenantContext.get();
        String username = currentUsername();

        Etudiant etudiant = etudiantRepository
                .findByUtilisateurUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Étudiant non trouvé"));

        Ouvrage ouvrage = ouvrageRepository
                .findByIdAndTenantIdAndActifTrue(request.ouvrageId(), tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ouvrage non trouvé"));

        boolean dejaEmprunte = empruntRepository
                .existsByTenantIdAndEtudiantIdAndOuvrageIdAndStatut(
                        tenantId,
                        etudiant.getId(),
                        ouvrage.getId(),
                        Emprunt.Statut.ACTIF);

        if (dejaEmprunte) {
            throw new IllegalStateException(
                    "Vous avez déjà un emprunt actif sur cet ouvrage.");
        }

        Instant expiration = Instant.now()
                .plus(DUREE_EMPRUNT_JOURS, ChronoUnit.DAYS);
        Emprunt emprunt = new Emprunt(
                tenantId, etudiant, ouvrage, expiration);

        return mapper.toResponse(empruntRepository.save(emprunt));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpruntResponse> mesEmprunts() {
        String tenantId = TenantContext.get();
        String username = currentUsername();

        Etudiant etudiant = etudiantRepository
                .findByUtilisateurUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Étudiant non trouvé"));

        return empruntRepository
                .findAllByTenantIdAndEtudiantId(
                        tenantId, etudiant.getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpruntResponse findById(UUID id) {
        return mapper.toResponse(
                empruntRepository
                        .findByIdAndTenantId(id, TenantContext.get())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Emprunt non trouvé"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpruntResponse> findAllByTenant(String statut) {
        String tenantId = TenantContext.get();

        if (statut != null && !statut.isBlank()) {
            Emprunt.Statut s = Emprunt.Statut.valueOf(
                    statut.toUpperCase());
            return empruntRepository
                    .findAllByTenantIdAndStatutOrderByDateEmpruntDesc(
                            tenantId, s)
                    .stream()
                    .map(mapper::toResponse)
                    .toList();
        }

        return empruntRepository
                .findAllByTenantIdOrderByDateEmpruntDesc(tenantId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LectureResponse lireEnLigne(UUID empruntId) {
        String tenantId = TenantContext.get();
        String username = currentUsername();

        Etudiant etudiant = etudiantRepository
                .findByUtilisateurUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Étudiant non trouvé"));

        Emprunt emprunt = empruntRepository
                .findByIdAndTenantId(empruntId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Emprunt non trouvé"));

        if (!emprunt.getEtudiant().getId().equals(etudiant.getId())) {
            throw new ResourceNotFoundException("Emprunt non trouvé");
        }

        if (emprunt.getStatut() != Emprunt.Statut.ACTIF) {
            throw new IllegalStateException(
                    "Cet emprunt est expiré. Lecture impossible.");
        }

        return new LectureResponse(
                emprunt.getId(),
                emprunt.getOuvrage().getId(),
                emprunt.getOuvrage().getTitre(),
                emprunt.getOuvrage().getUrlFichier()
        );
    }
}