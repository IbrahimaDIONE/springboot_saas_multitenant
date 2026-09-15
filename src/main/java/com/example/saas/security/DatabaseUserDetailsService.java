package com.example.saas.security;

import com.example.saas.repository.TenantUserRepository;
import com.example.saas.repository.EtablissementRepository;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

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
                boolean active = etablissements.findByCode(user.getTenantId())
                    .map(etablissement -> "ACTIF".equals(etablissement.getStatut()))
                    .orElse(false);
                if (!active) {
                throw new DisabledException("Etablissement désactivé");
                }
                return TenantUserPrincipal.from(user);
            })
                // Même message pour un utilisateur absent afin de limiter l'énumération de comptes.
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants invalides"));
    }
}
