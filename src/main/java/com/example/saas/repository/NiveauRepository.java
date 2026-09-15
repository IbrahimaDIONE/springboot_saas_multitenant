package com.example.saas.repository;

import com.example.saas.domain.Niveau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface NiveauRepository extends JpaRepository<Niveau, UUID> {
    List<Niveau> findAllByTenantIdOrderByNom(String tenantId);
    Optional<Niveau> findByIdAndTenantId(UUID id, String tenantId);
}