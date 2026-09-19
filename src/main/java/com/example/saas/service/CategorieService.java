package com.example.saas.service;

import com.example.saas.dto.CategorieRequest;
import com.example.saas.dto.CategorieResponse;

import java.util.List;
import java.util.UUID;

public interface CategorieService {
    List<CategorieResponse> findAll();
    CategorieResponse create(CategorieRequest request);
    CategorieResponse update(UUID id, CategorieRequest request);
    void delete(UUID id);
}