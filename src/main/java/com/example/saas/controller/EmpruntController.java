package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.EmpruntService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur REST mince : validation, statuts HTTP
 * et délégation au service.
 */
@RestController
@RequestMapping("/api/emprunts")
public class EmpruntController {

    private final EmpruntService service;

    public EmpruntController(EmpruntService service) {
        this.service = service;
    }

    /** Étudiant : emprunter un ouvrage */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ETUDIANT')")
    public EmpruntResponse emprunter(
            @Valid @RequestBody EmpruntRequest request) {
        return service.emprunter(request);
    }

    /** Étudiant : voir ses emprunts en cours */
    @GetMapping("/mes-emprunts")
    @PreAuthorize("hasRole('ETUDIANT')")
    public List<EmpruntResponse> mesEmprunts() {
        return service.mesEmprunts();
    }

    /** Étudiant : détail d'un emprunt */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ETUDIANT','ADMIN_ETABLISSEMENT')")
    public EmpruntResponse getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    /** Étudiant : lire un ouvrage emprunté en ligne */
    @GetMapping("/{id}/lire")
    @PreAuthorize("hasRole('ETUDIANT')")
    public LectureResponse lireEnLigne(@PathVariable UUID id) {
        return service.lireEnLigne(id);
    }

    @PatchMapping("/{id}/retour")
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public EmpruntResponse retourner(@PathVariable UUID id) {
        return service.retourner(id);
    }

    /** Admin : voir tous les emprunts avec filtre optionnel */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public List<EmpruntResponse> listAll(
            @RequestParam(required = false) String statut) {
        return service.findAllByTenant(statut);
    }
}