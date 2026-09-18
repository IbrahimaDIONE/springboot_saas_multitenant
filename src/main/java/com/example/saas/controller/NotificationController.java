package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.NotificationService;
import com.example.saas.service.PenaliteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Contrôleur REST mince : validation, statuts HTTP
 * et délégation au service.
 */
@RestController
public class NotificationController {

    private final NotificationService service;
    private final PenaliteService penaliteService;

    public NotificationController(NotificationService service, PenaliteService penaliteService) {
        this.service = service;
        this.penaliteService = penaliteService;
    }

    // ── ÉTUDIANT ──────────────────────────────────

    /** Étudiant : toutes ses notifications */
    @GetMapping("/api/notifications/mes-notifications")
    @PreAuthorize("hasRole('ETUDIANT')")
    public List<NotificationResponse> mesNotifications() {
        return service.mesNotifications();
    }

    /** Étudiant : notifications non lues */
    @GetMapping("/api/notifications/non-lues")
    @PreAuthorize("hasRole('ETUDIANT')")
    public List<NotificationResponse> nonLues() {
        return service.mesNotificationsNonLues();
    }

    /** Étudiant : compter les non lues */
    @GetMapping("/api/notifications/compter-non-lues")
    @PreAuthorize("hasRole('ETUDIANT')")
    public Map<String, Long> compterNonLues() {
        return Map.of("nonLues", service.compterNonLues());
    }

    /** Étudiant : marquer une notification comme lue */
    @PatchMapping("/api/notifications/{id}/lire")
    @PreAuthorize("hasRole('ETUDIANT')")
    public NotificationResponse marquerLu(@PathVariable UUID id) {
        return service.marquerLu(id);
    }

    // ── ADMIN ─────────────────────────────────────

    /** Admin : envoyer une notification à un étudiant */
    @PostMapping("/api/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public NotificationResponse envoyer(
            @Valid @RequestBody NotificationRequest request) {
        return service.envoyer(request);
    }

    /** Admin : toutes les notifications de l'établissement */
    @GetMapping("/api/notifications")
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public List<NotificationResponse> listAll() {
        return service.findAllByTenant();
    }

    // ── RÈGLES DE PÉNALITÉ ────────────────────────

    /** Admin : créer une règle de pénalité */
    @PostMapping("/api/regles-penalite")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public ReglePenaliteResponse creerRegle(
            @Valid @RequestBody ReglePenaliteRequest request) {
        return service.creerRegle(request);
    }

    /** Admin : lister les règles de pénalité */
    @GetMapping("/api/regles-penalite")
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public List<ReglePenaliteResponse> listerRegles() {
        return service.listerRegles();
    }

    /** Admin : modifier une règle */
    @PutMapping("/api/regles-penalite/{id}")
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public ReglePenaliteResponse modifierRegle(
            @PathVariable UUID id,
            @Valid @RequestBody ReglePenaliteRequest request) {
        return service.modifierRegle(id, request);
    }

    /** Admin : supprimer une règle */
    @DeleteMapping("/api/regles-penalite/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public void supprimerRegle(@PathVariable UUID id) {
        service.supprimerRegle(id);
    }

    @GetMapping("/api/penalites/mes-penalites")
    @PreAuthorize("hasRole('ETUDIANT')")
    public List<PenaliteResponse> mesPenalites() {
        return penaliteService.mesPenalites();
    }

    @GetMapping("/api/penalites")
    @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public List<PenaliteResponse> listerPenalites() {
        return penaliteService.findAllByTenant();
    }
}