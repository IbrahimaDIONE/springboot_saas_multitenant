package com.example.saas.repository;

import com.example.saas.domain.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface EtudiantRepository extends JpaRepository<Etudiant, UUID> {

    Optional<Etudiant> findByUtilisateurId(UUID utilisateurId);
}