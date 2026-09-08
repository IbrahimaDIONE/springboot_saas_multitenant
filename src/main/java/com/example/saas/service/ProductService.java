package com.example.saas.service;

import com.example.saas.dto.*;

import java.util.*;

/** Contrat des cas d'utilisation Produit ; le contrôleur dépend de cette abstraction. */
public interface ProductService {
    List<ProductResponse> findAll();

    List<ProductResponse> findAll(String search);

    ProductResponse findById(UUID id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(UUID id, ProductRequest request);

    void delete(UUID id);
}
