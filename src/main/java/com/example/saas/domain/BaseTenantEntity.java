package com.example.saas.domain;

import jakarta.persistence.*;
import jakarta.persistence.Id;

import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base de toute donnée appartenant à un tenant.
 *
 * <p>Règle de sécurité : une entité métier multi-tenant doit porter tenantId. DRY : identifiant,
 * tenant et audit sont définis une seule fois.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTenantEntity {
    @Id private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BaseTenantEntity() {}

    protected BaseTenantEntity(String tenantId) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
