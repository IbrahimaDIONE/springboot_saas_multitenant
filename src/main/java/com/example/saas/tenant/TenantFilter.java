package com.example.saas.tenant;

import com.example.saas.exception.InvalidTenantException;
import com.example.saas.repository.TenantUserRepository;
import com.example.saas.security.SecurityErrorWriter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/** Extrait tenant_id du JWT signé après sa validation cryptographique. */
@Component
public class TenantFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver resolver;
    private final TenantUserRepository users;
    private final SecurityErrorWriter securityErrorWriter;

    public TenantFilter(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver r,
            TenantUserRepository users,
            SecurityErrorWriter securityErrorWriter) {
        resolver = r;
        this.users = users;
        this.securityErrorWriter = securityErrorWriter;
    }

    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null
                || !(a.getPrincipal() instanceof Jwt jwt)
                || jwt.getClaimAsString("tenant_id") == null) {
            resolver.resolveException(
                    req, res, null, new InvalidTenantException("tenant_id absent du token"));
            return;
        }
        boolean accountIsUsable =
                users.findByUsernameIgnoreCase(jwt.getSubject())
                        .filter(user -> user.isEnabled()
                                && user.getTenantId().equals(jwt.getClaimAsString("tenant_id")))
                        .isPresent();
        if (!accountIsUsable) {
            securityErrorWriter.write(
                    req,
                    res,
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Le compte est désactivé ou la session n’est plus autorisée");
            return;
        }
        try {
            TenantContext.set(jwt.getClaimAsString("tenant_id"));
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }

    protected boolean shouldNotFilter(HttpServletRequest r) {
        return r.getRequestURI().startsWith("/actuator/")
                || r.getRequestURI().startsWith("/api/auth/");
    }
}
