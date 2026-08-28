package com.example.saas.service;

import com.example.saas.domain.*;
import com.example.saas.dto.*;
import com.example.saas.exception.InvalidTokenException;
import com.example.saas.repository.*;
import com.example.saas.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

/** Login et rotation des refresh tokens. Le tenant vient toujours du TenantUser chargé en base. */
@Service
@Transactional
public class AuthService {
    private final AuthenticationManager manager;
    private final TenantUserRepository users;
    private final RefreshTokenRepository tokens;
    private final JwtService jwt;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();

    public AuthService(
            AuthenticationManager m,
            TenantUserRepository u,
            RefreshTokenRepository t,
            JwtService j,
            @Value("${app.security.jwt.refresh-token-days}") long d) {
        manager = m;
        users = u;
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
}
