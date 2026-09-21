package com.example.saas.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "favoris")
public class Favori extends BaseTenantEntity {
    public enum TypeRessource { OUVRAGE, MEMOIRE }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false, updatable = false)
    private Etudiant etudiant;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_ressource", nullable = false, length = 20, updatable = false)
    private TypeRessource typeRessource;

    @Column(name = "ressource_id", nullable = false, updatable = false)
    private UUID ressourceId;

    protected Favori() {}

    public Favori(String tenantId, Etudiant etudiant, TypeRessource typeRessource, UUID ressourceId) {
        super(tenantId);
        if (etudiant == null || typeRessource == null || ressourceId == null)
            throw new IllegalArgumentException("Favori invalide");
        this.etudiant = etudiant;
        this.typeRessource = typeRessource;
        this.ressourceId = ressourceId;
    }

    public Etudiant getEtudiant() { return etudiant; }
    public TypeRessource getTypeRessource() { return typeRessource; }
    public UUID getRessourceId() { return ressourceId; }
}