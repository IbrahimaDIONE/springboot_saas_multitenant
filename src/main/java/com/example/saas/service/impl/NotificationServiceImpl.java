package com.example.saas.service.impl;

import com.example.saas.domain.*;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.*;
import com.example.saas.repository.*;
import com.example.saas.service.NotificationService;
import com.example.saas.tenant.TenantContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository    notificationRepository;
    private final ReglePenaliteRepository   reglePenaliteRepository;
    private final EtudiantRepository        etudiantRepository;
    private final NotificationMapper        notificationMapper;
    private final ReglePenaliteMapper       reglePenaliteMapper;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            ReglePenaliteRepository reglePenaliteRepository,
            EtudiantRepository etudiantRepository,
            NotificationMapper notificationMapper,
            ReglePenaliteMapper reglePenaliteMapper) {
        this.notificationRepository  = notificationRepository;
        this.reglePenaliteRepository = reglePenaliteRepository;
        this.etudiantRepository      = etudiantRepository;
        this.notificationMapper      = notificationMapper;
        this.reglePenaliteMapper     = reglePenaliteMapper;
    }

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        return UUID.fromString(jwt.getSubject());
    }

    private Etudiant currentEtudiant(String tenantId) {
        return etudiantRepository
                .findByUtilisateur_IdAndUtilisateur_TenantId(currentUserId(), tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Étudiant non trouvé"));
    }

    // ── ÉTUDIANT ──────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> mesNotifications() {
        String tenantId = TenantContext.get();
        Etudiant etudiant = currentEtudiant(tenantId);
        return notificationRepository
                .findAllByTenantIdAndEtudiantIdOrderByCreatedAtDesc(
                        tenantId, etudiant.getId())
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> mesNotificationsNonLues() {
        String tenantId = TenantContext.get();
        Etudiant etudiant = currentEtudiant(tenantId);
        return notificationRepository
                .findAllByTenantIdAndEtudiantIdAndLuFalseOrderByCreatedAtDesc(
                        tenantId, etudiant.getId())
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse marquerLu(UUID id) {
        String tenantId = TenantContext.get();
        Notification notification = notificationRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification non trouvée"));
        if (!notification.getEtudiant().getId().equals(currentEtudiant(tenantId).getId())) {
            throw new ResourceNotFoundException("Notification non trouvée");
        }
        notification.marquerLu();
        return notificationMapper.toResponse(
                notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public long compterNonLues() {
        String tenantId = TenantContext.get();
        Etudiant etudiant = currentEtudiant(tenantId);
        return notificationRepository
                .countByTenantIdAndEtudiantIdAndLuFalse(
                        tenantId, etudiant.getId());
    }

        @Override
        public void notifierNouvelleRessource(String tenantId, String titre) {
                for (Etudiant etudiant : etudiantRepository
                                .findAllByUtilisateur_TenantIdOrderByUtilisateur_NomAscUtilisateur_PrenomAsc(tenantId)) {
                        notificationRepository.save(new Notification(tenantId, etudiant,
                                        "Nouvelle ressource disponible : " + titre,
                                        Notification.Type.NOUVELLE_RESSOURCE));
                }
        }

    // ── ADMIN ─────────────────────────────────────

    @Override
    public NotificationResponse envoyer(NotificationRequest request) {
        String tenantId = TenantContext.get();

        Etudiant etudiant = etudiantRepository
                .findByUtilisateur_IdAndUtilisateur_TenantId(request.etudiantId(), tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Étudiant non trouvé"));

        Notification.Type type = Notification.Type.valueOf(
                request.type().toUpperCase());

        Notification notification = new Notification(
                tenantId, etudiant, request.message(), type);

        return notificationMapper.toResponse(
                notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> findAllByTenant() {
        return notificationRepository
                .findAllByTenantIdOrderByCreatedAtDesc(TenantContext.get())
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    // ── RÈGLES DE PÉNALITÉ ────────────────────────

    @Override
    public ReglePenaliteResponse creerRegle(ReglePenaliteRequest request) {
        String tenantId = TenantContext.get();
        ReglePenalite.TypeConsequence type =
                ReglePenalite.TypeConsequence.valueOf(
                        request.typeConsequence().toUpperCase());
        ReglePenalite regle = new ReglePenalite(
                tenantId,
                request.joursTolérance(),
                type,
                request.description());
        return reglePenaliteMapper.toResponse(
                reglePenaliteRepository.save(regle));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglePenaliteResponse> listerRegles() {
        return reglePenaliteRepository
                .findAllByTenantIdOrderByCreatedAtDesc(TenantContext.get())
                .stream()
                .map(reglePenaliteMapper::toResponse)
                .toList();
    }

    @Override
    public ReglePenaliteResponse modifierRegle(
            UUID id, ReglePenaliteRequest request) {
        String tenantId = TenantContext.get();
        ReglePenalite regle = reglePenaliteRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Règle non trouvée"));
        ReglePenalite.TypeConsequence type =
                ReglePenalite.TypeConsequence.valueOf(
                        request.typeConsequence().toUpperCase());
        regle.update(request.joursTolérance(), type, request.description());
        return reglePenaliteMapper.toResponse(
                reglePenaliteRepository.save(regle));
    }

    @Override
    public void supprimerRegle(UUID id) {
        String tenantId = TenantContext.get();
        ReglePenalite regle = reglePenaliteRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Règle non trouvée"));
        reglePenaliteRepository.delete(regle);
    }
}