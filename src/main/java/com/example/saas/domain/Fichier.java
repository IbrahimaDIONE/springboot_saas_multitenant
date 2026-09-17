package com.example.saas.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "fichiers")
public class Fichier extends BaseTenantEntity {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "memoire_id", nullable = false)
    private Memoire memoire;

    @Column(name = "nom_original", nullable = false, length = 255)
    private String nomOriginal;

    @Column(name = "chemin_stockage", nullable = false, length = 500)
    private String cheminStockage;

    @Column(name = "taille_octets", nullable = false)
    private long tailleOctets;

    @Column(name = "type_mime", nullable = false, length = 100)
    private String typeMime;

    @Column(nullable = false)
    private boolean disponible = true;

    protected Fichier() {}

    public Fichier(String tenantId, Memoire memoire, String nomOriginal, String cheminStockage,
                   long tailleOctets, String typeMime) {
        super(tenantId);
        if (memoire == null || nomOriginal == null || nomOriginal.isBlank()
                || cheminStockage == null || cheminStockage.isBlank()
                || tailleOctets <= 0
                || typeMime == null || typeMime.isBlank()) {
            throw new IllegalArgumentException("Fichier invalide");
        }
        this.memoire = memoire;
        this.nomOriginal = nomOriginal;
        this.cheminStockage = cheminStockage;
        this.tailleOctets = tailleOctets;
        this.typeMime = typeMime;
    }

    public void rendreIndisponible() { this.disponible = false; }
    public void rendreDisponible() { this.disponible = true; }

    public Memoire getMemoire() { return memoire; }
    public String getNomOriginal() { return nomOriginal; }
    public String getCheminStockage() { return cheminStockage; }
    public long getTailleOctets() { return tailleOctets; }
    public String getTypeMime() { return typeMime; }
    public boolean isDisponible() { return disponible; }
}