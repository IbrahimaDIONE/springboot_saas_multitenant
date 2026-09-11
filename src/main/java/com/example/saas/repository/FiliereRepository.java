package com.example.saas.repository;

import com.example.saas.domain.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface FiliereRepository extends JpaRepository<Filiere, UUID> {
    List<Filiere> findAllByTenantIdOrderByNom(String tenantId);
    Optional<Filiere> findByIdAndTenantId(UUID id, String tenantId);
}