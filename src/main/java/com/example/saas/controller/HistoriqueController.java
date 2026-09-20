package com.example.saas.controller;

import com.example.saas.dto.HistoriqueItemResponse;
import com.example.saas.service.HistoriqueService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historique")
@PreAuthorize("hasRole('ETUDIANT')")
public class HistoriqueController {
    private final HistoriqueService service;

    public HistoriqueController(HistoriqueService service) {
        this.service = service;
    }

    @GetMapping
    public List<HistoriqueItemResponse> mesActivites(@RequestParam(required = false) String type) {
        return service.mesActivites(type);
    }
}