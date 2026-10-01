package com.example.saas.storage;

import com.example.saas.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class OuvragePdfStorageService {
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final String FILE_NAME = "document.pdf";

    private final Path root;

    public OuvragePdfStorageService(
            @Value("${app.storage.ouvrages-dir:./data/ouvrages}") String storageDir) {
        root = Path.of(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le dossier des ouvrages", e);
        }
    }

    public String store(String tenantId, UUID ouvrageId, MultipartFile file) {
        validate(file);
        Path directory = directory(tenantId, ouvrageId);
        Path temporaryFile = null;
        try {
            Files.createDirectories(directory);
            temporaryFile = Files.createTempFile(directory, ".ouvrage-", ".upload");
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
            }
            Path destination = directory.resolve(FILE_NAME);
            try {
                Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return storageKey(tenantId, ouvrageId);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible d’enregistrer le PDF de l’ouvrage", e);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // A failed temporary-file cleanup must not hide the storage result.
                }
            }
        }
    }

    public byte[] read(String tenantId, UUID ouvrageId, String storedKey) {
        if (!storageKey(tenantId, ouvrageId).equals(storedKey)) {
            throw new ResourceNotFoundException("Document PDF introuvable");
        }
        Path file = directory(tenantId, ouvrageId).resolve(FILE_NAME).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("Document PDF introuvable");
        }
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de lire le PDF de l’ouvrage", e);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Sélectionnez un fichier PDF.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Le fichier PDF ne peut pas dépasser 20 Mo.");
        }
        try (InputStream input = file.getInputStream()) {
            byte[] signature = input.readNBytes(5);
            if (signature.length != 5 || signature[0] != '%'
                    || signature[1] != 'P' || signature[2] != 'D'
                    || signature[3] != 'F' || signature[4] != '-') {
                throw new IllegalArgumentException("Le fichier téléversé n’est pas un PDF valide.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Impossible de lire le fichier téléversé.", e);
        }
    }

    private Path directory(String tenantId, UUID ouvrageId) {
        Path directory = root.resolve(tenantId).resolve(ouvrageId.toString()).normalize();
        if (!directory.startsWith(root)) {
            throw new IllegalArgumentException("Chemin de stockage invalide.");
        }
        return directory;
    }

    private String storageKey(String tenantId, UUID ouvrageId) {
        return tenantId + "/" + ouvrageId + "/" + FILE_NAME;
    }
}