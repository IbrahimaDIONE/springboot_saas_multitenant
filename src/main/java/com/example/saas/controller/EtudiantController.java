package com.example.saas.controller;

import com.example.saas.dto.EtudiantRequest;
import com.example.saas.dto.EtudiantResponse;
import com.example.saas.service.EtudiantService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/etudiants")
@PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
public class EtudiantController {
    private final EtudiantService service;

    public EtudiantController(EtudiantService service) {
        this.service = service;
    }

    @GetMapping
    public List<EtudiantResponse> list() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EtudiantResponse create(@Valid @RequestBody EtudiantRequest request) {
        return service.create(request);
    }

    @PostMapping("/{id}/activation")
    public EtudiantResponse activate(@PathVariable UUID id) {
        return service.activate(id);
    }
}