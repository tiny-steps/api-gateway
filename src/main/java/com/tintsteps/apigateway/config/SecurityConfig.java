package com.tintsteps.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/oauth2/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(spec -> spec.jwt(Customizer.withDefaults()));

        // Enforce statelessness by disabling the session-based security context repository.
        // This ensures that the security context is not saved between requests,
        // forcing re-authentication and allowing the TokenBlacklistFilter to work correctly.
        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http.build();
    }
}
