package com.example.saas.security;

import com.example.saas.domain.TenantUser;

import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal authentifié contenant le tenant fiable de l'utilisateur. TenantFilter lit cette valeur
 * au lieu de faire confiance à un header client.
 */
public record TenantUserPrincipal(
        String username,
        String password,
        String tenantId,
        boolean enabled,
        Collection<? extends GrantedAuthority> authorities)
        implements UserDetails {

    public static TenantUserPrincipal from(TenantUser user) {
        return new TenantUserPrincipal(
                user.getUsername(),
                user.getPasswordHash(),
                user.getTenantId(),
                user.isEnabled(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
