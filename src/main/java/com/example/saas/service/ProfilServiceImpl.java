package com.example.saas.service;

import com.example.saas.domain.TenantUser;
import com.example.saas.dto.ProfilRequest;
import com.example.saas.dto.ProfilResponse;
import com.example.saas.exception.ResourceNotFoundException;
import com.example.saas.repository.TenantUserRepository;
import com.example.saas.tenant.TenantProvider;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfilServiceImpl implements ProfilService {
    private final TenantUserRepository users;
    private final TenantProvider tenant;

    public ProfilServiceImpl(TenantUserRepository users, TenantProvider tenant) {
        this.users = users;
        this.tenant = tenant;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfilResponse get(String username) {
        return toResponse(findCurrentUser(username));
    }

    @Override
    public ProfilResponse update(String username, ProfilRequest request) {
        TenantUser user = findCurrentUser(username);
        user.setNom(request.nom());
        user.setPrenom(request.prenom());
        user.setEmail(request.email());
        return toResponse(user);
    }

    private TenantUser findCurrentUser(String username) {
        TenantUser user =
                users.findByUsernameIgnoreCase(username)
                        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        if (!user.getTenantId().equals(tenant.currentTenant())) {
            throw new ResourceNotFoundException("Utilisateur introuvable");
        }
        return user;
    }

    private ProfilResponse toResponse(TenantUser user) {
        return new ProfilResponse(
                user.getUsername(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole(),
                user.getTenantId());
    }
}
