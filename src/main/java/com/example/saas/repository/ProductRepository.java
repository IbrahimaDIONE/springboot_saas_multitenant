package com.example.saas.repository;

import com.example.saas.domain.Product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

/**
 * Les méthodes destinées au métier exigent tenantId. N'utilisez jamais findById(id) dans
 * ProductService : il ignorerait l'isolation.
 */
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findAllByTenantIdOrderByName(String tenantId);

    Optional<Product> findByIdAndTenantId(UUID id, String tenantId);
}
