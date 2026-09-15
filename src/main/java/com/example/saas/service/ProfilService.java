package com.example.saas.service;

import com.example.saas.dto.ProfilRequest;
import com.example.saas.dto.ProfilResponse;

public interface ProfilService {
    ProfilResponse get(String username);

    ProfilResponse update(String username, ProfilRequest request);
}
