package com.example.saas.controller;

import com.example.saas.dto.*;
import com.example.saas.service.MemoireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/memoires")
@PreAuthorize("hasAnyRole('ADMIN_PLATEFORME','ADMIN_ETABLISSEMENT','ETUDIANT')")
public class MemoireController {
    private final MemoireService service;
    private final com.example.saas.service.ConsultationService consultationService;
    public MemoireController(MemoireService service, com.example.saas.service.ConsultationService consultationService) {
        this.service = service;
        this.consultationService = consultationService;
    }
    @GetMapping
    public List<MemoireResponse> list(@RequestParam(required = false) String search,
                                      @RequestParam(required = false) UUID filiereId, @RequestParam(required = false) UUID niveauId,
                                      @RequestParam(required = false) Integer annee) {
        return service.findAll(search, filiereId, niveauId, annee);
    }

    @GetMapping("/archives") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public List<MemoireResponse> archives() { return service.findArchives(); }

    @GetMapping("/{id}") public MemoireResponse get(@PathVariable UUID id) {
        MemoireResponse response = service.findById(id);
        consultationService.enregistrer(com.example.saas.domain.Consultation.TypeRessource.MEMOIRE, id);
        return response;
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public MemoireResponse create(@Valid @RequestBody MemoireRequest request) { return service.create(request); }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public MemoireResponse update(@PathVariable UUID id, @Valid @RequestBody MemoireRequest request) { return service.update(id, request); }

    @PatchMapping("/{id}/archiver") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public MemoireResponse archiver(@PathVariable UUID id) { return service.archiver(id); }

    @PatchMapping("/{id}/reactiver") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public MemoireResponse reactiver(@PathVariable UUID id) { return service.reactiver(id); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public void delete(@PathVariable UUID id) { service.delete(id); }

    @PostMapping("/{id}/fichiers") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public FichierResponse ajouterFichier(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        return service.ajouterFichier(id, file);
    }

    @DeleteMapping("/{id}/fichiers/{fichierId}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public void supprimerFichier(@PathVariable UUID id, @PathVariable UUID fichierId) {
        service.supprimerFichier(id, fichierId);
    }

    @PatchMapping("/{id}/fichiers/{fichierId}/disponibilite") @PreAuthorize("hasRole('ADMIN_ETABLISSEMENT')")
    public FichierResponse toggleDisponibilite(@PathVariable UUID id, @PathVariable UUID fichierId,
                                               @RequestParam boolean disponible) {
        return service.toggleDisponibilite(id, fichierId, disponible);
    }

    @GetMapping("/{id}/fichiers/{fichierId}")
    public ResponseEntity<byte[]> telechargerFichier(@PathVariable UUID id, @PathVariable UUID fichierId) {
        FichierDownloadResponse fichier = service.telechargerFichier(id, fichierId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(fichier.typeMime()));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(fichier.nomOriginal())
                .build());
        return new ResponseEntity<>(fichier.contenu(), headers, HttpStatus.OK);
    }
}