package com.example.saas.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Pénalité appliquée à un étudiant suite à un retard.
 * Toujours isolée par tenant.
 */
@Entity
@Table(name = "penalites")
public class Penalite extends BaseTenantEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false, updatable = false)
    private Etudiant etudiant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "emprunt_id", nullable = false, updatable = false)
    private Emprunt emprunt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motif;

    @Column(name = "date_application", nullable = false)
    private Instant dateApplication;

    @Column(nullable = false, length = 50)
    private String consequence;

    protected Penalite() {}

    public Penalite(String tenantId, Etudiant etudiant,
                    Emprunt emprunt, String motif, String consequence) {
        super(tenantId);
        if (etudiant == null || emprunt == null ||
                motif == null || consequence == null)
            throw new IllegalArgumentException("Pénalité invalide");
        this.etudiant        = etudiant;
        this.emprunt         = emprunt;
        this.motif           = motif;
        this.dateApplication = Instant.now();
        this.consequence     = consequence;
    }

    public Etudiant getEtudiant()        { return etudiant; }
    public Emprunt  getEmprunt()         { return emprunt; }
    public String   getMotif()           { return motif; }
    public Instant  getDateApplication() { return dateApplication; }
    public String   getConsequence()     { return consequence; }
}