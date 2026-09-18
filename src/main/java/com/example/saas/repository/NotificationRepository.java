package com.example.saas.repository;

import com.example.saas.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/**
 * RÈGLE : toujours filtrer par tenantId.
 */
public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByTenantIdAndEtudiantIdOrderByCreatedAtDesc(
            String tenantId, UUID etudiantId);

    List<Notification> findAllByTenantIdAndEtudiantIdAndLuFalseOrderByCreatedAtDesc(
            String tenantId, UUID etudiantId);

    Optional<Notification> findByIdAndTenantId(UUID id, String tenantId);

    long countByTenantIdAndEtudiantIdAndLuFalse(
            String tenantId, UUID etudiantId);
}