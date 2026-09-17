package com.example.saas.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "memoires")
public class Memoire extends BaseTenantEntity {
    @Column(nullable = false, length = 255)
    private String titre;

    @Column(nullable = false, length = 150)
    private String auteur;

    @Column(nullable = false, length = 150)
    private String encadreur;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resume;

    @Column(nullable = false)
    private int annee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id", nullable = false)
    private Filiere filiere;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "niveau_id", nullable = false)
    private Niveau niveau;

    @Column(nullable = false)
    private boolean actif = true;

    protected Memoire() {}

    public Memoire(String tenantId, String titre, String auteur, String encadreur, String resume,
                   int annee, Filiere filiere, Niveau niveau) {
        super(tenantId);
        update(titre, auteur, encadreur, resume, annee, filiere, niveau);
    }

    public void update(String titre, String auteur, String encadreur, String resume,
                       int annee, Filiere filiere, Niveau niveau) {
        if (titre == null || titre.isBlank()
                || auteur == null || auteur.isBlank()
                || encadreur == null || encadreur.isBlank()
                || resume == null || resume.isBlank()
                || filiere == null
                || niveau == null
                || annee < 2000) {
            throw new IllegalArgumentException("Mémoire invalide");
        }
        this.titre = titre.trim();
        this.auteur = auteur.trim();
        this.encadreur = encadreur.trim();
        this.resume = resume.trim();
        this.annee = annee;
        this.filiere = filiere;
        this.niveau = niveau;
    }

    public void archiver() { this.actif = false; }
    public void reactiver() { this.actif = true; }

    public String getTitre() { return titre; }
    public String getAuteur() { return auteur; }
    public String getEncadreur() { return encadreur; }
    public String getResume() { return resume; }
    public int getAnnee() { return annee; }
    public Filiere getFiliere() { return filiere; }
    public Niveau getNiveau() { return niveau; }
    public boolean isActif() { return actif; }
}