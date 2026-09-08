package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.CategoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/categories")
@PreAuthorize("hasAnyRole('ADMIN_PLATEFORME','ADMIN_ETABLISSEMENT','ETUDIANT')")
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService s) {
        service = s;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public CategoryResponse create(@Valid @RequestBody CategoryRequest r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
