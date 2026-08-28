package com.example.saas.service;

import com.example.saas.domain.Project;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.ProjectMapper;
import com.example.saas.repository.ProjectRepository;
import com.example.saas.tenant.TenantProvider;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Couche métier et frontière transactionnelle.
 *
 * <p>Sécurité : chaque requête repository reçoit le tenant courant. DIP : TenantProvider masque la
 * provenance du tenant (header aujourd'hui, JWT demain).
 */
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository repository;
    private final ProjectMapper mapper;
    private final TenantProvider tenantProvider;

    public ProjectServiceImpl(
            ProjectRepository repository, ProjectMapper mapper, TenantProvider tenantProvider) {
        this.repository = repository;
        this.mapper = mapper;
        this.tenantProvider = tenantProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return repository.findAllByTenantIdOrderByName(tenant()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse findById(UUID id) {
        return mapper.toResponse(findEntity(id));
    }

    @Override
    public ProjectResponse create(ProjectRequest request) {
        return mapper.toResponse(repository.save(new Project(tenant(), request.name())));
    }

    @Override
    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = findEntity(id);
        project.rename(request.name());
        return mapper.toResponse(project);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(findEntity(id));
    }

    private Project findEntity(UUID id) {
        return repository
                .findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new ResourceNotFoundException("Projet " + id + " introuvable"));
    }

    private String tenant() {
        return tenantProvider.currentTenant();
    }
}
