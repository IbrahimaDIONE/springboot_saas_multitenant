package com.example.saas.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Component
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.storage.memoires-dir:./data/memoires}") String storageDir) {
        this.root = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le dossier de stockage: " + root, e);
        }
    }

    public String store(String tenantId, UUID memoireId, MultipartFile file) {
        try {
            Path dir = root.resolve(tenantId).resolve(memoireId.toString());
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return root.relativize(target).toString();
        } catch (IOException e) {
            throw new IllegalStateException("Échec de l'enregistrement du fichier", e);
        }
    }

    public void delete(String cheminRelatif) {
        try {
            Files.deleteIfExists(resolve(cheminRelatif));
        } catch (IOException e) {
            throw new IllegalStateException("Échec de la suppression du fichier", e);
        }
    }

    public byte[] read(String cheminRelatif) {
        try {
            return Files.readAllBytes(resolve(cheminRelatif));
        } catch (IOException e) {
            throw new IllegalStateException("Échec de la lecture du fichier", e);
        }
    }

    private Path resolve(String cheminRelatif) {
        Path target = root.resolve(cheminRelatif).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Chemin de fichier invalide");
        }
        return target;
    }

    private String sanitize(String filename) {
        if (filename == null) return "fichier";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}