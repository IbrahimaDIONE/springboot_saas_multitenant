package com.example.saas.repository;

import com.example.saas.domain.TenantUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

/** Repository utilisé uniquement par Spring Security pour charger un compte. */
public interface TenantUserRepository extends JpaRepository<TenantUser, UUID> {
    Optional<TenantUser> findByUsernameIgnoreCase(String username);
}
