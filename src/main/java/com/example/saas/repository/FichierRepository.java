package com.example.saas.repository;

import com.example.saas.domain.Fichier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface FichierRepository extends JpaRepository<Fichier, UUID> {
    List<Fichier> findAllByMemoireIdAndTenantId(UUID memoireId, String tenantId);

    Optional<Fichier> findByIdAndTenantId(UUID id, String tenantId);
}