package com.example.saas.service;

import com.example.saas.domain.*;
import com.example.saas.domain.Consultation.TypeRessource;
import com.example.saas.repository.ConsultationRepository;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.tenant.TenantContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ConsultationServiceImpl implements ConsultationService {
    private final ConsultationRepository repository;
    private final EtudiantRepository etudiantRepository;

    public ConsultationServiceImpl(ConsultationRepository repository, EtudiantRepository etudiantRepository) {
        this.repository = repository;
        this.etudiantRepository = etudiantRepository;
    }

    @Override
    public void enregistrer(TypeRessource typeRessource, UUID ressourceId) {
        if (!isStudent()) return;

        String tenantId = TenantContext.get();
        String username = currentUsername();
        etudiantRepository.findByUsernameEtTenant(username, tenantId)
                .ifPresent(etudiant ->
                        repository.save(new Consultation(tenantId, etudiant, typeRessource, ressourceId)));
    }

    private boolean isStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ETUDIANT".equals(a.getAuthority()));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        return jwt.getSubject();
    }
}