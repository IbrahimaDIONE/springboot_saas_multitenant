package com.example.saas.config;

import com.example.saas.security.SecurityErrorWriter;
import com.example.saas.tenant.TenantFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/** API SaaS stateless protégée par JWT Bearer. */
@Configuration
public class SecurityConfig {
    private SecretKey key(String s) {
        return new SecretKeySpec(s.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${app.security.jwt.secret}") String s) {
        return NimbusJwtEncoder.withSecretKey(key(s)).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${app.security.jwt.secret}") String s) {
        return NimbusJwtDecoder.withSecretKey(key(s)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, TenantFilter filter, SecurityErrorWriter writer) throws Exception {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        a ->
                                a.requestMatchers(
                                                "/api/auth/**",
                                                "/actuator/health",
                                                "/actuator/info")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .oauth2ResourceServer(
                        o ->
                                o.jwt(j -> j.jwtAuthenticationConverter(converter))
                                        .authenticationEntryPoint(
                                                (req, res, e) ->
                                                        writer.write(
                                                                req,
                                                                res,
                                                                HttpStatus.UNAUTHORIZED,
                                                                "UNAUTHORIZED",
                                                                "Token absent, invalide ou"
                                                                    + " expiré")))
                .exceptionHandling(
                        e ->
                                e.accessDeniedHandler(
                                        (req, res, x) ->
                                                writer.write(
                                                        req,
                                                        res,
                                                        HttpStatus.FORBIDDEN,
                                                        "FORBIDDEN",
                                                        "Accès refusé")))
                .addFilterAfter(filter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
