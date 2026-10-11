package com.example.saas.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean actif;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Role() {}

    public Role(String code, String description, boolean actif) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.description = description;
        this.actif = actif;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Role system(String code) {
        return new Role(code, null, true);
    }

    public void update(String code, String description, boolean actif) {
        this.code = code;
        this.description = description;
        this.actif = actif;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public boolean isActif() { return actif; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
