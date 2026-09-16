package com.example.saas.controller;

import com.example.saas.domain.TenantUser;
import com.example.saas.dto.ProfilRequest;
import com.example.saas.dto.ProfilResponse;
import com.example.saas.repository.TenantUserRepository;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import com.example.saas.dto.ProfilRequest;
import com.example.saas.dto.ProfilResponse;
import com.example.saas.service.ProfilService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profil")
public class ProfilController {
    private final TenantUserRepository users;

    public ProfilController(TenantUserRepository users) {
        this.users = users;
@PreAuthorize("hasAnyRole('ADMIN_PLATEFORME','ADMIN_ETABLISSEMENT','ETUDIANT')")
public class ProfilController {
    private final ProfilService service;

    public ProfilController(ProfilService service) {
        this.service = service;
    }

    @GetMapping
    public ProfilResponse me(@AuthenticationPrincipal Jwt principal) {
        TenantUser u = users.findByUsernameIgnoreCase(principal.getSubject()).orElseThrow();
        return toResponse(u);
    }

    @PutMapping
    @Transactional
    public ProfilResponse update(
            @AuthenticationPrincipal Jwt principal, @RequestBody ProfilRequest body) {
        TenantUser u = users.findByUsernameIgnoreCase(principal.getSubject()).orElseThrow();
        u.setNom(body.nom());
        u.setPrenom(body.prenom());
        u.setEmail(body.email());
        return toResponse(u);
    }

    private ProfilResponse toResponse(TenantUser u) {
        return new ProfilResponse(
                u.getUsername(), u.getNom(), u.getPrenom(), u.getEmail(), u.getRole(), u.getTenantId());
    }
}
        return service.get(principal.getSubject());
    }

    @PutMapping
    public ProfilResponse update(
            @AuthenticationPrincipal Jwt principal, @Valid @RequestBody ProfilRequest body) {
        return service.update(principal.getSubject(), body);
    }
}
