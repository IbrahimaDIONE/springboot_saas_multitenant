package com.example.saas.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "filieres")
public class Filiere extends BaseTenantEntity {

    @Column(nullable = false, length = 100)
    private String nom;

    protected Filiere() {}

    public String getNom() { return nom; }
}