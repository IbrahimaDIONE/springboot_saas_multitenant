package com.example.saas.security;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.saas.domain.TenantUser;
import com.example.saas.repository.EtablissementRepository;
import com.example.saas.repository.TenantUserRepository;

/**
 * Adaptateur entre la table app_users et Spring Security. SRP : cette classe charge un compte ;
 * elle ne décide pas des droits métier.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final TenantUserRepository repository;
    private final EtablissementRepository etablissements;

    public DatabaseUserDetailsService(
            TenantUserRepository repository, EtablissementRepository etablissements) {
        this.repository = repository;
        this.etablissements = etablissements;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository
                .findByUsernameIgnoreCase(username)
            .map(user -> {
                boolean active = isTenantActive(user);
                if (!active) {
                throw new DisabledException("Etablissement désactivé");
                }
                if (user.getRole() == null || user.getRole().isBlank()) {
                    throw new DisabledException("Rôle invalide");
                }
                return TenantUserPrincipal.from(user);
            })
                // Même message pour un utilisateur absent afin de limiter l'énumération de comptes.
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants invalides"));
    }

    private boolean isTenantActive(TenantUser user) {
        if ("ADMIN_PLATEFORME".equalsIgnoreCase(user.getRole())
                || "tenant-platform".equalsIgnoreCase(user.getTenantId())) {
            return true;
        }

        return etablissements.findByCode(institutionCode(user.getTenantId()))
                .map(etablissement -> "ACTIF".equals(etablissement.getStatut()))
                .orElse(false);
    }

    private String institutionCode(String tenantId) {
        String prefix = "tenant-";
        String code = tenantId.regionMatches(true, 0, prefix, 0, prefix.length())
                ? tenantId.substring(prefix.length())
                : tenantId;
        return code.toUpperCase(java.util.Locale.ROOT);
    }
}
