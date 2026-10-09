package com.example.saas.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.saas.domain.Etudiant;
import com.example.saas.domain.Notification;
import com.example.saas.mapper.NotificationMapper;
import com.example.saas.mapper.ReglePenaliteMapper;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.NotificationRepository;
import com.example.saas.repository.ReglePenaliteRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock NotificationRepository notifications;
    @Mock ReglePenaliteRepository regles;
    @Mock EtudiantRepository etudiants;
    @Mock NotificationMapper notificationMapper;
    @Mock ReglePenaliteMapper regleMapper;

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
}