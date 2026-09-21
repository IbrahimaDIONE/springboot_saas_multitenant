package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.FavoriService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/favoris")
@PreAuthorize("hasRole('ETUDIANT')")
public class FavoriController {
    private final FavoriService service;

    public FavoriController(FavoriService service) {
        this.service = service;
    }

    @GetMapping
    public List<FavoriResponse> mesFavoris() {
        return service.mesFavoris();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriResponse ajouter(@Valid @RequestBody FavoriRequest request) {
        return service.ajouter(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirer(@PathVariable UUID id) {
        service.retirer(id);
    }
}