package com.example.saas.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.saas.exception.InvalidTenantException;
import com.example.saas.domain.TenantUser;
import com.example.saas.repository.TenantUserRepository;
import com.example.saas.security.SecurityErrorWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;
import java.util.Optional;

/** Tests pédagogiques du filtre qui fait le lien entre sécurité et multi-tenancy. */
@ExtendWith(MockitoExtension.class)
class TenantFilterTest {
    @Mock HandlerExceptionResolver resolver;
    @Mock TenantUserRepository users;
    @Mock SecurityErrorWriter securityErrorWriter;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain chain;

    @AfterEach
    void cleanSecurityContexts() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void shouldExposeAuthenticatedTenantDuringRequestThenClearIt() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/ouvrages");
        var principal =
                Jwt.withTokenValue("test-token")
                        .header("alg", "none")
                        .subject("client-a")
                        .claim("tenant_id", "tenant-a")
                        .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(principal, List.of()));
        TenantUser user =
                TenantUser.newAdminEtablissement(
                        "tenant-a", "client-a", "{bcrypt}hash", "Diop", "Awa", "awa@example.com");
        when(users.findByUsernameIgnoreCase("client-a")).thenReturn(Optional.of(user));

        doAnswer(
                        invocation -> {
                            assertThat(TenantContext.get()).isEqualTo("tenant-a");
                            return null;
                        })
                .when(chain)
                .doFilter(request, response);

        new TenantFilter(resolver, users, securityErrorWriter).doFilter(request, response, chain);

        assertThat(TenantContext.get()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldRejectRequestWithoutTenantPrincipal() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/ouvrages");

        new TenantFilter(resolver, users, securityErrorWriter).doFilter(request, response, chain);

        verify(resolver)
                .resolveException(
                        eq(request), eq(response), isNull(), any(InvalidTenantException.class));
        verifyNoInteractions(chain);
    }

    @Test
    void shouldRejectAccessTokenForDisabledAccount() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/ouvrages");
        var principal =
                Jwt.withTokenValue("test-token")
                        .header("alg", "none")
                        .subject("client-a")
                        .claim("tenant_id", "tenant-a")
                        .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(principal, List.of()));
        TenantUser user =
                TenantUser.newEtudiant(
                        "tenant-a", "client-a", "{bcrypt}hash", "Diop", "Awa", "awa@example.com");
        when(users.findByUsernameIgnoreCase("client-a")).thenReturn(Optional.of(user));

        new TenantFilter(resolver, users, securityErrorWriter).doFilter(request, response, chain);

        verify(securityErrorWriter)
                .write(
                        request,
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHORIZED",
                        "Le compte est désactivé ou la session n’est plus autorisée");
        verifyNoInteractions(chain);
        verifyNoInteractions(resolver);
    }
}
