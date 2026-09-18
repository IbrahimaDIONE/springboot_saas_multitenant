package com.example.saas.repository;

import com.example.saas.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
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

        List<Notification> findAllByTenantIdOrderByCreatedAtDesc(String tenantId);

            boolean existsByTenantIdAndEtudiantIdAndTypeAndCreatedAtAfter(
                    String tenantId, UUID etudiantId, Notification.Type type, Instant after);

    Optional<Notification> findByIdAndTenantId(UUID id, String tenantId);

    long countByTenantIdAndEtudiantIdAndLuFalse(
            String tenantId, UUID etudiantId);
}