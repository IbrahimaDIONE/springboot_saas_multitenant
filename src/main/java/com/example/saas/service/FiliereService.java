package com.example.saas.service;

import com.example.saas.dto.*;
import java.util.*;

public interface FiliereService {
    List<FiliereResponse> findAll();
    FiliereResponse create(FiliereRequest request);
    FiliereResponse update(UUID id, FiliereRequest request);
    void delete(UUID id);
}