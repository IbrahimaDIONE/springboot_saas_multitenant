package com.example.saas.service.impl;

import com.example.saas.domain.*;
import com.example.saas.dto.PenaliteResponse;
import com.example.saas.mapper.PenaliteMapper;
import com.example.saas.repository.*;
import com.example.saas.service.PenaliteService;
import com.example.saas.tenant.TenantProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PenaliteServiceImpl implements PenaliteService {
    private final PenaliteRepository penalites;
    private final EmpruntRepository emprunts;
    private final ReglePenaliteRepository regles;
    private final NotificationRepository notifications;
    private final EtudiantRepository etudiants;
    private final PenaliteMapper mapper;
    private final TenantProvider tenant;

    public PenaliteServiceImpl(PenaliteRepository penalites, EmpruntRepository emprunts,
                               ReglePenaliteRepository regles, NotificationRepository notifications,
                               EtudiantRepository etudiants, PenaliteMapper mapper,
                               TenantProvider tenant) {
        this.penalites = penalites;
        this.emprunts = emprunts;
        this.regles = regles;
        this.notifications = notifications;
        this.etudiants = etudiants;
        this.mapper = mapper;
        this.tenant = tenant;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaliteResponse> mesPenalites() {
        String tenantId = tenant.currentTenant();
        Etudiant etudiant = currentEtudiant(tenantId);
        return penalites.findAllByTenantIdAndEtudiantIdOrderByDateApplicationDesc(tenantId, etudiant.getId())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaliteResponse> findAllByTenant() {
        return penalites.findAllByTenantIdOrderByDateApplicationDesc(tenant.currentTenant())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Scheduled(fixedDelayString = "${app.penalites.processing-delay-ms:3600000}")
    public void traiterRetards() {
        Instant now = Instant.now();
        for (Emprunt emprunt : emprunts.findAll()) {
            if (emprunt.getStatut() == Emprunt.Statut.ACTIF
                && emprunt.getDateExpiration().isAfter(now)
                && emprunt.getDateExpiration().isBefore(now.plus(2, ChronoUnit.DAYS))
                && !notifications.existsByTenantIdAndEtudiantIdAndTypeAndCreatedAtAfter(
                    emprunt.getTenantId(), emprunt.getEtudiant().getId(),
                    Notification.Type.RAPPEL_ECHEANCE, now.minus(1, ChronoUnit.DAYS))) {
            notifications.save(new Notification(emprunt.getTenantId(), emprunt.getEtudiant(),
                "Votre emprunt arrive à échéance dans moins de 48 heures.",
                Notification.Type.RAPPEL_ECHEANCE));
            }
            if (emprunt.getStatut() == Emprunt.Statut.ACTIF
                    && emprunt.getDateExpiration().isBefore(now)) {
                emprunt.retarder();
                String tenantId = emprunt.getTenantId();
                if (!penalites.existsByTenantIdAndEmpruntId(tenantId, emprunt.getId())) {
                    ReglePenalite regle = regles.findAllByTenantIdOrderByCreatedAtDesc(tenantId)
                            .stream().findFirst().orElse(null);
                    String consequence = regle == null
                            ? ReglePenalite.TypeConsequence.AVERTISSEMENT.name()
                            : regle.getTypeConsequence().name();
                    Penalite penalite = penalites.save(new Penalite(
                            tenantId, emprunt.getEtudiant(), emprunt,
                            "Retard de restitution de l'ouvrage", consequence));
                    notifications.save(new Notification(
                            tenantId, emprunt.getEtudiant(),
                            "Une pénalité a été appliquée à votre emprunt.",
                            Notification.Type.PENALITE));
                    if (regle != null && regle.getTypeConsequence()
                            == ReglePenalite.TypeConsequence.SUSPENSION_TEMPORAIRE) {
                        emprunt.getEtudiant().getUtilisateur().setEnabled(false);
                    }
                }
            }
        }
    }

    private Etudiant currentEtudiant(String tenantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(((Jwt) authentication.getPrincipal()).getSubject());
        return etudiants.findByUtilisateur_IdAndUtilisateur_TenantId(userId, tenantId)
            .orElseThrow(() -> new IllegalStateException("Étudiant non trouvé"));
    }
}
