package com.example.saas.config;

import com.example.saas.tenant.TenantFilter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;

/**
 * Empêche Spring Boot d'enregistrer TenantFilter comme filtre Servlet autonome. Il doit s'exécuter
 * uniquement dans la chaîne Security, après l'authentification.
 */
@Configuration
public class TenantFilterConfig {
    @Bean
    FilterRegistrationBean<TenantFilter> disableAutomaticTenantFilterRegistration(
            TenantFilter filter) {
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
