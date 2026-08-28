package com.example.saas.tenant;

/**
 * Stockage du tenant pendant une requête. ThreadLocal doit toujours être nettoyé dans un finally
 * pour éviter une fuite lorsque le serveur réutilise le même thread.
 */
public final class TenantContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(String tenant) {
        CURRENT.set(tenant);
    }

    public static String get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
