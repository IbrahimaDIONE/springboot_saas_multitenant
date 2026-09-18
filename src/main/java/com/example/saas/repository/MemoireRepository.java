package com.example.saas.repository;

import com.example.saas.domain.Memoire;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface MemoireRepository extends JpaRepository<Memoire, UUID> {
    List<Memoire> findAllByTenantIdAndActifTrueOrderByAnneeDesc(String tenantId);

    List<Memoire> findAllByTenantIdAndActifFalseOrderByAnneeDesc(String tenantId);

    @Query("""
            select m from Memoire m
            where m.tenantId = :tenantId
              and m.actif = true
              and (:filiereId is null or m.filiere.id = :filiereId)
              and (:niveauId is null or m.niveau.id = :niveauId)
              and (:annee is null or m.annee = :annee)
              and (lower(m.titre) like lower(concat('%', :search, '%'))
                   or lower(m.auteur) like lower(concat('%', :search, '%'))
                   or lower(m.resume) like lower(concat('%', :search, '%')))
            order by m.annee desc, m.titre
            """)
    List<Memoire> searchByTenantId(
            @Param("tenantId") String tenantId,
            @Param("search") String search,
            @Param("filiereId") UUID filiereId,
            @Param("niveauId") UUID niveauId,
            @Param("annee") Integer annee);

    Optional<Memoire> findByIdAndTenantId(UUID id, String tenantId);
}