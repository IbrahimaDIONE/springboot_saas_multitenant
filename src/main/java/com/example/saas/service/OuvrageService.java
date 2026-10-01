package com.example.saas.service;

import com.example.saas.dto.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

public interface OuvrageService {
    List<OuvrageResponse> findAll();
    List<OuvrageResponse> findAll(String search, UUID filiereId, UUID niveauId, UUID categorieId);
    void televerserPdf(UUID ouvrageId, MultipartFile file);
    List<OuvrageResponse> findArchives();
    OuvrageResponse findById(UUID id);
    OuvrageResponse create(OuvrageRequest request);
    OuvrageResponse update(UUID id, OuvrageRequest request);
    OuvrageResponse archiver(UUID id);
    OuvrageResponse reactiver(UUID id);
    void delete(UUID id);
}