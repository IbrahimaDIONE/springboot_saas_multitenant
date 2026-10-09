package com.example.saas.service;

import com.example.saas.dto.*;
import com.example.saas.domain.Ouvrage;
import java.util.*;

public interface NotificationService {

    // Étudiant
    List<NotificationResponse> mesNotifications();
    List<NotificationResponse> mesNotificationsNonLues();
    NotificationResponse       marquerLu(UUID id);
    long                       compterNonLues();
        void                       notifierNouvelleRessource(
            String tenantId, String titre, UUID filiereId, UUID niveauId);

    // Admin
    List<NotificationResponse> findAllByTenant();

    // Règles de pénalité
    ReglePenaliteResponse       creerRegle(ReglePenaliteRequest request);
    List<ReglePenaliteResponse> listerRegles();
    ReglePenaliteResponse       modifierRegle(UUID id, ReglePenaliteRequest request);
    void                        supprimerRegle(UUID id);
}