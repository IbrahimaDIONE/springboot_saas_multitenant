package com.example.saas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.saas.domain.Project;
import com.example.saas.mapper.ProjectMapper;
import com.example.saas.repository.ProjectRepository;
import com.example.saas.tenant.TenantProvider;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * Ce test prouve l'isolation : le tenant fourni au repository est celui du contexte, jamais une
 * valeur envoyée dans le DTO.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {
    @Mock ProjectRepository repository;
    @Mock TenantProvider tenantProvider;
    private ProjectServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProjectServiceImpl(repository, new ProjectMapper(), tenantProvider);
    }

    @Test
    void shouldFilterProjectsWithCurrentTenant() {
        when(tenantProvider.currentTenant()).thenReturn("acme");
        when(repository.findAllByTenantIdOrderByName("acme"))
                .thenReturn(List.of(new Project("acme", "Portail")));

        var projects = service.findAll();

        assertThat(projects).hasSize(1);
        verify(repository).findAllByTenantIdOrderByName("acme");
        verify(repository, never()).findAll();
    }
}
