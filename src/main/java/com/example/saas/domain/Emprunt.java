package com.example.saas.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Emprunt numérique : droit d'accès temporaire d'un étudiant
 * sur un ouvrage, toujours isolé par tenant.
 */
@Entity
@Table(name = "emprunts")
public class Emprunt extends BaseTenantEntity {

    public enum Statut { ACTIF, EXPIRE, RETARDE }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false, updatable = false)
    private Etudiant etudiant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ouvrage_id", nullable = false, updatable = false)
    private Ouvrage ouvrage;

    @Column(name = "date_emprunt", nullable = false, updatable = false)
    private Instant dateEmprunt;

    @Column(name = "date_expiration", nullable = false)
    private Instant dateExpiration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Statut statut;

    protected Emprunt() {}

    public Emprunt(String tenantId, Etudiant etudiant,
                   Ouvrage ouvrage, Instant dateExpiration) {
        super(tenantId);
        if (etudiant == null || ouvrage == null || dateExpiration == null)
            throw new IllegalArgumentException("Emprunt invalide");
        this.etudiant      = etudiant;
        this.ouvrage       = ouvrage;
        this.dateEmprunt   = Instant.now();
        this.dateExpiration = dateExpiration;
        this.statut        = Statut.ACTIF;
    }

    public void expirer()  { this.statut = Statut.EXPIRE; }
    public void retarder() { this.statut = Statut.RETARDE; }

    public Etudiant getEtudiant()        { return etudiant; }
    public Ouvrage  getOuvrage()         { return ouvrage; }
    public Instant  getDateEmprunt()     { return dateEmprunt; }
    public Instant  getDateExpiration()  { return dateExpiration; }
    public Statut   getStatut()          { return statut; }
}