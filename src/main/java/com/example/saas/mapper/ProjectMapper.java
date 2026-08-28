package com.example.saas.mapper;

import com.example.saas.domain.Project;
import com.example.saas.dto.ProjectResponse;

import org.springframework.stereotype.Component;

/** SRP : conversion du domaine vers le contrat public, sans logique métier. */
@Component
public class ProjectMapper {
    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(), project.getName(), project.getCreatedAt(), project.getUpdatedAt());
    }
}
