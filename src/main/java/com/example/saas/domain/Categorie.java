package com.example.saas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Categorie extends BaseTenantEntity {
    @Column(nullable = false, length = 100)
    private String nom;

    protected Categorie() {}

    public Categorie(String tenantId, String nom) {
        super(tenantId);
        renommer(nom);
    }

    public void renommer(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Nom obligatoire");
        nom = value.trim();
    }

    public String getNom() {
        return nom;
    }
}