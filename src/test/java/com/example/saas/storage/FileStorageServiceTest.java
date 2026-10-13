package com.example.saas.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldDeleteOnlyTheSelectedTenantFiles() throws Exception {
        FileStorageService storage = new FileStorageService(temporaryDirectory.toString());
        Path tenantFile = temporaryDirectory.resolve("tenant-b/memoire-1/document.pdf");
        Path otherTenantFile = temporaryDirectory.resolve("tenant-c/memoire-2/document.pdf");
        Files.createDirectories(tenantFile.getParent());
        Files.createDirectories(otherTenantFile.getParent());
        Files.writeString(tenantFile, "tenant document");
        Files.writeString(otherTenantFile, "other tenant document");

        storage.deleteTenantFiles("tenant-b");

        assertThat(tenantFile).doesNotExist();
        assertThat(temporaryDirectory.resolve("tenant-b")).doesNotExist();
        assertThat(otherTenantFile).exists();
    }

    @Test
    void shouldRejectTenantPathsOutsideTheStorageRoot() {
        FileStorageService storage = new FileStorageService(temporaryDirectory.toString());

        assertThatThrownBy(() -> storage.deleteTenantFiles(".."))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Identifiant d'établissement invalide");
    }
}
