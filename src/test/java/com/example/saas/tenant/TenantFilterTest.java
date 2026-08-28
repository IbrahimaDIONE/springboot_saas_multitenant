package com.example.saas.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.saas.exception.InvalidTenantException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.List;

/** Tests pédagogiques du filtre qui fait le lien entre sécurité et multi-tenancy. */
@ExtendWith(MockitoExtension.class)
class TenantFilterTest {
    @Mock HandlerExceptionResolver resolver;
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
        when(request.getRequestURI()).thenReturn("/api/products");
        var principal =
                Jwt.withTokenValue("test-token")
                        .header("alg", "none")
                        .subject("client-a")
                        .claim("tenant_id", "tenant-a")
                        .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(principal, List.of()));

        doAnswer(
                        invocation -> {
                            assertThat(TenantContext.get()).isEqualTo("tenant-a");
                            return null;
                        })
                .when(chain)
                .doFilter(request, response);

        new TenantFilter(resolver).doFilter(request, response, chain);

        assertThat(TenantContext.get()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldRejectRequestWithoutTenantPrincipal() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/products");

        new TenantFilter(resolver).doFilter(request, response, chain);

        verify(resolver)
                .resolveException(
                        eq(request), eq(response), isNull(), any(InvalidTenantException.class));
        verifyNoInteractions(chain);
    }
}
