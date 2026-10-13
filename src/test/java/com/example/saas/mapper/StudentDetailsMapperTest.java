package com.example.saas.mapper;

import com.example.saas.domain.Emprunt;
import com.example.saas.domain.Etudiant;
import com.example.saas.domain.Notification;
import com.example.saas.domain.Ouvrage;
import com.example.saas.domain.TenantUser;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentDetailsMapperTest {
    @Test
    void notificationResponseIncludesRecipientIdentityAndReadState() {
        UUID studentId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        TenantUser user = mock(TenantUser.class);
        Etudiant student = mock(Etudiant.class);
        Notification notification = mock(Notification.class);
        when(student.getId()).thenReturn(studentId);
        when(student.getUtilisateur()).thenReturn(user);
        when(user.getPrenom()).thenReturn("Awa");
        when(user.getNom()).thenReturn("Diop");
        when(user.getUsername()).thenReturn("awa.diop");
        when(notification.getEtudiant()).thenReturn(student);
        when(notification.getId()).thenReturn(UUID.randomUUID());
        when(notification.getMessage()).thenReturn("Rappel d’échéance");
        when(notification.getType()).thenReturn(Notification.Type.RAPPEL_ECHEANCE);
        when(notification.isLu()).thenReturn(true);
        when(notification.getCreatedAt()).thenReturn(createdAt);

        var response = new NotificationMapper().toResponse(notification);

        assertThat(response.etudiantId()).isEqualTo(studentId);
        assertThat(response.etudiantPrenom()).isEqualTo("Awa");
        assertThat(response.etudiantNom()).isEqualTo("Diop");
        assertThat(response.etudiantUsername()).isEqualTo("awa.diop");
        assertThat(response.lu()).isTrue();
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void loanResponseIncludesStudentFirstAndLastName() {
        UUID studentId = UUID.randomUUID();
        TenantUser user = mock(TenantUser.class);
        Etudiant student = mock(Etudiant.class);
        Ouvrage ouvrage = mock(Ouvrage.class);
        Emprunt emprunt = mock(Emprunt.class);
        when(student.getId()).thenReturn(studentId);
        when(student.getUtilisateur()).thenReturn(user);
        when(user.getPrenom()).thenReturn("Moussa");
        when(user.getNom()).thenReturn("Fall");
        when(ouvrage.getId()).thenReturn(UUID.randomUUID());
        when(ouvrage.getTitre()).thenReturn("Algorithmique");
        when(ouvrage.getAuteur()).thenReturn("A. Auteur");
        when(emprunt.getId()).thenReturn(UUID.randomUUID());
        when(emprunt.getEtudiant()).thenReturn(student);
        when(emprunt.getOuvrage()).thenReturn(ouvrage);
        when(emprunt.getStatut()).thenReturn(Emprunt.Statut.ACTIF);

        var response = new EmpruntMapper().toResponse(emprunt);

        assertThat(response.etudiantId()).isEqualTo(studentId);
        assertThat(response.etudiantPrenom()).isEqualTo("Moussa");
        assertThat(response.etudiantNom()).isEqualTo("Fall");
    }
}
