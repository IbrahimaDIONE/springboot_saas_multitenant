package com.example.saas.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "app_users")
public class TenantUser {
    @Id private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "tenant_id", nullable = false, updatable = false, length = 50)
    private String tenantId;

    @Column(name = "role", nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(length = 100)
    private String nom;

    @Column(length = 100)
    private String prenom;

    @Column(length = 150)
    private String email;

    protected TenantUser() {}

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Compte étudiant créé par l'admin d'établissement, inactif jusqu'à activation. */
    public static TenantUser newEtudiant(
            String tenantId,
            String username,
            String passwordHash,
            String nom,
            String prenom,
            String email) {
        return newEtudiant(tenantId, username, passwordHash, nom, prenom, email, "ETUDIANT");
    }

    public static TenantUser newEtudiant(
            String tenantId, String username, String passwordHash, String nom, String prenom,
            String email, String role) {
        TenantUser user = new TenantUser();
        user.id = UUID.randomUUID();
        user.username = username;
        user.passwordHash = passwordHash;
        user.tenantId = tenantId;
        user.role = role;
        user.enabled = false;
        user.nom = nom;
        user.prenom = prenom;
        user.email = email;
        return user;
    }

    public static TenantUser newAdminEtablissement(
            String tenantId,
            String username,
            String passwordHash,
            String nom,
            String prenom,
            String email) {
        return newAdminEtablissement(tenantId, username, passwordHash, nom, prenom, email,
                "ADMIN_ETABLISSEMENT");
    }

    public static TenantUser newAdminEtablissement(
            String tenantId, String username, String passwordHash, String nom, String prenom,
            String email, String role) {
        TenantUser user = new TenantUser();
        user.id = UUID.randomUUID();
        user.username = username;
        user.passwordHash = passwordHash;
        user.tenantId = tenantId;
        user.role = role;
        user.enabled = true;
        user.nom = nom;
        user.prenom = prenom;
        user.email = email;
        return user;
    }
}