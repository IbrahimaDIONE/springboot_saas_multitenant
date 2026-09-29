package com.example.saas.repository;

import com.example.saas.domain.TenantUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

/** Repository utilisé uniquement par Spring Security pour charger un compte. */
public interface TenantUserRepository extends JpaRepository<TenantUser, UUID> {
    Optional<TenantUser> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("select u from TenantUser u where u.tenantId = :tenantId and u.role.code = :role order by u.nom, u.prenom")
    List<TenantUser> findAllByTenantIdAndRoleOrderByNomAscPrenomAsc(@Param("tenantId") String tenantId, @Param("role") String role);

    @Query("select u from TenantUser u where u.id = :id and u.tenantId = :tenantId and u.role.code = :role")
    Optional<TenantUser> findByIdAndTenantIdAndRole(@Param("id") UUID id, @Param("tenantId") String tenantId, @Param("role") String role);
}
