package com.example.saas.domain;

import jakarta.persistence.*;

/**
 * Notification interne envoyée à un étudiant.
 * Toujours isolée par tenant.
 */
@Entity
@Table(name = "notifications")
public class Notification extends BaseTenantEntity {

    public enum Type {
        NOUVELLE_RESSOURCE,
        RAPPEL_ECHEANCE,
        AVERTISSEMENT_RETARD,
        PENALITE
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false, updatable = false)
    private Etudiant etudiant;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Type type;

    @Column(nullable = false)
    private boolean lu = false;

    protected Notification() {}

    public Notification(String tenantId, Etudiant etudiant,
                        String message, Type type) {
        super(tenantId);
        if (etudiant == null || message == null || message.isBlank() || type == null)
            throw new IllegalArgumentException("Notification invalide");
        this.etudiant = etudiant;
        this.message  = message;
        this.type     = type;
        this.lu       = false;
    }

    public void marquerLu() { this.lu = true; }

    public Etudiant getEtudiant() { return etudiant; }
    public String   getMessage()  { return message; }
    public Type     getType()     { return type; }
    public boolean  isLu()       { return lu; }
}