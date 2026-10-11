package com.example.saas.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.saas.dto.RoleRequest;
import com.example.saas.dto.RoleResponse;
import com.example.saas.service.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN_PLATEFORME')")
public class RoleController {
    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoleResponse> list(@RequestParam(required = false) Boolean actif) {
        return service.findAll(actif);
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable UUID id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody RoleRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }
}
