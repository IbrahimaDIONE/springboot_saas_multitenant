package com.example.saas.repository;

import com.example.saas.domain.Ouvrage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface OuvrageRepository extends JpaRepository<Ouvrage, UUID> {
    List<Ouvrage> findAllByTenantIdOrderByTitre(String tenantId);

    @Query("""
            select o from Ouvrage o
            where o.tenantId = :tenantId
              and (:filiereId is null or o.filiere.id = :filiereId)
              and (:niveauId is null or o.niveau.id = :niveauId)
              and (lower(o.titre) like lower(concat('%', :search, '%'))
                   or lower(o.auteur) like lower(concat('%', :search, '%'))
                   or lower(o.resume) like lower(concat('%', :search, '%')))
            order by o.titre
            """)
    List<Ouvrage> searchByTenantId(
            @Param("tenantId") String tenantId,
            @Param("search") String search,
            @Param("filiereId") UUID filiereId,
            @Param("niveauId") UUID niveauId);

    Optional<Ouvrage> findByIdAndTenantId(UUID id, String tenantId);
}