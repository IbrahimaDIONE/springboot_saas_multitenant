package com.example.saas.service;

import com.example.saas.dto.EtudiantRequest;
import com.example.saas.dto.EtudiantResponse;

import java.util.List;
import java.util.UUID;

public interface EtudiantService {
    List<EtudiantResponse> findAll();

    EtudiantResponse create(EtudiantRequest request);

    EtudiantResponse activate(UUID id);
}