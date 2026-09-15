package com.example.saas.service;

import com.example.saas.domain.*;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class EtablissementServiceImpl implements EtablissementService {
    private static final String ADMIN_ROLE = "ADMIN_ETABLISSEMENT";
    private final EtablissementRepository etablissements;
    private final TenantUserRepository users;
    private final OuvrageRepository ouvrages;
    private final PasswordEncoder passwordEncoder;

    public EtablissementServiceImpl(
            EtablissementRepository etablissements,
            TenantUserRepository users,
            OuvrageRepository ouvrages,
            PasswordEncoder passwordEncoder) {
        this.etablissements = etablissements;
        this.users = users;
        this.ouvrages = ouvrages;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EtablissementResponse create(EtablissementRequest request) {
        if (etablissements.existsByCode(request.code())) {
            throw new IllegalArgumentException("Le code établissement existe déjà");
        }
        if (request.administrateur() == null) {
            throw new IllegalArgumentException("Le premier administrateur est obligatoire");
        }
        Etablissement saved = etablissements.save(new Etablissement(
                request.code(), request.nom(), request.statut(), request.parametres()));
        AdminEtablissementRequest admin = request.administrateur();
        users.save(TenantUser.newAdminEtablissement(
                saved.getCode(), admin.username(), passwordEncoder.encode(admin.password()),
                admin.nom(), admin.prenom(), admin.email()));
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtablissementResponse> findAll() {
        return etablissements.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public EtablissementResponse update(String code, EtablissementRequest request) {
        Etablissement etablissement = find(code);
        etablissement.update(request.nom(), request.statut(), request.parametres());
        return toResponse(etablissement);
    }

    @Override
    public EtablissementResponse setStatus(String code, String statut) {
        if (!Set.of("ACTIF", "INACTIF").contains(statut)) {
            throw new IllegalArgumentException("Statut établissement invalide");
        }
        Etablissement etablissement = find(code);
        etablissement.setStatut(statut);
        return toResponse(etablissement);
    }

    @Override
    @Transactional(readOnly = true)
    public PlateformeStatsResponse stats() {
        long total = etablissements.count();
        long actifs = etablissements.findAll().stream().filter(e -> "ACTIF".equals(e.getStatut())).count();
        long utilisateurs = users.count();
        long ressources = ouvrages.count();
        return new PlateformeStatsResponse(total, actifs, utilisateurs, ressources);
    }

    private Etablissement find(String code) {
        return etablissements.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Etablissement introuvable"));
    }

    private EtablissementResponse toResponse(Etablissement e) {
        return new EtablissementResponse(e.getId(), e.getCode(), e.getNom(), e.getStatut(), e.getParametres());
    }
}