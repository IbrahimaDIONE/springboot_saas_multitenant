package com.example.saas.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.saas.domain.RefreshToken;
import com.example.saas.domain.TenantUser;
import com.example.saas.dto.AuthResponse;
import com.example.saas.dto.LoginRequest;
import com.example.saas.dto.RefreshRequest;
import com.example.saas.exception.InvalidTokenException;
import com.example.saas.repository.EtablissementRepository;
import com.example.saas.repository.RefreshTokenRepository;
import com.example.saas.repository.TenantUserRepository;
import com.example.saas.security.JwtService;

/** Login et rotation des refresh tokens. Le tenant vient toujours du TenantUser chargé en base. */
@Service
@Transactional
public class AuthService {
    private final AuthenticationManager manager;
    private final TenantUserRepository users;
    private final EtablissementRepository etablissements;
    private final RefreshTokenRepository tokens;
    private final JwtService jwt;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();

    public AuthService(
            AuthenticationManager m,
            TenantUserRepository u,
            EtablissementRepository e,
            RefreshTokenRepository t,
            JwtService j,
            @Value("${app.security.jwt.refresh-token-days}") long d) {
        manager = m;
        users = u;
        etablissements = e;
        tokens = t;
        jwt = j;
        ttl = Duration.ofDays(d);
    }

    public AuthResponse login(LoginRequest r) {
        manager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(r.username(), r.password()));
        return issue(users.findByUsernameIgnoreCase(r.username()).orElseThrow());
    }

    public AuthResponse refresh(RefreshRequest r) {
        RefreshToken old =
                tokens.findByTokenHash(hash(r.refreshToken()))
                        .filter(RefreshToken::isUsable)
                        .orElseThrow(
                                () ->
                                        new InvalidTokenException(
                                                "Refresh token invalide ou expiré"));
        if (!old.getUser().isEnabled()) {
            throw new InvalidTokenException("Compte désactivé");
        }
        if (!isTenantActive(old.getUser())) {
            throw new InvalidTokenException("Etablissement désactivé");
        }
        old.revoke();
        return issue(old.getUser());
    }

    public void logout(RefreshRequest r) {
        tokens.findByTokenHash(hash(r.refreshToken())).ifPresent(RefreshToken::revoke);
    }

    private AuthResponse issue(TenantUser u) {
        byte[] b = new byte[48];
        random.nextBytes(b);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
        tokens.save(new RefreshToken(hash(raw), u, Instant.now().plus(ttl)));
        return new AuthResponse("Bearer", jwt.create(u), jwt.expiresInSeconds(), raw);
    }

    private String hash(String v) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(v.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isTenantActive(TenantUser user) {
        if ("ADMIN_PLATEFORME".equalsIgnoreCase(user.getRole())
                || "tenant-platform".equalsIgnoreCase(user.getTenantId())) {
            return true;
        }

        return etablissements.findByCode(institutionCode(user.getTenantId()))
                .map(etablissement -> "ACTIF".equals(etablissement.getStatut()))
                .orElse(false);
    }

    private String institutionCode(String tenantId) {
        String prefix = "tenant-";
        String code = tenantId.regionMatches(true, 0, prefix, 0, prefix.length())
                ? tenantId.substring(prefix.length())
                : tenantId;
        return code.toUpperCase(java.util.Locale.ROOT);
    }
}
