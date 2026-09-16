package com.example.saas.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "niveaux")
public class Niveau extends BaseTenantEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    protected Niveau() {}

    public String getNom() { return nom; }
}