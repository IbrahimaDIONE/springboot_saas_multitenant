package com.example.saas.repository;

import com.example.saas.domain.Emprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/**
 * RÈGLE : toujours filtrer par tenantId — jamais findById seul.
 */
public interface EmpruntRepository extends JpaRepository<Emprunt, UUID> {

    List<Emprunt> findAllByTenantIdOrderByDateEmpruntDesc(
            String tenantId);

    List<Emprunt> findAllByTenantIdAndEtudiantId(
            String tenantId, UUID etudiantId);

    List<Emprunt> findAllByTenantIdAndStatutOrderByDateEmpruntDesc(
            String tenantId, Emprunt.Statut statut);

    Optional<Emprunt> findByIdAndTenantId(UUID id, String tenantId);

    boolean existsByTenantIdAndEtudiantIdAndOuvrageIdAndStatut(
            String tenantId, UUID etudiantId,
            UUID ouvrageId, Emprunt.Statut statut);

        long countByTenantIdAndStatut(String tenantId, Emprunt.Statut statut);
}