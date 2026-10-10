package com.example.saas.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.example.saas.domain.Etudiant;
import com.example.saas.domain.Notification;
import com.example.saas.mapper.NotificationMapper;
import com.example.saas.mapper.ReglePenaliteMapper;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.NotificationRepository;
import com.example.saas.repository.ReglePenaliteRepository;
import com.example.saas.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock NotificationRepository notifications;
    @Mock ReglePenaliteRepository regles;
    @Mock EtudiantRepository etudiants;
    @Mock NotificationMapper notificationMapper;
    @Mock ReglePenaliteMapper regleMapper;

        @AfterEach
        void clearRequestContext() {
                TenantContext.clear();
                SecurityContextHolder.clearContext();
        }

    @Test
    void shouldNotifyActiveStudentsInTheBookFieldAndLevel() {
        String tenantId = "tenant-a";
        UUID filiereId = UUID.randomUUID();
        UUID niveauId = UUID.randomUUID();
        Etudiant etudiant = org.mockito.Mockito.mock(Etudiant.class);
        when(etudiants
                .findAllByUtilisateur_TenantIdAndUtilisateur_RoleAndUtilisateur_EnabledTrueAndFiliereIdAndNiveauIdOrderByUtilisateur_NomAscUtilisateur_PrenomAsc(
                        tenantId, "ETUDIANT", filiereId, niveauId))
                .thenReturn(List.of(etudiant));

        NotificationServiceImpl service = new NotificationServiceImpl(
                notifications, regles, etudiants, notificationMapper, regleMapper);
        service.notifierNouvelleRessource(tenantId, "Algorithmique", filiereId, niveauId);

        verify(etudiants)
                .findAllByUtilisateur_TenantIdAndUtilisateur_RoleAndUtilisateur_EnabledTrueAndFiliereIdAndNiveauIdOrderByUtilisateur_NomAscUtilisateur_PrenomAsc(
                        tenantId, "ETUDIANT", filiereId, niveauId);
        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(notification.capture());
        assertThat(notification.getValue().getType()).isEqualTo(Notification.Type.NOUVELLE_RESSOURCE);
        assertThat(notification.getValue().getMessage()).contains("Algorithmique");
    }

    @Test
    void shouldResolveNotificationOwnerFromJwtUsername() {
        String tenantId = "tenant-a";
        UUID studentId = UUID.randomUUID();
        Etudiant etudiant = org.mockito.Mockito.mock(Etudiant.class);
        when(etudiant.getId()).thenReturn(studentId);
        when(etudiants.findByUsernameEtTenant("client-c", tenantId))
                .thenReturn(Optional.of(etudiant));
        when(notifications.findAllByTenantIdAndEtudiantIdOrderByCreatedAtDesc(tenantId, studentId))
                .thenReturn(List.of());
        TenantContext.set(tenantId);
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("client-c")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(jwt, "test-token", List.of()));
        NotificationServiceImpl service = new NotificationServiceImpl(
                notifications, regles, etudiants, notificationMapper, regleMapper);

        assertThat(service.mesNotifications()).isEmpty();
        verify(etudiants).findByUsernameEtTenant("client-c", tenantId);
    }
}