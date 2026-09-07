package com.example.saas.repository;

import com.example.saas.domain.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EtudiantRepository extends JpaRepository<Etudiant, UUID> {
    Optional<Etudiant> findByUtilisateur_Id(UUID utilisateurId);
}