package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.OuvrageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/ouvrages")
@PreAuthorize("hasAnyRole('ADMIN_PLATEFORME','ADMIN_ETABLISSEMENT','ETUDIANT')")
public class OuvrageController {
    private final OuvrageService service;
    public OuvrageController(OuvrageService service) { this.service = service; }

    @GetMapping
    public List<OuvrageResponse> list(@RequestParam(required = false) String search,
                                      @RequestParam(required = false) UUID filiereId, @RequestParam(required = false) UUID niveauId) {
        return service.findAll(search, filiereId, niveauId);
    }

    @GetMapping("/archives") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public List<OuvrageResponse> archives() { return service.findArchives(); }

    @GetMapping("/{id}") public OuvrageResponse get(@PathVariable UUID id) { return service.findById(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public OuvrageResponse create(@Valid @RequestBody OuvrageRequest request) { return service.create(request); }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public OuvrageResponse update(@PathVariable UUID id, @Valid @RequestBody OuvrageRequest request) { return service.update(id, request); }

    @PatchMapping("/{id}/archiver") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public OuvrageResponse archiver(@PathVariable UUID id) { return service.archiver(id); }

    @PatchMapping("/{id}/reactiver") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public OuvrageResponse reactiver(@PathVariable UUID id) { return service.reactiver(id); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public void delete(@PathVariable UUID id) { service.delete(id); }
}