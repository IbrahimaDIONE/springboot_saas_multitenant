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

    @Column(name = "url_fichier", length = 2048)
    private String urlFichier;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "niveau_id")
    private Niveau niveau;

    protected Ouvrage() {}

    public String getTitre()      { return titre; }
    public String getAuteur()     { return auteur; }
    public String getResume()     { return resume; }
    public String getUrlFichier() { return urlFichier; }
    public Filiere getFiliere()   { return filiere; }
    public Niveau getNiveau()     { return niveau; }
}