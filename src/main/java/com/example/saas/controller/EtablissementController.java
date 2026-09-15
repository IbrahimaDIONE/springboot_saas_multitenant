package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.EtablissementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etablissements")
@PreAuthorize("hasRole('ADMIN_PLATEFORME')")
public class EtablissementController {
    private final EtablissementService service;

    public EtablissementController(EtablissementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EtablissementResponse create(@Valid @RequestBody EtablissementRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<EtablissementResponse> list() {
        return service.findAll();
    }

    @PutMapping("/{code}")
    public EtablissementResponse update(@PathVariable String code, @Valid @RequestBody EtablissementRequest request) {
        return service.update(code, request);
    }

    @PatchMapping("/{code}/statut")
    public EtablissementResponse status(@PathVariable String code, @RequestParam String valeur) {
        return service.setStatus(code, valeur);
    }

    @GetMapping("/statistiques")
    public PlateformeStatsResponse stats() {
        return service.stats();
    }
}