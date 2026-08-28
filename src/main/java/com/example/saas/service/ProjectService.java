package com.example.saas.service;

import com.example.saas.dto.*;

import java.util.*;

/** Contrat des cas d'utilisation ; le contrôleur dépend de cette abstraction (DIP). */
public interface ProjectService {
    List<ProjectResponse> findAll();

    ProjectResponse findById(UUID id);

    ProjectResponse create(ProjectRequest request);

    ProjectResponse update(UUID id, ProjectRequest request);

    void delete(UUID id);
}
