package com.example.saas.repository;

import com.example.saas.domain.Category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByTenantIdOrderByName(String tenant);

    Optional<Category> findByIdAndTenantId(UUID id, String tenant);
}
