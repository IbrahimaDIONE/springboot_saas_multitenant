package com.example.saas.mapper;

import com.example.saas.domain.Niveau;
import com.example.saas.dto.NiveauResponse;
import org.springframework.stereotype.Component;

@Component
public class NiveauMapper {
    public NiveauResponse toResponse(Niveau niveau) {
        return new NiveauResponse(niveau.getId(), niveau.getNom());
    }
}