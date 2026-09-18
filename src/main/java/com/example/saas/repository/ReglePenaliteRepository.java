package com.example.saas.repository;

import com.example.saas.domain.ReglePenalite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ReglePenaliteRepository
        extends JpaRepository<ReglePenalite, UUID> {

    List<ReglePenalite> findAllByTenantIdOrderByCreatedAtDesc(
            String tenantId);

    Optional<ReglePenalite> findByIdAndTenantId(UUID id, String tenantId);
}