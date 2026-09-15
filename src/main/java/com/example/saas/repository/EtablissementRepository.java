package com.example.saas.repository;

import com.example.saas.domain.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface EtablissementRepository extends JpaRepository<Etablissement, UUID> {
    Optional<Etablissement> findByCode(String code);
    boolean existsByCode(String code);
}