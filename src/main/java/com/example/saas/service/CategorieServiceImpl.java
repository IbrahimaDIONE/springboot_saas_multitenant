package com.example.saas.service;

import com.example.saas.domain.Categorie;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.CategorieMapper;
import com.example.saas.repository.CategorieRepository;
import com.example.saas.tenant.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class CategorieServiceImpl implements CategorieService {
    private final CategorieRepository repository;
    private final CategorieMapper mapper;
    private final TenantProvider tenantProvider;

    public CategorieServiceImpl(CategorieRepository repository, CategorieMapper mapper, TenantProvider tenantProvider) {
        this.repository = repository;
        this.mapper = mapper;
        this.tenantProvider = tenantProvider;
    }

    @Transactional(readOnly = true)
    public List<CategorieResponse> findAll() {
        return repository.findAllByTenantIdOrderByNom(tenantProvider.currentTenant()).stream().map(mapper::toResponse).toList();
    }

    public CategorieResponse create(CategorieRequest request) {
        return mapper.toResponse(repository.save(new Categorie(tenantProvider.currentTenant(), request.nom())));
    }

    public CategorieResponse update(UUID id, CategorieRequest request) {
        Categorie categorie = find(id);
        categorie.renommer(request.nom());
        return mapper.toResponse(categorie);
    }

    public void delete(UUID id) { repository.delete(find(id)); }

    private Categorie find(UUID id) {
        return repository.findByIdAndTenantId(id, tenantProvider.currentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
    }
}