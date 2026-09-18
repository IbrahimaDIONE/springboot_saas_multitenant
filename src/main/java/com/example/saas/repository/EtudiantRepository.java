package com.example.saas.repository;

import com.example.saas.domain.Etudiant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EtudiantRepository extends JpaRepository<Etudiant, UUID> {
    List<Etudiant> findAllByUtilisateur_TenantIdOrderByUtilisateur_NomAscUtilisateur_PrenomAsc(
            String tenantId);

    Optional<Etudiant> findByIdAndUtilisateur_TenantId(UUID id, String tenantId);

    Optional<Etudiant> findByUtilisateur_Id(UUID utilisateurId);

    @Query("select e from Etudiant e where lower(e.utilisateur.username) = lower(:username)")
    Optional<Etudiant> findByUtilisateurUsername(@Param("username") String username);

    long countByUtilisateur_TenantId(String tenantId);
}