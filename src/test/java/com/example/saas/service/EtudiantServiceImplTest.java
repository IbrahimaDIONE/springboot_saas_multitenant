package com.example.saas.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.saas.domain.Etudiant;
import com.example.saas.domain.TenantUser;
import com.example.saas.dto.EtudiantRequest;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.TenantUserRepository;
import com.example.saas.tenant.TenantProvider;

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
        when(etudiants.findAllByUtilisateur_TenantIdAndUtilisateur_RoleOrderByUtilisateur_NomAscUtilisateur_PrenomAsc(
                        "tenant-b", "ETUDIANT"))
                .thenReturn(List.of(etudiant));

        assertThat(service.findAll()).extracting("username").containsExactly("student-b");
        verify(etudiants)
                .findAllByUtilisateur_TenantIdAndUtilisateur_RoleOrderByUtilisateur_NomAscUtilisateur_PrenomAsc(
                        "tenant-b", "ETUDIANT");
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

    @Test
    void shouldDeactivateOnlyStudentFromAuthenticatedTenant() {
        TenantUser student =
                TenantUser.newEtudiant(
                        "tenant-b", "student-b", "{bcrypt}hash", "Diop", "Awa", "awa@example.com");
        student.setEnabled(true);
        Etudiant etudiant = new Etudiant(student);
        when(etudiants.findByIdAndUtilisateur_TenantId(etudiant.getId(), "tenant-b"))
                .thenReturn(Optional.of(etudiant));

        assertThat(service.deactivate(etudiant.getId()).enabled()).isFalse();
        assertThat(student.isEnabled()).isFalse();
        verify(etudiants).findByIdAndUtilisateur_TenantId(etudiant.getId(), "tenant-b");
    }
}
