package com.example.saas.storage;

import com.example.saas.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OuvragePdfStorageServiceTest {
    @TempDir
    Path storageDirectory;

    private OuvragePdfStorageService storage;

    @BeforeEach
    void setUp() {
        storage = new OuvragePdfStorageService(storageDirectory.toString());
    }

    @Test
    void storesAndReadsPdfOnlyForMatchingTenantAndBook() {
        UUID bookId = UUID.randomUUID();
        byte[] pdf = "%PDF-1.7\nexample".getBytes(StandardCharsets.US_ASCII);
        MockMultipartFile upload = new MockMultipartFile("file", "book.pdf", "application/pdf", pdf);

        String key = storage.store("tenant-a", bookId, upload);

        assertEquals("tenant-a/" + bookId + "/document.pdf", key);
        assertArrayEquals(pdf, storage.read("tenant-a", bookId, key));
        assertThrows(ResourceNotFoundException.class, () -> storage.read("tenant-b", bookId, key));
        assertThrows(ResourceNotFoundException.class,
                () -> storage.read("tenant-a", UUID.randomUUID(), key));
    }

    @Test
    void rejectsFilesWithoutPdfSignature() {
        MockMultipartFile upload = new MockMultipartFile(
                "file", "book.pdf", "application/pdf", "not a pdf".getBytes(StandardCharsets.US_ASCII));

        assertThrows(IllegalArgumentException.class,
                () -> storage.store("tenant-a", UUID.randomUUID(), upload));
    }

    @Test
    void rejectsOversizedFilesBeforeOpeningTheirStream() throws Exception {
        MultipartFile upload = mock(MultipartFile.class);
        when(upload.isEmpty()).thenReturn(false);
        when(upload.getSize()).thenReturn(20L * 1024 * 1024 + 1);

        assertThrows(IllegalArgumentException.class,
                () -> storage.store("tenant-a", UUID.randomUUID(), upload));
        verify(upload, never()).getInputStream();
    }
}
