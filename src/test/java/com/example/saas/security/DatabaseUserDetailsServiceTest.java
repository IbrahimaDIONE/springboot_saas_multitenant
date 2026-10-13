package com.example.saas.security;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.saas.domain.Etablissement;
import com.example.saas.domain.TenantUser;
import com.example.saas.repository.EtablissementRepository;
import com.example.saas.repository.TenantUserRepository;
import java.util.Map;

class DatabaseUserDetailsServiceTest {

    @Test
    void loadUserByUsername_allowsPlatformAdminWithoutInstitution() {
        TenantUserRepository repository = mock(TenantUserRepository.class);
        EtablissementRepository etablissements = mock(EtablissementRepository.class);

        TenantUser admin = TenantUser.newAdminEtablissement(
                "tenant-platform",
                "ibzodione20@gmail.com",
                "{bcrypt}$2b$10$dummy",
                "Dione",
                "Ibrahima",
                "ibzodione20@gmail.com",
                "ADMIN_PLATEFORME");

        when(repository.findByUsernameIgnoreCase("ibzodione20@gmail.com"))
                .thenReturn(Optional.of(admin));
        UserDetails user =
                new DatabaseUserDetailsService(repository, etablissements)
                        .loadUserByUsername("ibzodione20@gmail.com");

        assertEquals("ibzodione20@gmail.com", user.getUsername());
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN_PLATEFORME")));
        assertTrue(user.isEnabled());
    }

    @Test
    void loadUserByUsername_resolvesInstitutionCodeFromTenantId() {
        TenantUserRepository repository = mock(TenantUserRepository.class);
        EtablissementRepository etablissements = mock(EtablissementRepository.class);
        TenantUser admin = TenantUser.newAdminEtablissement(
                "tenant-ipd", "direction@ipd.sn", "{bcrypt}$2b$10$dummy", "Ndiaye", "Mouhamed",
                "direction@ipd.sn", "ADMIN_ETABLISSEMENT");

        when(repository.findByUsernameIgnoreCase("direction@ipd.sn"))
                .thenReturn(Optional.of(admin));
        when(etablissements.findByCodeIgnoreCase("IPD"))
                .thenReturn(Optional.of(new Etablissement("IPD", "Institut", "ACTIF", Map.of())));

        UserDetails user = new DatabaseUserDetailsService(repository, etablissements)
                .loadUserByUsername("direction@ipd.sn");

        assertEquals("direction@ipd.sn", user.getUsername());
        assertTrue(user.isEnabled());
    }

    @Test
    void loadUserByUsername_acceptsLowercaseCodeForNewInstitutionAdmin() {
        TenantUserRepository repository = mock(TenantUserRepository.class);
        EtablissementRepository etablissements = mock(EtablissementRepository.class);
        TenantUser admin = TenantUser.newAdminEtablissement(
                "ipd-test", "admin@ipd-test.sn", "{bcrypt}$2b$10$dummy", "Diallo", "Awa",
                "admin@ipd-test.sn", "ADMIN_ETABLISSEMENT");

        when(repository.findByUsernameIgnoreCase("admin@ipd-test.sn"))
                .thenReturn(Optional.of(admin));
        when(etablissements.findByCodeIgnoreCase("IPD-TEST"))
                .thenReturn(Optional.of(new Etablissement("ipd-test", "Institut test", "ACTIF", Map.of())));

        UserDetails user = new DatabaseUserDetailsService(repository, etablissements)
                .loadUserByUsername("admin@ipd-test.sn");

        assertEquals("admin@ipd-test.sn", user.getUsername());
        assertTrue(user.isEnabled());
    }
}
