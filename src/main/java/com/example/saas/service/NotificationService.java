package com.example.saas.service;

import com.example.saas.dto.*;
import java.util.*;

public interface NotificationService {

    // Étudiant
    List<NotificationResponse> mesNotifications();
    List<NotificationResponse> mesNotificationsNonLues();
    NotificationResponse       marquerLu(UUID id);
    long                       compterNonLues();
    void                       notifierNouvelleRessource(String tenantId, String titre);

    // Admin
    NotificationResponse       envoyer(NotificationRequest request);
    List<NotificationResponse> findAllByTenant();

    // Règles de pénalité
    ReglePenaliteResponse       creerRegle(ReglePenaliteRequest request);
    List<ReglePenaliteResponse> listerRegles();
    ReglePenaliteResponse       modifierRegle(UUID id, ReglePenaliteRequest request);
    void                        supprimerRegle(UUID id);
}