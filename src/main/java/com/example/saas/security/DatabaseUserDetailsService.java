package com.example.saas.security;

import com.example.saas.repository.TenantUserRepository;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Adaptateur entre la table app_users et Spring Security. SRP : cette classe charge un compte ;
 * elle ne décide pas des droits métier.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final TenantUserRepository repository;

    public DatabaseUserDetailsService(TenantUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository
                .findByUsernameIgnoreCase(username)
                .map(TenantUserPrincipal::from)
                // Même message pour un utilisateur absent afin de limiter l'énumération de comptes.
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants invalides"));
    }
}
