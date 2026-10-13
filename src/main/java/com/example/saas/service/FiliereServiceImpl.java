package com.example.saas.service;

import com.example.saas.domain.Filiere;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.FiliereMapper;
import com.example.saas.repository.FiliereRepository;
import com.example.saas.storage.FileStorageService;
import com.example.saas.storage.OuvragePdfStorageService;
import com.example.saas.tenant.TenantProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.*;

@Service
@Transactional
public class FiliereServiceImpl implements FiliereService {
    private final FiliereRepository repository;
    private final FiliereMapper mapper;
    private final TenantProvider tenantProvider;
    private final JdbcTemplate jdbcTemplate;
    private final FileStorageService fileStorage;
    private final OuvragePdfStorageService ouvragePdfStorage;

    public FiliereServiceImpl(FiliereRepository repository, FiliereMapper mapper, TenantProvider tenantProvider,
                              JdbcTemplate jdbcTemplate, FileStorageService fileStorage,
                              OuvragePdfStorageService ouvragePdfStorage) {
        this.repository = repository;
        this.mapper = mapper;
        this.tenantProvider = tenantProvider;
        this.jdbcTemplate = jdbcTemplate;
        this.fileStorage = fileStorage;
        this.ouvragePdfStorage = ouvragePdfStorage;
    }
    @Transactional(readOnly = true)
    public List<FiliereResponse> findAll() {
        return repository.findAllByTenantIdOrderByNom(tenantProvider.currentTenant()).stream().map(mapper::toResponse).toList();
    }
    public FiliereResponse create(FiliereRequest request) {
        return mapper.toResponse(repository.save(new Filiere(tenantProvider.currentTenant(), request.nom())));
    }
    public FiliereResponse update(UUID id, FiliereRequest request) {
        Filiere filiere = find(id);
        filiere.renommer(request.nom());
        return mapper.toResponse(filiere);
    }
    public void delete(UUID id) {
        String tenantId = tenantProvider.currentTenant();
        Filiere filiere = find(id);
        List<UUID> ouvrageIds = jdbcTemplate.query(
                "SELECT id FROM ouvrages WHERE tenant_id = ? AND filiere_id = ?",
                (result, row) -> result.getObject("id", UUID.class), tenantId, id);
        List<UUID> memoireIds = jdbcTemplate.query(
                "SELECT id FROM memoires WHERE tenant_id = ? AND filiere_id = ?",
                (result, row) -> result.getObject("id", UUID.class), tenantId, id);
        List<String> filePaths = jdbcTemplate.query(
                "SELECT chemin_stockage FROM fichiers WHERE tenant_id = ? AND memoire_id IN " +
                        "(SELECT id FROM memoires WHERE tenant_id = ? AND filiere_id = ?)",
                (result, row) -> result.getString("chemin_stockage"), tenantId, tenantId, id);

        jdbcTemplate.update(
                "UPDATE etudiants e SET filiere_id = NULL FROM app_users u " +
                        "WHERE e.utilisateur_id = u.id AND u.tenant_id = ? AND e.filiere_id = ?",
                tenantId, id);
        jdbcTemplate.update(
                "DELETE FROM penalites WHERE tenant_id = ? AND emprunt_id IN " +
                        "(SELECT id FROM emprunts WHERE tenant_id = ? AND ouvrage_id IN " +
                        "(SELECT id FROM ouvrages WHERE tenant_id = ? AND filiere_id = ?))",
                tenantId, tenantId, tenantId, id);
        jdbcTemplate.update(
                "DELETE FROM emprunts WHERE tenant_id = ? AND ouvrage_id IN " +
                        "(SELECT id FROM ouvrages WHERE tenant_id = ? AND filiere_id = ?)",
                tenantId, tenantId, id);
        jdbcTemplate.update(
                "DELETE FROM favoris WHERE tenant_id = ? AND type_ressource = 'OUVRAGE' AND ressource_id IN " +
                        "(SELECT id FROM ouvrages WHERE tenant_id = ? AND filiere_id = ?)",
                tenantId, tenantId, id);
        jdbcTemplate.update(
                "DELETE FROM consultations WHERE tenant_id = ? AND type_ressource = 'OUVRAGE' AND ressource_id IN " +
                        "(SELECT id FROM ouvrages WHERE tenant_id = ? AND filiere_id = ?)",
                tenantId, tenantId, id);
        jdbcTemplate.update(
                "DELETE FROM favoris WHERE tenant_id = ? AND type_ressource = 'MEMOIRE' AND ressource_id IN " +
                        "(SELECT id FROM memoires WHERE tenant_id = ? AND filiere_id = ?)",
                tenantId, tenantId, id);
        jdbcTemplate.update(
                "DELETE FROM consultations WHERE tenant_id = ? AND type_ressource = 'MEMOIRE' AND ressource_id IN " +
                        "(SELECT id FROM memoires WHERE tenant_id = ? AND filiere_id = ?)",
                tenantId, tenantId, id);
        jdbcTemplate.update(
                "DELETE FROM fichiers WHERE tenant_id = ? AND memoire_id IN " +
                        "(SELECT id FROM memoires WHERE tenant_id = ? AND filiere_id = ?)",
                tenantId, tenantId, id);
        jdbcTemplate.update("DELETE FROM memoires WHERE tenant_id = ? AND filiere_id = ?", tenantId, id);
        jdbcTemplate.update("DELETE FROM ouvrages WHERE tenant_id = ? AND filiere_id = ?", tenantId, id);
        repository.delete(filiere);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                filePaths.forEach(fileStorage::delete);
                ouvrageIds.forEach(ouvrageId -> ouvragePdfStorage.delete(tenantId, ouvrageId));
            }
        });
    }
    private Filiere find(UUID id) {
        return repository.findByIdAndTenantId(id, tenantProvider.currentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Filière introuvable"));
    }
}