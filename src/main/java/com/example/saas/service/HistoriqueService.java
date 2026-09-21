package com.example.saas.service;

import com.example.saas.dto.HistoriqueItemResponse;
import java.util.List;

public interface HistoriqueService {
    List<HistoriqueItemResponse> mesActivites(String typeAction);
}