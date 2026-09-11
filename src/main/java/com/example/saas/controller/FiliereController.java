package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.FiliereService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/filieres")
@PreAuthorize("hasAnyRole('ADMIN_PLATEFORME','ADMIN_ETABLISSEMENT','ETUDIANT')")
public class FiliereController {
    private final FiliereService service;
    public FiliereController(FiliereService service) { this.service = service; }
    @GetMapping public List<FiliereResponse> list() { return service.findAll(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public FiliereResponse create(@Valid @RequestBody FiliereRequest request) { return service.create(request); }
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public FiliereResponse update(@PathVariable UUID id, @Valid @RequestBody FiliereRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public void delete(@PathVariable UUID id) { service.delete(id); }
}