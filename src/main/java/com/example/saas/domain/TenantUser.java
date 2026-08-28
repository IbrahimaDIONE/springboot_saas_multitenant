package com.example.saas.domain;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Utilisateur technique rattaché à un tenant. Le mot de passe stocké est un hash BCrypt, jamais le
 * mot de passe en clair.
 */
@Entity
@Table(name = "app_users")
public class TenantUser {
    @Id private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 50)
    private String tenantId;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    protected TenantUser() {}

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
