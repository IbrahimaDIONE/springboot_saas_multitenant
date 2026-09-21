package com.example.saas.repository;

import com.example.saas.domain.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    List<Consultation> findAllByTenantIdAndEtudiantIdOrderByDateConsultationDesc(String tenantId, UUID etudiantId);
}