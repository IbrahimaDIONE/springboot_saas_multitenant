package com.example.saas.service;

import com.example.saas.domain.Filiere;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.FiliereMapper;
import com.example.saas.repository.FiliereRepository;
import com.example.saas.tenant.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class FiliereServiceImpl implements FiliereService {
    private final FiliereRepository repository;
    private final FiliereMapper mapper;
    private final TenantProvider tenantProvider;

    public FiliereServiceImpl(FiliereRepository repository, FiliereMapper mapper, TenantProvider tenantProvider) {
        this.repository = repository;
        this.mapper = mapper;
        this.tenantProvider = tenantProvider;
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
    public void delete(UUID id) { repository.delete(find(id)); }
    private Filiere find(UUID id) {
        return repository.findByIdAndTenantId(id, tenantProvider.currentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Filière introuvable"));
    }
}