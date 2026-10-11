package com.example.saas.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.saas.domain.Role;
import com.example.saas.dto.RoleRequest;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {
    @Mock RoleRepository roles;

    private RoleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RoleServiceImpl(roles);
    }

    @Test
    void shouldListRolesSortedAndFilterByActiveState() {
        Role inactive = new Role("LECTEUR", "Lecture", false);
        Role active = new Role("ADMIN", "Administration", true);
        when(roles.findAll()).thenReturn(List.of(inactive, active));

        assertThat(service.findAll(true)).extracting("code").containsExactly("ADMIN");
        assertThat(service.findAll(false)).extracting("code").containsExactly("LECTEUR");
        assertThat(service.findAll()).extracting("code").containsExactly("ADMIN", "LECTEUR");
    }

    @Test
    void shouldFindRoleById() {
        UUID id = UUID.randomUUID();
        Role role = new Role("ADMIN", "Administration", true);
        when(roles.findById(id)).thenReturn(Optional.of(role));

        assertThat(service.findById(id).code()).isEqualTo("ADMIN");
    }

    @Test
    void shouldRejectUnknownRoleId() {
        UUID id = UUID.randomUUID();
        when(roles.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Rôle introuvable");
    }

    @Test
    void shouldNormalizeCodeAndCreateRole() {
        when(roles.existsByCodeIgnoreCase("LECTEUR_ETUDIANT")).thenReturn(false);
        when(roles.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new RoleRequest(" lecteur-etudiant ", "Lecture", true));

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roles).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getCode()).isEqualTo("LECTEUR_ETUDIANT");
        assertThat(response.code()).isEqualTo("LECTEUR_ETUDIANT");
    }

    @Test
    void shouldRejectDuplicateRoleCode() {
        when(roles.existsByCodeIgnoreCase("ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new RoleRequest("admin", null, true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Le rôle existe déjà");
        verify(roles, never()).save(any(Role.class));
    }

    @Test
    void shouldUpdateRole() {
        UUID id = UUID.randomUUID();
        Role role = new Role("LECTEUR", "Lecture", false);
        when(roles.findById(id)).thenReturn(Optional.of(role));
        when(roles.existsByCodeIgnoreCase("LECTEUR_ETUDIANT")).thenReturn(false);

        var response = service.update(id, new RoleRequest("lecteur-etudiant", "Lecture étudiante", true));

        assertThat(response.code()).isEqualTo("LECTEUR_ETUDIANT");
        assertThat(response.actif()).isTrue();
        assertThat(response.description()).isEqualTo("Lecture étudiante");
        verify(roles).findById(id);
    }

    @Test
    void shouldDeleteExistingRole() {
        UUID id = UUID.randomUUID();
        Role role = new Role("LECTEUR", "Lecture", false);
        when(roles.findById(id)).thenReturn(Optional.of(role));

        service.delete(id);

        verify(roles).delete(role);
    }
}