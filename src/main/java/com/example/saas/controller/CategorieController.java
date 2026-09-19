package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.CategorieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/categories")
@PreAuthorize("hasAnyRole('ADMIN_PLATEFORME','ADMIN_ETABLISSEMENT','ETUDIANT')")
public class CategorieController {
    private final CategorieService service;
    public CategorieController(CategorieService service) { this.service = service; }
    @GetMapping public List<CategorieResponse> list() { return service.findAll(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public CategorieResponse create(@Valid @RequestBody CategorieRequest request) { return service.create(request); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public CategorieResponse update(@PathVariable UUID id, @Valid @RequestBody CategorieRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public void delete(@PathVariable UUID id) { service.delete(id); }
}