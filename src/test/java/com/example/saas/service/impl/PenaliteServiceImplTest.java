package com.example.saas.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.example.saas.domain.Emprunt;
import com.example.saas.domain.Etudiant;
import com.example.saas.domain.Notification;
import com.example.saas.domain.Ouvrage;
import com.example.saas.domain.ReglePenalite;
import com.example.saas.mapper.PenaliteMapper;
import com.example.saas.repository.EmpruntRepository;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.NotificationRepository;
import com.example.saas.repository.PenaliteRepository;
import com.example.saas.repository.ReglePenaliteRepository;
import com.example.saas.tenant.TenantContext;
import com.example.saas.tenant.TenantProvider;

@ExtendWith(MockitoExtension.class)
class PenaliteServiceImplTest {
    @Mock PenaliteRepository penalites;
    @Mock EmpruntRepository emprunts;
    @Mock ReglePenaliteRepository regles;
    @Mock NotificationRepository notifications;
    @Mock EtudiantRepository etudiants;
    @Mock PenaliteMapper mapper;
    @Mock TenantProvider tenant;

    private PenaliteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PenaliteServiceImpl(
                penalites, emprunts, regles, notifications, etudiants, mapper, tenant);
    }

        @AfterEach
        void clearRequestContext() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
        }

        @Test
        void shouldResolvePenaltyOwnerFromJwtUsername() {
        String tenantId = "tenant-a";
        UUID studentId = UUID.randomUUID();
        Etudiant etudiant = mock(Etudiant.class);
        when(tenant.currentTenant()).thenReturn(tenantId);
        when(etudiant.getId()).thenReturn(studentId);
        when(etudiants.findByUsernameEtTenant("client-c", tenantId))
            .thenReturn(Optional.of(etudiant));
        when(penalites.findAllByTenantIdAndEtudiantIdOrderByDateApplicationDesc(tenantId, studentId))
            .thenReturn(List.of());
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "HS256")
            .subject("client-c")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(900))
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(jwt, "test-token", List.of()));

        assertThat(service.mesPenalites()).isEmpty();
        verify(etudiants).findByUsernameEtTenant("client-c", tenantId);
        }

    @Test
    void shouldSendReminderWhenLoanIsDueWithin48Hours() {
        Emprunt emprunt = emprunt(Emprunt.Statut.ACTIF, Instant.now().plus(24, ChronoUnit.HOURS));
        when(emprunts.findAll()).thenReturn(List.of(emprunt));
        Etudiant etudiant = emprunt.getEtudiant();
        UUID etudiantId = UUID.randomUUID();
        when(etudiant.getId()).thenReturn(etudiantId);
        when(notifications.existsByTenantIdAndEtudiantIdAndTypeAndCreatedAtAfter(
            eq("tenant-a"), eq(etudiantId), eq(Notification.Type.RAPPEL_ECHEANCE),
                any(Instant.class))).thenReturn(false);

        service.traiterRetards();

        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(notification.capture());
        assertThat(notification.getValue().getType()).isEqualTo(Notification.Type.RAPPEL_ECHEANCE);
        assertThat(notification.getValue().getMessage()).contains("Livre ciblé");
        verify(penalites, never()).save(any());
    }

    @Test
    void shouldWarnAtDueDateButWaitForConfiguredGracePeriod() {
        Emprunt emprunt = emprunt(Emprunt.Statut.ACTIF, Instant.now().minus(1, ChronoUnit.DAYS));
        ReglePenalite regle = mock(ReglePenalite.class);
        when(emprunts.findAll()).thenReturn(List.of(emprunt));
        when(emprunt.getStatut()).thenReturn(
            Emprunt.Statut.ACTIF, Emprunt.Statut.ACTIF, Emprunt.Statut.RETARDE);
        when(regles.findAllByTenantIdOrderByCreatedAtDesc("tenant-a")).thenReturn(List.of(regle));
        when(regle.getJoursTolérance()).thenReturn(3);

        service.traiterRetards();

        verify(emprunt).retarder();
        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(notification.capture());
        assertThat(notification.getValue().getType()).isEqualTo(Notification.Type.AVERTISSEMENT_RETARD);
        verify(penalites, never()).save(any());
    }

    @Test
    void shouldApplyConfiguredPenaltyAfterGracePeriod() {
        Emprunt emprunt = emprunt(
                Emprunt.Statut.RETARDE, Instant.now().minus(5, ChronoUnit.DAYS));
        ReglePenalite regle = mock(ReglePenalite.class);
        when(emprunts.findAll()).thenReturn(List.of(emprunt));
        when(regles.findAllByTenantIdOrderByCreatedAtDesc("tenant-a")).thenReturn(List.of(regle));
        when(regle.getJoursTolérance()).thenReturn(2);
        when(regle.getTypeConsequence()).thenReturn(ReglePenalite.TypeConsequence.AVERTISSEMENT);
        when(emprunt.getId()).thenReturn(UUID.randomUUID());
        when(penalites.existsByTenantIdAndEmpruntId("tenant-a", emprunt.getId())).thenReturn(false);
        when(penalites.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.traiterRetards();

        verify(penalites).save(any());
        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(notifications).save(notification.capture());
        assertThat(notification.getValue().getType()).isEqualTo(Notification.Type.PENALITE);
    }

    private Emprunt emprunt(Emprunt.Statut statut, Instant expiration) {
        Emprunt emprunt = mock(Emprunt.class);
        Etudiant etudiant = mock(Etudiant.class);
        Ouvrage ouvrage = mock(Ouvrage.class);
        when(emprunt.getStatut()).thenReturn(statut);
        when(emprunt.getDateExpiration()).thenReturn(expiration);
        when(emprunt.getTenantId()).thenReturn("tenant-a");
        when(emprunt.getEtudiant()).thenReturn(etudiant);
        when(emprunt.getOuvrage()).thenReturn(ouvrage);
        when(ouvrage.getTitre()).thenReturn("Livre ciblé");
        return emprunt;
    }
}