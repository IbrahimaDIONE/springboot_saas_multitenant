package com.example.saas.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ouvrages")
public class Ouvrage extends BaseTenantEntity {
    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, length = 150)
    private String auteur;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resume;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "niveau_id", nullable = false)
    private Niveau niveau;

    protected Ouvrage() {}

    public Ouvrage(String tenantId, String titre, String auteur, String resume, Filiere filiere, Niveau niveau) {
        super(tenantId);
        update(titre, auteur, resume, filiere, niveau);
    }

    public void update(String titre, String auteur, String resume, Filiere filiere, Niveau niveau) {
        if (titre == null || titre.isBlank()
                || auteur == null || auteur.isBlank()
                || resume == null
                || filiere == null
                || niveau == null) {
            throw new IllegalArgumentException("Ouvrage invalide");
        }
        this.titre = titre.trim();
        this.auteur = auteur.trim();
        this.resume = resume.trim();
        this.filiere = filiere;
        this.niveau = niveau;
    }

    public String getTitre() { return titre; }
    public String getAuteur() { return auteur; }
    public String getResume() { return resume; }
    public Filiere getFiliere() { return filiere; }
    public Niveau getNiveau() { return niveau; }
}