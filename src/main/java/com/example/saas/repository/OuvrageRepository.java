package com.example.saas.repository;

import com.example.saas.domain.Ouvrage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/**
 * RÈGLE : toujours filtrer par tenantId.
 */
public interface OuvrageRepository extends JpaRepository<Ouvrage, UUID> {

    List<Ouvrage> findAllByTenantIdOrderByTitre(String tenantId);

    Optional<Ouvrage> findByIdAndTenantId(UUID id, String tenantId);
}