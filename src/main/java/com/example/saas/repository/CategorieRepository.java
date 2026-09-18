package com.example.saas.repository;

import com.example.saas.domain.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface CategorieRepository extends JpaRepository<Categorie, UUID> {
    List<Categorie> findAllByTenantIdOrderByNom(String tenantId);
    Optional<Categorie> findByIdAndTenantId(UUID id, String tenantId);
}