package com.example.saas.domain;

import jakarta.persistence.*;

/**
 * Règle de pénalité définie par un établissement.
 * Toujours isolée par tenant.
 */
@Entity
@Table(name = "regles_penalite")
public class ReglePenalite extends BaseTenantEntity {

    public enum TypeConsequence {
        AVERTISSEMENT,
        SUSPENSION_TEMPORAIRE
    }

    @Column(name = "jours_tolerance", nullable = false)
    private int joursTolérance = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_consequence", nullable = false, length = 50)
    private TypeConsequence typeConsequence;

    @Column(columnDefinition = "TEXT")
    private String description;

    protected ReglePenalite() {}

    public ReglePenalite(String tenantId, int joursTolérance,
                         TypeConsequence typeConsequence, String description) {
        super(tenantId);
        update(joursTolérance, typeConsequence, description);
    }

    public void update(int joursTolérance, TypeConsequence typeConsequence, String description) {
        if (joursTolérance < 0 || typeConsequence == null)
            throw new IllegalArgumentException("Règle de pénalité invalide");
        this.joursTolérance = joursTolérance;
        this.typeConsequence = typeConsequence;
        this.description = description;
    }

    public int              getJoursTolérance()  { return joursTolérance; }
    public TypeConsequence  getTypeConsequence() { return typeConsequence; }
    public String           getDescription()     { return description; }
}