package com.example.saas.domain;

import jakarta.persistence.*;

/** Catégorie appartenant au tenant, donc impossible à partager accidentellement. */
@Entity
@Table(name = "categories")
public class Category extends BaseTenantEntity {
    @Column(nullable = false, length = 100)
    private String name;

    protected Category() {}

    public Category(String tenant, String name) {
        super(tenant);
        rename(name);
    }

    public void rename(String n) {
        if (n == null || n.isBlank()) throw new IllegalArgumentException("Nom obligatoire");
        name = n.trim();
    }

    public String getName() {
        return name;
    }
}
