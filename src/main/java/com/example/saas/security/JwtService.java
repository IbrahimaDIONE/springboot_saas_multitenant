package com.example.saas.security;

import com.example.saas.domain.TenantUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

/** Signe les access tokens avec tenant_id et rôle, deux claims non modifiables par le client. */
@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final Duration ttl;

    public JwtService(JwtEncoder e, @Value("${app.security.jwt.access-token-minutes}") long m) {
        encoder = e;
        ttl = Duration.ofMinutes(m);
    }

    public String create(TenantUser u) {
        Instant n = Instant.now();
        JwtClaimsSet c =
                JwtClaimsSet.builder()
                        .issuer("saas-starter")
                        .subject(u.getUsername())
                        .issuedAt(n)
                        .expiresAt(n.plus(ttl))
                        .id(UUID.randomUUID().toString())
                        .claim("tenant_id", u.getTenantId())
                        .claim("roles", List.of(u.getRole()))
                        .build();
        return encoder.encode(
                        JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), c))
                .getTokenValue();
    }

    public long expiresInSeconds() {
        return ttl.toSeconds();
    }
}
