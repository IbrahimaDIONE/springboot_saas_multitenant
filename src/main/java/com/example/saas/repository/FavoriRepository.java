package com.example.saas.repository;

import com.example.saas.domain.Favori;
import com.example.saas.domain.Favori.TypeRessource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface FavoriRepository extends JpaRepository<Favori, UUID> {
    List<Favori> findAllByTenantIdAndEtudiantIdOrderByCreatedAtDesc(String tenantId, UUID etudiantId);

    Optional<Favori> findByTenantIdAndEtudiantIdAndTypeRessourceAndRessourceId(
            String tenantId, UUID etudiantId, TypeRessource typeRessource, UUID ressourceId);

    boolean existsByTenantIdAndEtudiantIdAndTypeRessourceAndRessourceId(
            String tenantId, UUID etudiantId, TypeRessource typeRessource, UUID ressourceId);

    Optional<Favori> findByIdAndTenantIdAndEtudiantId(UUID id, String tenantId, UUID etudiantId);
}