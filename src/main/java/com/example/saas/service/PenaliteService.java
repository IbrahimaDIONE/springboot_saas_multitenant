package com.example.saas.service;

import com.example.saas.dto.PenaliteResponse;
import java.util.List;
import java.util.UUID;

public interface PenaliteService {
    List<PenaliteResponse> mesPenalites();
    List<PenaliteResponse> findAllByTenant();
    void traiterRetards();
}
