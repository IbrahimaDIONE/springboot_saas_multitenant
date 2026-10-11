package com.example.saas.service;

import com.example.saas.domain.*;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.OuvrageMapper;
import com.example.saas.repository.*;
import com.example.saas.storage.OuvragePdfStorageService;
import com.example.saas.tenant.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
@Transactional
public class OuvrageServiceImpl implements OuvrageService {
    private final OuvrageRepository repository;
    private final OuvrageMapper mapper;
    private final FiliereRepository filieres;
    private final NiveauRepository niveaux;
    private final CategorieRepository categories;
    private final NotificationService notifications;
    private final TenantProvider tenantProvider;
    private final OuvragePdfStorageService pdfStorage;

    public OuvrageServiceImpl(OuvrageRepository repository, OuvrageMapper mapper, FiliereRepository filieres,
                              NiveauRepository niveaux, CategorieRepository categories,
                              NotificationService notifications, TenantProvider tenantProvider,
                              OuvragePdfStorageService pdfStorage) {
        this.repository = repository;
        this.mapper = mapper;
        this.filieres = filieres;
        this.niveaux = niveaux;
        this.categories = categories;
        this.notifications = notifications;
        this.tenantProvider = tenantProvider;
        this.pdfStorage = pdfStorage;
    }
    @Transactional(readOnly = true)
    public List<OuvrageResponse> findAll() {
        return repository.findAllByTenantIdAndActifTrueOrderByTitre(tenant()).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<OuvrageResponse> findAll(String search, UUID filiereId, UUID niveauId, UUID categorieId) {
        String term = search == null ? "" : search.trim();
        if (term.isEmpty() && filiereId == null && niveauId == null && categorieId == null) return findAll();
        return repository.searchByTenantId(tenant(), term, filiereId, niveauId, categorieId).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<OuvrageResponse> findArchives() {
        return repository.findAllByTenantIdAndActifFalseOrderByTitre(tenant()).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public OuvrageResponse findById(UUID id) {
        return mapper.toResponse(repository.findByIdAndTenantIdAndActifTrue(id, tenant())
                .orElseThrow(() -> new ResourceNotFoundException("Ouvrage introuvable")));
    }
    public OuvrageResponse create(OuvrageRequest request) {
        Ouvrage ouvrage = new Ouvrage(tenant(), request.titre(), request.auteur(), request.resume(),
            filiere(request.filiereId()), niveau(request.niveauId()));
        ouvrage.associerCategorie(categorie(request.categorieId()));
        Ouvrage saved = repository.save(ouvrage);
        notifications.notifierNouvelleRessource(
            tenant(), saved.getTitre(), saved.getFiliere().getId(), saved.getNiveau().getId());
        return mapper.toResponse(saved);
    }
    public OuvrageResponse update(UUID id, OuvrageRequest request) {
        Ouvrage ouvrage = findEntity(id);
        ouvrage.update(request.titre(), request.auteur(), request.resume(), filiere(request.filiereId()), niveau(request.niveauId()));
        ouvrage.associerCategorie(categorie(request.categorieId()));
        return mapper.toResponse(ouvrage);
    }
    public void televerserPdf(UUID ouvrageId, MultipartFile file) {
        Ouvrage ouvrage = findEntity(ouvrageId);
        ouvrage.associerFichier(pdfStorage.store(tenant(), ouvrageId, file));
    }
    public OuvrageResponse archiver(UUID id) {
        Ouvrage ouvrage = findEntity(id);
        ouvrage.archiver();
        return mapper.toResponse(ouvrage);
    }
    public OuvrageResponse reactiver(UUID id) {
        Ouvrage ouvrage = findEntity(id);
        ouvrage.reactiver();
        return mapper.toResponse(ouvrage);
    }
    public void delete(UUID id) { repository.delete(findEntity(id)); }
    private Ouvrage findEntity(UUID id) {
        return repository.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResourceNotFoundException("Ouvrage introuvable"));
    }
    private Filiere filiere(UUID id) {
        return filieres.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResourceNotFoundException("Filière introuvable"));
    }
    private Niveau niveau(UUID id) {
        return niveaux.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResourceNotFoundException("Niveau introuvable"));
    }
    private Categorie categorie(UUID id) {
        return id == null ? null : categories.findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable"));
    }
    private String tenant() { return tenantProvider.currentTenant(); }
}