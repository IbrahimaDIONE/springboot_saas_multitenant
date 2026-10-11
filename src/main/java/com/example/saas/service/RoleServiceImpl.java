package com.example.saas.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.saas.domain.Role;
import com.example.saas.dto.RoleRequest;
import com.example.saas.dto.RoleResponse;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.RoleRepository;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roles;

    public RoleServiceImpl(RoleRepository roles) {
        this.roles = roles;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return findAll(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> findAll(Boolean actif) {
        return roles.findAll().stream()
                .filter(role -> actif == null || role.isActif() == actif)
                .map(this::toResponse)
                .sorted(Comparator.comparing(RoleResponse::code))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse findById(UUID id) {
        return toResponse(find(id));
    }

    @Override
    public RoleResponse create(RoleRequest request) {
        String code = normalize(request.code());
        if (roles.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Le rôle existe déjà");
        }
        return toResponse(roles.save(new Role(code, request.description(), request.actif())));
    }

    @Override
    public RoleResponse update(UUID id, RoleRequest request) {
        Role role = find(id);
        String code = normalize(request.code());
        if (!role.getCode().equalsIgnoreCase(code) && roles.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Le rôle existe déjà");
        }
        role.update(code, request.description(), request.actif());
        return toResponse(role);
    }

    @Override
    public void delete(UUID id) {
        roles.delete(find(id));
    }

    private Role find(UUID id) {
        return roles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable"));
    }

    private String normalize(String code) {
        return code.trim().toUpperCase().replace('-', '_').replace(' ', '_');
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getId(), role.getCode(), role.getDescription(), role.isActif(),
                role.getCreatedAt(), role.getUpdatedAt());
    }
}
