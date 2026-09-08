package com.example.saas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.saas.domain.Etudiant;
import com.example.saas.domain.TenantUser;
import com.example.saas.dto.EtudiantRequest;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.TenantUserRepository;
import com.example.saas.tenant.TenantProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class EtudiantServiceImplTest {
    @Mock TenantUserRepository users;
    @Mock EtudiantRepository etudiants;
    @Mock TenantProvider tenantProvider;
    @Mock PasswordEncoder passwordEncoder;

    private EtudiantServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EtudiantServiceImpl(users, etudiants, tenantProvider, passwordEncoder);
        when(tenantProvider.currentTenant()).thenReturn("tenant-b");
    }

    @Test
    void shouldListStudentsUsingAuthenticatedTenant() {
        TenantUser student =
                TenantUser.newEtudiant(
                        "tenant-b", "student-b", "{bcrypt}hash", "Diop", "Awa", "awa@example.com");
        Etudiant etudiant = new Etudiant(student);
        when(etudiants.findAllByUtilisateur_TenantIdOrderByUtilisateur_NomAscUtilisateur_PrenomAsc(
                        "tenant-b"))
                .thenReturn(List.of(etudiant));

        assertThat(service.findAll()).extracting("username").containsExactly("student-b");
        verify(etudiants)
                .findAllByUtilisateur_TenantIdOrderByUtilisateur_NomAscUtilisateur_PrenomAsc(
                        "tenant-b");
        verify(etudiants, never()).findAll();
    }

    @Test
    void shouldCreateInactiveStudentWithTenantAndEncodedPassword() {
                when(passwordEncoder.encode("password")).thenReturn("{bcrypt}encoded");
        when(users.save(any(TenantUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(etudiants.save(any(Etudiant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(
                new EtudiantRequest(
                        "student-b",
                        "password",
                        "Diop",
                        "Awa",
                        "awa@example.com",
                        null,
                        null));

        ArgumentCaptor<TenantUser> userCaptor = ArgumentCaptor.forClass(TenantUser.class);
        verify(users).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getTenantId()).isEqualTo("tenant-b");
        assertThat(userCaptor.getValue().getRole()).isEqualTo("ETUDIANT");
        assertThat(userCaptor.getValue().isEnabled()).isFalse();
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("{bcrypt}encoded");
        verify(passwordEncoder).encode("password");
    }

    @Test
    void shouldActivateOnlyStudentFromAuthenticatedTenant() {
        TenantUser student =
                TenantUser.newEtudiant(
                        "tenant-b", "student-b", "{bcrypt}hash", "Diop", "Awa", "awa@example.com");
        Etudiant etudiant = new Etudiant(student);
        when(etudiants.findByIdAndUtilisateur_TenantId(etudiant.getId(), "tenant-b"))
                .thenReturn(Optional.of(etudiant));

        assertThat(service.activate(etudiant.getId()).enabled()).isTrue();
        assertThat(student.isEnabled()).isTrue();
        verify(etudiants).findByIdAndUtilisateur_TenantId(etudiant.getId(), "tenant-b");
    }
}
