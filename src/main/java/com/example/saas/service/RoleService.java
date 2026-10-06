package com.example.saas.service;

import java.util.List;
import java.util.UUID;

import com.example.saas.dto.RoleRequest;
import com.example.saas.dto.RoleResponse;

public interface RoleService {
    List<RoleResponse> findAll();
    List<RoleResponse> findAll(Boolean actif);
    RoleResponse findById(UUID id);
    RoleResponse create(RoleRequest request);
    RoleResponse update(UUID id, RoleRequest request);
    void delete(UUID id);
}
