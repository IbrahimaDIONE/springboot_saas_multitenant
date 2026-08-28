package com.example.saas.tenant;

import com.example.saas.exception.InvalidTenantException;

import org.springframework.stereotype.Component;

/** Adaptateur actuel basé sur TenantContext ; il pourra être remplacé sans modifier le service. */
@Component
public class CurrentTenantProvider implements TenantProvider {
    @Override
    public String currentTenant() {
        String tenant = TenantContext.get();
        if (tenant == null) {
            throw new InvalidTenantException("Aucun tenant n'est associé à la requête");
        }
        return tenant;
    }
}
