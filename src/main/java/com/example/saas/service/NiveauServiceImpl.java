package com.example.saas.service;

import com.example.saas.domain.Niveau;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.NiveauMapper;
import com.example.saas.repository.NiveauRepository;
import com.example.saas.tenant.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class NiveauServiceImpl implements NiveauService {
    private final NiveauRepository repository;
    private final NiveauMapper mapper;
    private final TenantProvider tenantProvider;

    public NiveauServiceImpl(NiveauRepository repository, NiveauMapper mapper, TenantProvider tenantProvider) {
        this.repository = repository;
        this.mapper = mapper;
        this.tenantProvider = tenantProvider;
    }
    @Transactional(readOnly = true)
    public List<NiveauResponse> findAll() {
        return repository.findAllByTenantIdOrderByNom(tenantProvider.currentTenant()).stream().map(mapper::toResponse).toList();
    }
    public NiveauResponse create(NiveauRequest request) {
        return mapper.toResponse(repository.save(new Niveau(tenantProvider.currentTenant(), request.nom())));
    }
    public NiveauResponse update(UUID id, NiveauRequest request) {
        Niveau niveau = find(id);
        niveau.renommer(request.nom());
        return mapper.toResponse(niveau);
    }
    public void delete(UUID id) { repository.delete(find(id)); }
    private Niveau find(UUID id) {
        return repository.findByIdAndTenantId(id, tenantProvider.currentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Niveau introuvable"));
    }
}