package com.example.saas.service;

import com.example.saas.domain.Etablissement;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.EtablissementRepository;
import com.example.saas.repository.OuvrageRepository;
import com.example.saas.repository.TenantUserRepository;
import com.example.saas.storage.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtablissementServiceImplTest {
    @Mock EtablissementRepository etablissements;
    @Mock TenantUserRepository users;
    @Mock OuvrageRepository ouvrages;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JdbcTemplate jdbcTemplate;
    @Mock FileStorageService fileStorage;

    private EtablissementServiceImpl service;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
        service = new EtablissementServiceImpl(
                etablissements, users, ouvrages, passwordEncoder, jdbcTemplate, fileStorage);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void shouldDeleteTenantDataBeforeTheEstablishmentAndCleanFilesAfterCommit() {
        Etablissement etablissement = new Etablissement("tenant-b", "Université B", "ACTIF", Map.of());
        when(etablissements.findByCode("tenant-b")).thenReturn(Optional.of(etablissement));

        service.delete("tenant-b");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(14)).update(sql.capture(), eq("tenant-b"));
        assertThat(sql.getAllValues()).containsExactly(
                "DELETE FROM notifications WHERE tenant_id = ?",
                "DELETE FROM penalites WHERE tenant_id = ?",
                "DELETE FROM favoris WHERE tenant_id = ?",
                "DELETE FROM consultations WHERE tenant_id = ?",
                "DELETE FROM emprunts WHERE tenant_id = ?",
                "DELETE FROM fichiers WHERE tenant_id = ?",
                "DELETE FROM memoires WHERE tenant_id = ?",
                "DELETE FROM ouvrages WHERE tenant_id = ?",
                "DELETE FROM categories WHERE tenant_id = ?",
                "DELETE FROM filieres WHERE tenant_id = ?",
                "DELETE FROM niveaux WHERE tenant_id = ?",
                "DELETE FROM regles_penalite WHERE tenant_id = ?",
                "DELETE FROM projects WHERE tenant_id = ?",
                "DELETE FROM app_users WHERE tenant_id = ?");

        InOrder deletionOrder = inOrder(jdbcTemplate, etablissements);
        deletionOrder.verify(jdbcTemplate).update("DELETE FROM notifications WHERE tenant_id = ?", "tenant-b");
        deletionOrder.verify(etablissements).delete(etablissement);
        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        synchronizations.forEach(TransactionSynchronization::afterCommit);
        verify(fileStorage).deleteTenantFiles("tenant-b");
    }

    @Test
    void shouldNotDeleteAnythingWhenTheEstablishmentDoesNotExist() {
        when(etablissements.findByCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Etablissement introuvable");

        verify(jdbcTemplate, never()).update(org.mockito.ArgumentMatchers.anyString(), eq("missing"));
        verify(etablissements, never()).delete(org.mockito.ArgumentMatchers.any(Etablissement.class));
        assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
    }
}
