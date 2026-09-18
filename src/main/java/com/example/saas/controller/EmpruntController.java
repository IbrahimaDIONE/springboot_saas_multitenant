package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.EmpruntService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
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
    public EmpruntResponse emprunter(
            @Valid @RequestBody EmpruntRequest request) {
        return service.emprunter(request);
    }

    /** Étudiant : voir ses emprunts en cours */
    @GetMapping("/mes-emprunts")
    public List<EmpruntResponse> mesEmprunts() {
        return service.mesEmprunts();
    }

    /** Étudiant : détail d'un emprunt */
    @GetMapping("/{id}")
    public EmpruntResponse getById(@PathVariable UUID id) {
        return service.findById(id);
    }

    /** Étudiant : lire un ouvrage emprunté en ligne */
    @GetMapping("/{id}/lire")
    public LectureResponse lireEnLigne(@PathVariable UUID id) {
        return service.lireEnLigne(id);
    }

    /** Admin : voir tous les emprunts avec filtre optionnel */
    @GetMapping
    public List<EmpruntResponse> listAll(
            @RequestParam(required = false) String statut) {
        return service.findAllByTenant(statut);
    }
}