package com.example.saas.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class Project extends BaseTenantEntity {
    @Column(nullable = false)
    private String name;

    protected Project() {}

    public Project(String tenantId, String name) {
        super(tenantId);
        rename(name);
    }

    public String getName() {
        return name;
    }

    /** Invariant métier également protégé en dehors de la validation HTTP. */
    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du projet est obligatoire");
        }
        this.name = name.trim();
    }
}
