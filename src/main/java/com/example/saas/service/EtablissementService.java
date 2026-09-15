package com.example.saas.service;

import com.example.saas.dto.*;
import java.util.List;

public interface EtablissementService {
    EtablissementResponse create(EtablissementRequest request);
    List<EtablissementResponse> findAll();
    EtablissementResponse update(String code, EtablissementRequest request);
    EtablissementResponse setStatus(String code, String statut);
    PlateformeStatsResponse stats();
}