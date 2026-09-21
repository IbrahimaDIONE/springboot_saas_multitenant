package com.example.saas.service;

import com.example.saas.dto.*;

import java.util.*;

public interface FavoriService {
    List<FavoriResponse> mesFavoris();
    FavoriResponse ajouter(FavoriRequest request);
    void retirer(UUID favoriId);
}