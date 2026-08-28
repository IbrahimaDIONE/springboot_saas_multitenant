package com.example.saas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Configuration transversale : active les dates d'audit de toutes les entités. */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {}
