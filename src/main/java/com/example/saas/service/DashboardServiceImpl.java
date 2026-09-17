package com.example.saas.service;

import com.example.saas.domain.Emprunt;
import com.example.saas.dto.DashboardEtablissementResponse;
import com.example.saas.repository.EmpruntRepository;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.OuvrageRepository;
import com.example.saas.tenant.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private final EmpruntRepository emprunts;
    private final EtudiantRepository etudiants;
    private final OuvrageRepository ouvrages;
    private final TenantProvider tenant;

    public DashboardServiceImpl(
            EmpruntRepository emprunts,
            EtudiantRepository etudiants,
            OuvrageRepository ouvrages,
            TenantProvider tenant) {
        this.emprunts = emprunts;
        this.etudiants = etudiants;
        this.ouvrages = ouvrages;
        this.tenant = tenant;
    }

    @Override
    public DashboardEtablissementResponse getEtablissementDashboard() {
        String tenantId = tenant.currentTenant();
        return new DashboardEtablissementResponse(
                etudiants.countByUtilisateur_TenantId(tenantId),
                ouvrages.countByTenantIdAndActifTrue(tenantId),
                emprunts.countByTenantIdAndStatut(tenantId, Emprunt.Statut.ACTIF),
                emprunts.countByTenantIdAndStatut(tenantId, Emprunt.Statut.EXPIRE),
                emprunts.countByTenantIdAndStatut(tenantId, Emprunt.Statut.RETARDE));
    }
}
