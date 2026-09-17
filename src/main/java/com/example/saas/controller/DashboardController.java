package com.example.saas.controller;

import com.example.saas.dto.DashboardEtablissementResponse;
import com.example.saas.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/etablissement")
@PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardEtablissementResponse get() {
        return service.getEtablissementDashboard();
    }
}
