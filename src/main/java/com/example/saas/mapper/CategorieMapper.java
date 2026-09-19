package com.example.saas.mapper;

import com.example.saas.domain.Categorie;
import com.example.saas.dto.CategorieResponse;
import org.springframework.stereotype.Component;

@Component
public class CategorieMapper {
    public CategorieResponse toResponse(Categorie categorie) {
        return new CategorieResponse(categorie.getId(), categorie.getNom());
    }
}