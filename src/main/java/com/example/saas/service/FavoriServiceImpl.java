package com.example.saas.service;

import com.example.saas.domain.*;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.*;
import com.example.saas.tenant.TenantContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class FavoriServiceImpl implements FavoriService {
    private final FavoriRepository repository;
    private final EtudiantRepository etudiantRepository;
    private final OuvrageRepository ouvrageRepository;
    private final MemoireRepository memoireRepository;

    public FavoriServiceImpl(FavoriRepository repository, EtudiantRepository etudiantRepository,
                             OuvrageRepository ouvrageRepository, MemoireRepository memoireRepository) {
        this.repository = repository;
        this.etudiantRepository = etudiantRepository;
        this.ouvrageRepository = ouvrageRepository;
        this.memoireRepository = memoireRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriResponse> mesFavoris() {
        String tenantId = TenantContext.get();
        Etudiant etudiant = etudiantCourant(tenantId);
        return repository.findAllByTenantIdAndEtudiantIdOrderByCreatedAtDesc(tenantId, etudiant.getId())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public FavoriResponse ajouter(FavoriRequest request) {
        String tenantId = TenantContext.get();
        Etudiant etudiant = etudiantCourant(tenantId);

        verifierRessourceExiste(tenantId, request.typeRessource(), request.ressourceId());

        boolean dejaEnFavori = repository.existsByTenantIdAndEtudiantIdAndTypeRessourceAndRessourceId(
                tenantId, etudiant.getId(), request.typeRessource(), request.ressourceId());
        if (dejaEnFavori) {
            throw new IllegalStateException("Cette ressource est déjà dans vos favoris.");
        }

        Favori favori = new Favori(tenantId, etudiant, request.typeRessource(), request.ressourceId());
        return toResponse(repository.save(favori));
    }

    @Override
    public void retirer(UUID favoriId) {
        String tenantId = TenantContext.get();
        Etudiant etudiant = etudiantCourant(tenantId);
        Favori favori = repository.findByIdAndTenantIdAndEtudiantId(favoriId, tenantId, etudiant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Favori introuvable"));
        repository.delete(favori);
    }

    private void verifierRessourceExiste(String tenantId, Favori.TypeRessource type, UUID ressourceId) {
        boolean existe = switch (type) {
            case OUVRAGE -> ouvrageRepository.findByIdAndTenantId(ressourceId, tenantId).isPresent();
            case MEMOIRE -> memoireRepository.findByIdAndTenantId(ressourceId, tenantId).isPresent();
        };
        if (!existe) throw new ResourceNotFoundException("Ressource introuvable");
    }

    private FavoriResponse toResponse(Favori favori) {
        String titre = switch (favori.getTypeRessource()) {
            case OUVRAGE -> ouvrageRepository.findByIdAndTenantId(favori.getRessourceId(), favori.getTenantId())
                    .map(Ouvrage::getTitre).orElse("(ressource supprimée)");
            case MEMOIRE -> memoireRepository.findByIdAndTenantId(favori.getRessourceId(), favori.getTenantId())
                    .map(Memoire::getTitre).orElse("(ressource supprimée)");
        };
        return new FavoriResponse(favori.getId(), favori.getTypeRessource(), favori.getRessourceId(),
                titre, favori.getCreatedAt());
    }

    private Etudiant etudiantCourant(String tenantId) {
        String username = currentUsername();
        return etudiantRepository.findByUsernameEtTenant(username, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Étudiant non trouvé"));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();
        return jwt.getSubject();
    }
}