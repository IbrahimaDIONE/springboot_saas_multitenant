package com.example.saas.repository;

import com.example.saas.domain.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

/**
 * Les méthodes destinées au métier exigent tenantId. N'utilisez jamais findById(id) dans
 * ProductService : il ignorerait l'isolation.
 */
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findAllByTenantIdOrderByName(String tenantId);

        @Query("""
                        select p from Product p
                        where p.tenantId = :tenantId
                            and (:categoryId is null or p.category.id = :categoryId)
                            and (lower(p.name) like lower(concat('%', :search, '%'))
                                     or lower(p.category.name) like lower(concat('%', :search, '%')))
                        order by p.name
                        """)
        List<Product> searchByTenantId(
                        @Param("tenantId") String tenantId,
                        @Param("search") String search,
                        @Param("categoryId") UUID categoryId);

    Optional<Product> findByIdAndTenantId(UUID id, String tenantId);
}
