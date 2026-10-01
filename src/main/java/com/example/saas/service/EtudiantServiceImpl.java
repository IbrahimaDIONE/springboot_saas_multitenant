package com.example.saas.service;

import com.example.saas.domain.Etudiant;
import com.example.saas.domain.TenantUser;
import com.example.saas.dto.EtudiantRequest;
import com.example.saas.dto.EtudiantResponse;
import com.example.saas.dto.ImportEtudiantsResponse;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.TenantUserRepository;
import com.example.saas.tenant.TenantProvider;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.io.IOException;

@Service
@Transactional
public class EtudiantServiceImpl implements EtudiantService {
    private static final String ROLE = "ETUDIANT";

    private final TenantUserRepository users;
    private final EtudiantRepository etudiants;
    private final TenantProvider tenant;
    private final PasswordEncoder passwordEncoder;

    @Autowired
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
                        request.email(),
                        "ETUDIANT");
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

    @Override
    public ImportEtudiantsResponse importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier Excel est obligatoire");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Le fichier doit être au format .xlsx");
        }

        List<String> erreurs = new ArrayList<>();
        int total = 0;
        int ajoutes = 0;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> colonnes = lireEntetes(sheet.getRow(0));
            Set<String> obligatoires = Set.of("username", "password", "nom", "prenom", "email");
            if (!colonnes.keySet().containsAll(obligatoires)) {
                throw new IllegalArgumentException("Colonnes obligatoires: username, password, nom, prenom, email");
            }
            DataFormatter formatter = new DataFormatter();
            for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (row == null || rowIsEmpty(row, formatter)) continue;
                total++;
                try {
                    EtudiantRequest request = requestFrom(row, colonnes, formatter);
                    if (users.existsByUsernameIgnoreCase(request.username())) {
                        throw new IllegalArgumentException("username déjà utilisé");
                    }
                    create(request);
                    ajoutes++;
                } catch (RuntimeException exception) {
                    erreurs.add("Ligne " + (index + 1) + ": " + exception.getMessage());
                }
            }
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof IllegalArgumentException) throw (IllegalArgumentException) exception;
            throw new IllegalArgumentException("Impossible de lire le fichier Excel", exception);
        }
        return new ImportEtudiantsResponse(total, ajoutes, erreurs);
    }

    private Map<String, Integer> lireEntetes(Row row) {
        if (row == null) throw new IllegalArgumentException("La première ligne doit contenir les en-têtes");
        Map<String, Integer> result = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : row) {
            String value = formatter.formatCellValue(cell).trim().toLowerCase();
            if (!value.isBlank()) result.put(value, cell.getColumnIndex());
        }
        return result;
    }

    private EtudiantRequest requestFrom(Row row, Map<String, Integer> colonnes, DataFormatter formatter) {
        String username = value(row, colonnes, "username", formatter);
        String password = value(row, colonnes, "password", formatter);
        String nom = value(row, colonnes, "nom", formatter);
        String prenom = value(row, colonnes, "prenom", formatter);
        String email = value(row, colonnes, "email", formatter);
        if (username.isBlank() || password.length() < 8 || nom.isBlank() || prenom.isBlank() || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("username, password (8 caractères), nom, prenom et email valides sont obligatoires");
        }
        return new EtudiantRequest(username, password, nom, prenom, email,
                optionalUuid(row, colonnes, "filiereId", formatter),
                optionalUuid(row, colonnes, "niveauId", formatter));
    }

    private String value(Row row, Map<String, Integer> colonnes, String name, DataFormatter formatter) {
        Integer column = colonnes.get(name);
        return column == null || row.getCell(column) == null ? "" : formatter.formatCellValue(row.getCell(column)).trim();
    }

    private UUID optionalUuid(Row row, Map<String, Integer> colonnes, String name, DataFormatter formatter) {
        String value = value(row, colonnes, name, formatter);
        if (value.isBlank()) return null;
        try { return UUID.fromString(value); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException(name + " invalide"); }
    }

    private boolean rowIsEmpty(Row row, DataFormatter formatter) {
        for (Cell cell : row) if (!formatter.formatCellValue(cell).trim().isBlank()) return false;
        return true;
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