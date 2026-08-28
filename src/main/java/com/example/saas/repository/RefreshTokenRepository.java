package com.example.saas.repository;

import com.example.saas.domain.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
