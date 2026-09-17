package com.example.saas.service;

import com.example.saas.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public interface MemoireService {
    List<MemoireResponse> findAll();
    List<MemoireResponse> findAll(String search, UUID filiereId, UUID niveauId, Integer annee);
    List<MemoireResponse> findArchives();
    MemoireResponse findById(UUID id);
    MemoireResponse create(MemoireRequest request);
    MemoireResponse update(UUID id, MemoireRequest request);
    MemoireResponse archiver(UUID id);
    MemoireResponse reactiver(UUID id);
    void delete(UUID id);
    FichierResponse ajouterFichier(UUID memoireId, MultipartFile file);
    void supprimerFichier(UUID memoireId, UUID fichierId);
    FichierResponse toggleDisponibilite(UUID memoireId, UUID fichierId, boolean disponible);
}