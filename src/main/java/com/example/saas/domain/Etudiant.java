package com.example.saas.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "etudiants")
public class Etudiant {
    @Id private UUID id;

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private TenantUser utilisateur;

    @Column(name = "filiere_id")
    private UUID filiereId;

    @Column(name = "niveau_id")
    private UUID niveauId;

    protected Etudiant() {}

    public Etudiant(TenantUser utilisateur) {
        this.id = UUID.randomUUID();
        this.utilisateur = utilisateur;
    }

    public UUID getId() { return id; }
    public TenantUser getUtilisateur() { return utilisateur; }
    public UUID getFiliereId() { return filiereId; }
    public void setFiliereId(UUID filiereId) { this.filiereId = filiereId; }
    public UUID getNiveauId() { return niveauId; }
    public void setNiveauId(UUID niveauId) { this.niveauId = niveauId; }
}