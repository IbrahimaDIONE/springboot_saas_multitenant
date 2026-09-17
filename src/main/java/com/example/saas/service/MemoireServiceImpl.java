package com.example.saas.service;

import com.example.saas.domain.*;
import com.example.saas.dto.*;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.*;
import com.example.saas.repository.*;
import com.example.saas.storage.FileStorageService;
import com.example.saas.tenant.TenantProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Transactional
public class MemoireServiceImpl implements MemoireService {
    private final MemoireRepository repository;
    private final MemoireMapper mapper;
    private final FichierRepository fichierRepository;
    private final FichierMapper fichierMapper;
    private final FiliereRepository filieres;
    private final NiveauRepository niveaux;
    private final FileStorageService storage;
    private final TenantProvider tenantProvider;

    public MemoireServiceImpl(MemoireRepository repository, MemoireMapper mapper,
                              FichierRepository fichierRepository, FichierMapper fichierMapper,
                              FiliereRepository filieres, NiveauRepository niveaux,
                              FileStorageService storage, TenantProvider tenantProvider) {
        this.repository = repository;
        this.mapper = mapper;
        this.fichierRepository = fichierRepository;
        this.fichierMapper = fichierMapper;
        this.filieres = filieres;
        this.niveaux = niveaux;
        this.storage = storage;
        this.tenantProvider = tenantProvider;
    }
    @Transactional(readOnly = true)
    public List<MemoireResponse> findAll() {
        return repository.findAllByTenantIdAndActifTrueOrderByAnneeDesc(tenant()).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<MemoireResponse> findAll(String search, UUID filiereId, UUID niveauId, Integer annee) {
        String term = search == null ? "" : search.trim();
        if (term.isEmpty() && filiereId == null && niveauId == null && annee == null) return findAll();
        return repository.searchByTenantId(tenant(), term, filiereId, niveauId, annee).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<MemoireResponse> findArchives() {
        return repository.findAllByTenantIdAndActifFalseOrderByAnneeDesc(tenant()).stream().map(mapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public MemoireResponse findById(UUID id) { return mapper.toResponse(findEntity(id)); }
    public MemoireResponse create(MemoireRequest r) {
        Memoire memoire = new Memoire(tenant(), r.titre(), r.auteur(), r.encadreur(), r.resume(), r.annee(),
                filiere(r.filiereId()), niveau(r.niveauId()));
        return mapper.toResponse(repository.save(memoire));
    }
    public MemoireResponse update(UUID id, MemoireRequest r) {
        Memoire memoire = findEntity(id);
        memoire.update(r.titre(), r.auteur(), r.encadreur(), r.resume(), r.annee(), filiere(r.filiereId()), niveau(r.niveauId()));
        return mapper.toResponse(memoire);
    }
    public MemoireResponse archiver(UUID id) {
        Memoire memoire = findEntity(id);
        memoire.archiver();
        return mapper.toResponse(memoire);
    }
    public MemoireResponse reactiver(UUID id) {
        Memoire memoire = findEntity(id);
        memoire.reactiver();
        return mapper.toResponse(memoire);
    }
    public void delete(UUID id) {
        Memoire memoire = findEntity(id);
        fichierRepository.findAllByMemoireIdAndTenantId(id, tenant()).forEach(f -> storage.delete(f.getCheminStockage()));
        repository.delete(memoire);
    }
    public FichierResponse ajouterFichier(UUID memoireId, MultipartFile file) {
        Memoire memoire = findEntity(memoireId);
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Fichier vide");
        if (!"application/pdf".equals(file.getContentType()))
            throw new IllegalArgumentException("Seuls les fichiers PDF sont acceptés");
        String chemin = storage.store(tenant(), memoireId, file);
        Fichier fichier = new Fichier(tenant(), memoire, file.getOriginalFilename(), chemin, file.getSize(), file.getContentType());
        return fichierMapper.toResponse(fichierRepository.save(fichier));
    }
    public void supprimerFichier(UUID memoireId, UUID fichierId) {
        Fichier fichier = findFichier(memoireId, fichierId);
        storage.delete(fichier.getCheminStockage());
        fichierRepository.delete(fichier);
    }
    public FichierResponse toggleDisponibilite(UUID memoireId, UUID fichierId, boolean disponible) {
        Fichier fichier = findFichier(memoireId, fichierId);
        if (disponible) fichier.rendreDisponible(); else fichier.rendreIndisponible();
        return fichierMapper.toResponse(fichier);
    }
    private Fichier findFichier(UUID memoireId, UUID fichierId) {
        Fichier fichier = fichierRepository.findByIdAndTenantId(fichierId, tenant())
                .orElseThrow(() -> new ResourceNotFoundException("Fichier introuvable"));
        if (!fichier.getMemoire().getId().equals(memoireId))
            throw new ResourceNotFoundException("Fichier introuvable pour ce mémoire");
        return fichier;
    }
    private Memoire findEntity(UUID id) {
        return repository.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResourceNotFoundException("Mémoire introuvable"));
    }
    private Filiere filiere(UUID id) {
        return filieres.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResourceNotFoundException("Filière introuvable"));
    }
    private Niveau niveau(UUID id) {
        return niveaux.findByIdAndTenantId(id, tenant()).orElseThrow(() -> new ResourceNotFoundException("Niveau introuvable"));
    }
    private String tenant() { return tenantProvider.currentTenant(); }
}