package com.example.saas.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "etablissements")
public class Etablissement {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 20)
    private String statut;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, String> parametres = new LinkedHashMap<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Etablissement() {}

    public Etablissement(String code, String nom, String statut, Map<String, String> parametres) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.nom = nom;
        this.statut = statut;
        this.parametres = new LinkedHashMap<>(parametres == null ? Map.of() : parametres);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getNom() { return nom; }
    public String getStatut() { return statut; }
    public Map<String, String> getParametres() { return Map.copyOf(parametres); }

    public void update(String nom, String statut, Map<String, String> parametres) {
        this.nom = nom;
        this.statut = statut;
        this.parametres = new LinkedHashMap<>(parametres == null ? Map.of() : parametres);
        this.updatedAt = Instant.now();
    }

    public void setStatut(String statut) {
        this.statut = statut;
        this.updatedAt = Instant.now();
    }
}