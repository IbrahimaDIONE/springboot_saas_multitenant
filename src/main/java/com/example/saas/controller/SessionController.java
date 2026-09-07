package com.example.saas.controller;

import com.example.saas.dto.SessionResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** Endpoint pédagogique montrant l'identité et le tenant réellement authentifiés. */
@RestController
@RequestMapping("/api/me")
@PreAuthorize("hasAnyRole('ADMIN_PLATEFORME','ADMIN_ETABLISSEMENT','ETUDIANT')")
public class SessionController {
    @GetMapping
    public SessionResponse me(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt principal) {
        return new SessionResponse(
                principal.getSubject(),
                principal.getClaimAsString("tenant_id"),
                principal.getClaimAsStringList("roles"));
    }
}
