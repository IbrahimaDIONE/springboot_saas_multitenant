package com.example.saas.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EmpruntTest {
    @Test
    void retourMarqueEmpruntEtDateRetour() {
        Emprunt emprunt = new Emprunt(
                "tenant-a", mock(Etudiant.class), mock(Ouvrage.class),
                Instant.now().plusSeconds(3600));

        emprunt.retourner();

        assertThat(emprunt.getStatut()).isEqualTo(Emprunt.Statut.RETOURNE);
        assertThat(emprunt.getDateRetour()).isNotNull();
    }
}
