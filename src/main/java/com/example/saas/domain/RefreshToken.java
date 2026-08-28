package com.example.saas.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/** Refresh token opaque et révocable ; seul son hash SHA-256 est conservé. */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private TenantUser user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    protected RefreshToken() {}

    public RefreshToken(String h, TenantUser u, Instant e) {
        tokenHash = h;
        user = u;
        expiresAt = e;
    }

    public TenantUser getUser() {
        return user;
    }

    public boolean isUsable() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }

    public void revoke() {
        revoked = true;
    }
}
