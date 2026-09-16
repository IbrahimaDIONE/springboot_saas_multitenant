package com.example.saas.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Étudiant rattaché à un utilisateur et à un tenant.
 */
@Entity
@Table(name = "etudiants")
public class Etudiant {

    @Id
    private UUID id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private TenantUser utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "niveau_id")
    private Niveau niveau;

    protected Etudiant() {}

    public UUID getId()              { return id; }
    public TenantUser getUtilisateur(){ return utilisateur; }
    public Filiere getFiliere()      { return filiere; }
    public Niveau getNiveau()        { return niveau; }
}