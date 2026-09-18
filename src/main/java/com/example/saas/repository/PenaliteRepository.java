package com.example.saas.repository;

import com.example.saas.domain.Penalite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PenaliteRepository
        extends JpaRepository<Penalite, UUID> {

    List<Penalite> findAllByTenantIdAndEtudiantIdOrderByDateApplicationDesc(
            String tenantId, UUID etudiantId);

    List<Penalite> findAllByTenantIdOrderByDateApplicationDesc(
            String tenantId);

    Optional<Penalite> findByIdAndTenantId(UUID id, String tenantId);

        boolean existsByTenantIdAndEmpruntId(String tenantId, UUID empruntId);
}