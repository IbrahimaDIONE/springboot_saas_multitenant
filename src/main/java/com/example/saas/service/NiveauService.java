package com.example.saas.service;

import com.example.saas.dto.*;
import java.util.*;

public interface NiveauService {
    List<NiveauResponse> findAll();
    NiveauResponse create(NiveauRequest request);
    NiveauResponse update(UUID id, NiveauRequest request);
    void delete(UUID id);
}