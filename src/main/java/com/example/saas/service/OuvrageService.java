package com.example.saas.service;

import com.example.saas.dto.*;
import java.util.*;

public interface OuvrageService {
    List<OuvrageResponse> findAll();
    List<OuvrageResponse> findAll(String search, UUID filiereId, UUID niveauId);
    OuvrageResponse findById(UUID id);
    OuvrageResponse create(OuvrageRequest request);
    OuvrageResponse update(UUID id, OuvrageRequest request);
    void delete(UUID id);
}