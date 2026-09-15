package com.example.saas.service;

import com.example.saas.domain.*;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.OuvrageMapper;
import com.example.saas.repository.*;
import com.example.saas.tenant.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class OuvrageServiceImpl implements OuvrageService {
    private final OuvrageRepository repository;
    private final OuvrageMapper mapper;
    private final FiliereRepository filieres;
    private final NiveauRepository niveaux;
    private final TenantProvider tenantProvider;

    public OuvrageServiceImpl(OuvrageRepository repository, OuvrageMapper mapper, FiliereRepository filieres,
            NiveauRepository niveaux, TenantProvider tenantProvider) {
        this.repository = repository;
        this.mapper = mapper;
        this.filieres = filieres;
        this.niveaux = niveaux;
        this.tenantProvider = tenantProvider;
    }
    @Transactional(readOnly = true)
    public List<OuvrageResponse> findAll() {
        return repository.findAllByTenantIdOrderByTitre(tenant()).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<OuvrageResponse> findAll(String search, UUID filiereId, UUID niveauId) {
        String term = search == null ? "" : search.trim();
        if (term.isEmpty() && filiereId == null && niveauId == null) return findAll();
        return repository.searchByTenantId(tenant(), term, filiereId, niveauId).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public OuvrageResponse findById(UUID id) { return mapper.toResponse(findEntity(id)); }
    public OuvrageResponse create(OuvrageRequest request) {
        return mapper.toResponse(repository.save(new Ouvrage(tenant(), request.titre(), request.auteur(), request.resume(),
                filiere(request.filiereId()), niveau(request.niveauId()))));
    }
    public OuvrageResponse update(UUID id, OuvrageRequest request) {
        Ouvrage ouvrage = findEntity(id);
        ouvrage.update(request.titre(), request.auteur(), request.resume(), filiere(request.filiereId()), niveau(request.niveauId()));
        return mapper.toResponse(ouvrage);
    }
    public void delete(UUID id) { repository.delete(findEntity(id)); }
    private Ouvrage findEntity(UUID id) {
        return repository.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResourceNotFoundException("Ouvrage introuvable"));
    }
    private Filiere filiere(UUID id) {
        return filieres.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResourceNotFoundException("Filière introuvable"));
    }
    private Niveau niveau(UUID id) {
        return niveaux.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResourceNotFoundException("Niveau introuvable"));
    }
    private String tenant() { return tenantProvider.currentTenant(); }
}