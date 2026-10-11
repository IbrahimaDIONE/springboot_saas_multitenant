package com.example.saas.service.impl;

import com.example.saas.domain.Emprunt;
import com.example.saas.domain.Etudiant;
import com.example.saas.domain.Ouvrage;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.mapper.EmpruntMapper;
import com.example.saas.repository.EmpruntRepository;
import com.example.saas.repository.EtudiantRepository;
import com.example.saas.repository.OuvrageRepository;
import com.example.saas.storage.OuvragePdfStorageService;
import com.example.saas.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmpruntLectureAccessTest {
    private final EmpruntRepository emprunts = mock(EmpruntRepository.class);
    private final OuvrageRepository ouvrages = mock(OuvrageRepository.class);
    private final EtudiantRepository etudiants = mock(EtudiantRepository.class);
    private final EmpruntMapper mapper = mock(EmpruntMapper.class);
    private final OuvragePdfStorageService storage = mock(OuvragePdfStorageService.class);
    private final EmpruntServiceImpl service = new EmpruntServiceImpl(emprunts, ouvrages, etudiants, mapper, storage);

    private final UUID empruntId = UUID.randomUUID();
    private final UUID currentStudentId = UUID.randomUUID();
    private final UUID ouvrageId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.set("tenant-a");
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("student-a")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .build();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsPdfOnlyForStudentsWithTheirOwnActiveLoan() {
        Etudiant currentStudent = mock(Etudiant.class);
        Emprunt activeLoan = mock(Emprunt.class);
        Ouvrage ouvrage = mock(Ouvrage.class);
        byte[] pdf = "%PDF-1.7".getBytes();
        String storageKey = "tenant-a/" + ouvrageId + "/document.pdf";

        when(currentStudent.getId()).thenReturn(currentStudentId);
        when(etudiants.findByUsernameEtTenant("student-a", "tenant-a")).thenReturn(Optional.of(currentStudent));
        when(emprunts.findByIdAndTenantId(empruntId, "tenant-a")).thenReturn(Optional.of(activeLoan));
        when(activeLoan.getEtudiant()).thenReturn(currentStudent);
        when(activeLoan.getOuvrage()).thenReturn(ouvrage);
        when(activeLoan.getStatut()).thenReturn(Emprunt.Statut.ACTIF);
        when(ouvrage.getId()).thenReturn(ouvrageId);
        when(ouvrage.getUrlFichier()).thenReturn(storageKey);
        when(storage.read("tenant-a", ouvrageId, storageKey)).thenReturn(pdf);

        assertArrayEquals(pdf, service.lireFichier(empruntId));
        verify(storage).read("tenant-a", ouvrageId, storageKey);
    }

    @Test
    void rejectsAnotherStudentsLoanBeforeReadingTheFile() {
        Etudiant currentStudent = mock(Etudiant.class);
        Etudiant otherStudent = mock(Etudiant.class);
        Emprunt activeLoan = mock(Emprunt.class);

        when(currentStudent.getId()).thenReturn(currentStudentId);
        when(otherStudent.getId()).thenReturn(UUID.randomUUID());
        when(etudiants.findByUsernameEtTenant("student-a", "tenant-a")).thenReturn(Optional.of(currentStudent));
        when(emprunts.findByIdAndTenantId(empruntId, "tenant-a")).thenReturn(Optional.of(activeLoan));
        when(activeLoan.getEtudiant()).thenReturn(otherStudent);

        assertThrows(ResourceNotFoundException.class, () -> service.lireFichier(empruntId));
        verify(storage, never()).read(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void rejectsExpiredLoanBeforeReadingTheFile() {
        Etudiant currentStudent = mock(Etudiant.class);
        Emprunt expiredLoan = mock(Emprunt.class);

        when(currentStudent.getId()).thenReturn(currentStudentId);
        when(etudiants.findByUsernameEtTenant("student-a", "tenant-a")).thenReturn(Optional.of(currentStudent));
        when(emprunts.findByIdAndTenantId(empruntId, "tenant-a")).thenReturn(Optional.of(expiredLoan));
        when(expiredLoan.getEtudiant()).thenReturn(currentStudent);
        when(expiredLoan.getStatut()).thenReturn(Emprunt.Statut.EXPIRE);

        assertThrows(IllegalStateException.class, () -> service.lireFichier(empruntId));
        verify(storage, never()).read(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }
}
