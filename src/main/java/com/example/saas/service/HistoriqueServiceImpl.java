package com.example.saas.service;

import com.example.saas.domain.*;
import com.example.saas.dto.HistoriqueItemResponse;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.*;
import com.example.saas.tenant.TenantContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class HistoriqueServiceImpl implements HistoriqueService {
    private final EmpruntRepository empruntRepository;
    private final ConsultationRepository consultationRepository;
    private final EtudiantRepository etudiantRepository;
    private final OuvrageRepository ouvrageRepository;
    private final MemoireRepository memoireRepository;

    public HistoriqueServiceImpl(EmpruntRepository empruntRepository,
                                 ConsultationRepository consultationRepository, EtudiantRepository etudiantRepository,
                                 OuvrageRepository ouvrageRepository, MemoireRepository memoireRepository) {
        this.empruntRepository = empruntRepository;
        this.consultationRepository = consultationRepository;
        this.etudiantRepository = etudiantRepository;
        this.ouvrageRepository = ouvrageRepository;
        this.memoireRepository = memoireRepository;
    }

    @Override
    public List<HistoriqueItemResponse> mesActivites(String typeAction) {
        String tenantId = TenantContext.get();
        String username = currentUsername();
        Etudiant etudiant = etudiantRepository.findByUsernameEtTenant(username, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));

        Stream<HistoriqueItemResponse> emprunts = Stream.empty();
        Stream<HistoriqueItemResponse> consultations = Stream.empty();

        if (typeAction == null || typeAction.equalsIgnoreCase("EMPRUNT")) {
            emprunts = empruntRepository.findAllByTenantIdAndEtudiantId(tenantId, etudiant.getId())
                    .stream()
                    .map(e -> new HistoriqueItemResponse(
                            "EMPRUNT", "OUVRAGE", e.getOuvrage().getId(), e.getOuvrage().getTitre(),
                            e.getStatut().name(), e.getDateEmprunt()));
        }

        if (typeAction == null || typeAction.equalsIgnoreCase("CONSULTATION")) {
            consultations = consultationRepository.findAllByTenantIdAndEtudiantIdOrderByDateConsultationDesc(tenantId, etudiant.getId())
                    .stream()
                    .map(c -> new HistoriqueItemResponse(
                            "CONSULTATION", c.getTypeRessource().name(), c.getRessourceId(),
                            titreRessource(c),
                            null, c.getDateConsultation()));
        }

        return Stream.concat(emprunts, consultations)
                .sorted(Comparator.comparing(HistoriqueItemResponse::date).reversed())
                .toList();
    }

    private String titreRessource(Consultation consultation) {
        return switch (consultation.getTypeRessource()) {
            case OUVRAGE -> ouvrageRepository.findByIdAndTenantId(
                    consultation.getRessourceId(), consultation.getTenantId())
                    .map(Ouvrage::getTitre).orElse("(ressource supprimée)");
            case MEMOIRE -> memoireRepository.findByIdAndTenantId(
                    consultation.getRessourceId(), consultation.getTenantId())
                    .map(Memoire::getTitre).orElse("(ressource supprimée)");
        };
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        return jwt.getSubject();
    }
}