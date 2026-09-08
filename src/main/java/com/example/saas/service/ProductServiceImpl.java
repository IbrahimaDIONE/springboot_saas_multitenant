package com.example.saas.service;

import com.example.saas.domain.Product;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.ProductMapper;
import com.example.saas.repository.ProductRepository;
import com.example.saas.tenant.TenantProvider;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Implémentation multi-tenant du CRUD. Chaque lecture, mise à jour et suppression combine l'id avec
 * le tenant authentifié.
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final TenantProvider tenantProvider;
    private final com.example.saas.repository.CategoryRepository categories;

    public ProductServiceImpl(
            ProductRepository repository,
            ProductMapper mapper,
            TenantProvider tenantProvider,
            com.example.saas.repository.CategoryRepository categories) {
        this.repository = repository;
        this.mapper = mapper;
        this.tenantProvider = tenantProvider;
        this.categories = categories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return repository.findAllByTenantIdOrderByName(tenant()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(String search) {
        if (search == null || search.isBlank()) {
            return findAll();
        }
        return findAll(search, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(String search, UUID categoryId) {
        if (search == null || search.isBlank()) {
            return findAll();
        }
        return repository.searchByTenantId(tenant(), search.trim(), categoryId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return mapper.toResponse(findEntity(id));
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        Product product =
                new Product(
                        tenant(),
                        request.name(),
                        request.price(),
                        request.stock(),
                        request.imageUrl(),
                        category(request.categoryId()));
        return mapper.toResponse(repository.save(product));
    }

    @Override
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = findEntity(id);
        product.update(
                request.name(),
                request.price(),
                request.stock(),
                request.imageUrl(),
                category(request.categoryId()));
        return mapper.toResponse(product);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(findEntity(id));
    }

    private Product findEntity(UUID id) {
        return repository
                .findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new ResourceNotFoundException("Produit " + id + " introuvable"));
    }

    private String tenant() {
        return tenantProvider.currentTenant();
    }

    private com.example.saas.domain.Category category(java.util.UUID id) {
        return categories
                .findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
    }
}
