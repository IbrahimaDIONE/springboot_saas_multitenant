package com.example.saas.service;

import com.example.saas.dto.EtudiantRequest;
import com.example.saas.dto.EtudiantResponse;
import com.example.saas.dto.ImportEtudiantsResponse;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface EtudiantService {
    List<EtudiantResponse> findAll();

    EtudiantResponse create(EtudiantRequest request);

    EtudiantResponse activate(UUID id);

    ImportEtudiantsResponse importExcel(MultipartFile file);
}