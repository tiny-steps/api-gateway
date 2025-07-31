package com.tintsteps.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    @SuppressWarnings("all")
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                // Allow unauthenticated access to auth endpoints and token endpoints
                .pathMatchers("/api/auth/**", "/oauth2/**", "/.well-known/**").permitAll()
                // All other requests must be authenticated
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .build();
    }
}
