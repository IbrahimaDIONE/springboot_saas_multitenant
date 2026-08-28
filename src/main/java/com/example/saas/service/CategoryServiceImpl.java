package com.example.saas.service;

import com.example.saas.domain.Category;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.CategoryMapper;
import com.example.saas.repository.CategoryRepository;
import com.example.saas.tenant.TenantProvider;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper mapper;
    private final TenantProvider tenant;

    public CategoryServiceImpl(CategoryRepository r, CategoryMapper m, TenantProvider t) {
        repository = r;
        mapper = m;
        tenant = t;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return repository.findAllByTenantIdOrderByName(tenant.currentTenant()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public CategoryResponse create(CategoryRequest r) {
        return mapper.toResponse(repository.save(new Category(tenant.currentTenant(), r.name())));
    }

    public CategoryResponse update(UUID id, CategoryRequest r) {
        Category c = find(id);
        c.rename(r.name());
        return mapper.toResponse(c);
    }

    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private Category find(UUID id) {
        return repository
                .findByIdAndTenantId(id, tenant.currentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
    }
}
