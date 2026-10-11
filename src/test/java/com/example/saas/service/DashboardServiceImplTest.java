package com.example.saas.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.saas.domain.Emprunt;
import com.example.saas.dto.DashboardEtablissementResponse;
import com.example.saas.repository.EmpruntRepository;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.OuvrageRepository;
import com.example.saas.tenant.TenantProvider;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {
    @Mock EmpruntRepository emprunts;
    @Mock EtudiantRepository etudiants;
    @Mock OuvrageRepository ouvrages;
    @Mock TenantProvider tenant;

    @Test
    void shouldAggregateOnlyCurrentTenant() {
        when(tenant.currentTenant()).thenReturn("tenant-b");
        when(etudiants.countByUtilisateur_TenantIdAndUtilisateur_Role("tenant-b", "ETUDIANT"))
            .thenReturn(12L);
        when(ouvrages.countByTenantIdAndActifTrue("tenant-b")).thenReturn(34L);
        when(emprunts.countByTenantIdAndStatut("tenant-b", Emprunt.Statut.ACTIF)).thenReturn(5L);
        when(emprunts.countByTenantIdAndStatut("tenant-b", Emprunt.Statut.EXPIRE)).thenReturn(2L);
        when(emprunts.countByTenantIdAndStatut("tenant-b", Emprunt.Statut.RETARDE)).thenReturn(1L);

        DashboardEtablissementResponse response =
                new DashboardServiceImpl(emprunts, etudiants, ouvrages, tenant)
                        .getEtablissementDashboard();

        assertThat(response.nombreEtudiants()).isEqualTo(12L);
        assertThat(response.nombreRessources()).isEqualTo(34L);
        assertThat(response.empruntsEnCours()).isEqualTo(5L);
        assertThat(response.empruntsExpires()).isEqualTo(2L);
        assertThat(response.empruntsRetardes()).isEqualTo(1L);
        verify(etudiants).countByUtilisateur_TenantIdAndUtilisateur_Role("tenant-b", "ETUDIANT");
        verify(ouvrages).countByTenantIdAndActifTrue("tenant-b");
    }
}
