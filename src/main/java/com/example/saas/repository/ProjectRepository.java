package com.example.saas.repository;

import com.example.saas.domain.Project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

/**
 * Toutes les lectures métier incluent tenantId. findById seul ne doit pas être utilisé dans la
 * couche service.
 */
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByTenantIdOrderByName(String tenantId);

    Optional<Project> findByIdAndTenantId(UUID id, String tenantId);
}
