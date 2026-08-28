package com.example.saas.tenant;

/**
 * Abstraction du tenant courant. DIP : le service ignore ThreadLocal et le futur mécanisme
 * JWT/OIDC.
 */
public interface TenantProvider {
    String currentTenant();
}
