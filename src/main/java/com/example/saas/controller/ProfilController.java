package com.example.saas.controller;

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
@PreAuthorize("hasAnyRole('ADMIN_PLATEFORME','ADMIN_ETABLISSEMENT','ETUDIANT')")
public class ProfilController {
    private final ProfilService service;

    public ProfilController(ProfilService service) {
        this.service = service;
    }

    @GetMapping
    public ProfilResponse me(@AuthenticationPrincipal Jwt principal) {
        return service.get(principal.getSubject());
    }

    @PutMapping
    public ProfilResponse update(
            @AuthenticationPrincipal Jwt principal, @Valid @RequestBody ProfilRequest body) {
        return service.update(principal.getSubject(), body);
    }
}
