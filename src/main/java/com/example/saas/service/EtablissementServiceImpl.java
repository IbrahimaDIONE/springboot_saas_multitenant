package com.example.saas.service;

import com.example.saas.domain.*;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.*;
import com.example.saas.storage.FileStorageService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Service
@Transactional
public class EtablissementServiceImpl implements EtablissementService {
    private static final String ADMIN_ROLE = "ADMIN_ETABLISSEMENT";
    private final EtablissementRepository etablissements;
    private final TenantUserRepository users;
    private final OuvrageRepository ouvrages;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final FileStorageService fileStorage;

    public EtablissementServiceImpl(
            EtablissementRepository etablissements,
            TenantUserRepository users,
            OuvrageRepository ouvrages,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate,
            FileStorageService fileStorage) {
        this.etablissements = etablissements;
        this.users = users;
        this.ouvrages = ouvrages;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.fileStorage = fileStorage;
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
                admin.nom(), admin.prenom(), admin.email(), ADMIN_ROLE));
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
    public void delete(String code) {
        Etablissement etablissement = find(code);
        String tenantId = etablissement.getCode();

        deleteTenantData(tenantId);
        etablissements.delete(etablissement);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                fileStorage.deleteTenantFiles(tenantId);
            }
        });
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

    private void deleteTenantData(String tenantId) {
        jdbcTemplate.update("DELETE FROM notifications WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM penalites WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM favoris WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM consultations WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM emprunts WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM fichiers WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM memoires WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM ouvrages WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM categories WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM filieres WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM niveaux WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM regles_penalite WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM projects WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update("DELETE FROM app_users WHERE tenant_id = ?", tenantId);
    }

    private EtablissementResponse toResponse(Etablissement e) {
        return new EtablissementResponse(e.getId(), e.getCode(), e.getNom(), e.getStatut(), e.getParametres());
    }
}