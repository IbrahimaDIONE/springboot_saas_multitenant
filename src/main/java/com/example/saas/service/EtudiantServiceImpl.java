package com.example.saas.service;

import com.example.saas.domain.Etudiant;
import com.example.saas.domain.TenantUser;
import com.example.saas.dto.EtudiantRequest;
import com.example.saas.dto.EtudiantResponse;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.TenantUserRepository;
import com.example.saas.tenant.TenantProvider;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EtudiantServiceImpl implements EtudiantService {
    private static final String ROLE = "ETUDIANT";

    private final TenantUserRepository users;
    private final EtudiantRepository etudiants;
    private final TenantProvider tenant;
    private final PasswordEncoder passwordEncoder;

    public EtudiantServiceImpl(
            TenantUserRepository users,
            EtudiantRepository etudiants,
            TenantProvider tenant,
            PasswordEncoder passwordEncoder) {
        this.users = users;
        this.etudiants = etudiants;
        this.tenant = tenant;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtudiantResponse> findAll() {
        return etudiants
            .findAllByUtilisateur_TenantIdOrderByUtilisateur_NomAscUtilisateur_PrenomAsc(
                tenant.currentTenant())
                .stream()
            .map(this::toResponse)
                .toList();
    }

    @Override
    public EtudiantResponse create(EtudiantRequest request) {
        TenantUser user =
                TenantUser.newEtudiant(
                        tenant.currentTenant(),
                        request.username(),
                        passwordEncoder.encode(request.password()),
                        request.nom(),
                        request.prenom(),
                        request.email());
        TenantUser savedUser = users.save(user);
        Etudiant etudiant = new Etudiant(savedUser);
        etudiant.setFiliereId(request.filiereId());
        etudiant.setNiveauId(request.niveauId());
        return toResponse(etudiants.save(etudiant));
    }

    @Override
    public EtudiantResponse activate(UUID id) {
        Etudiant etudiant =
                etudiants
                        .findByIdAndUtilisateur_TenantId(id, tenant.currentTenant())
                        .orElseThrow(() -> new ResourceNotFoundException("Étudiant introuvable"));
        TenantUser user = etudiant.getUtilisateur();
        if (!ROLE.equals(user.getRole())) {
            throw new ResourceNotFoundException("Étudiant introuvable");
        }
        user.setEnabled(true);
        return toResponse(etudiant);
    }

    private Etudiant findEtudiant(TenantUser user) {
        return etudiants
                .findByUtilisateur_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant introuvable"));
    }

    private EtudiantResponse toResponse(Etudiant etudiant) {
        TenantUser user = etudiant.getUtilisateur();
        return new EtudiantResponse(
                etudiant.getId(),
                user.getId(),
                user.getUsername(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.isEnabled(),
                etudiant.getFiliereId(),
                etudiant.getNiveauId());
    }
}