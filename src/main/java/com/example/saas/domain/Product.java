package com.example.saas.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Produit multi-tenant avec image URL et catégorie du même tenant. */
@Entity
@Table(name = "products")
public class Product extends BaseTenantEntity {
    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    protected Product() {}

    public Product(String t, String n, BigDecimal p, int s, String i, Category c) {
        super(t);
        update(n, p, s, i, c);
    }

    public void update(String n, BigDecimal p, int s, String i, Category c) {
        if (n == null
                || n.isBlank()
                || p == null
                || p.signum() < 0
                || s < 0
                || i == null
                || i.isBlank()
                || c == null) throw new IllegalArgumentException("Produit invalide");
        name = n.trim();
        price = p;
        stock = s;
        imageUrl = i.trim();
        category = c;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Category getCategory() {
        return category;
    }
}
