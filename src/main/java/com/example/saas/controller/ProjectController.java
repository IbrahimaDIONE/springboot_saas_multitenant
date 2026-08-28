package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.ProjectService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Adaptateur REST mince : il valide les DTO et délègue les cas d'utilisation. Il ne connaît ni JPA,
 * ni TenantContext, ni les règles métier.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService service;

    // Injection de l'interface et non de ProjectServiceImpl (DIP).
    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProjectResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProjectResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(
            @PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
