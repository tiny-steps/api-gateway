// In api-gateway/src/main/java/com/tintsteps/apigateway/config/SecurityConfig.java
package com.tintsteps.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Import this class
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.WebFilter;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
    @Autowired
    private JwtCookieToHeaderFilter jwtCookieToHeaderFilter;

    @Bean
    // Change the order to run before Spring Security's filters
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter jwtCookieToHeaderWebFilter() {
        return jwtCookieToHeaderFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/auth/**", "/oauth2/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(spec -> spec.jwt(jwt -> jwt
                        .jwtDecoder(jwtDecoder())));

        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return new ReactiveJwtDecoder() {
            @Override
            public Mono<Jwt> decode(String token) throws Exception {
                log.info("🔐 JWT Decoder: Attempting to decode token: {}",
                        token.substring(0, Math.min(50, token.length())) + "...");

                try {
                    // Try to create a decoder for the issuer URI
                    String issuerUri = "http://ts-auth-service:8081";
                    log.info("🔐 JWT Decoder: Using issuer URI: {}", issuerUri);

                    ReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri).build();
                    return decoder.decode(token)
                            .doOnSuccess(jwt -> {
                                log.info("✅ JWT Decoder: Successfully decoded JWT. Subject: {}, Claims: {}",
                                        jwt.getSubject(), jwt.getClaims());
                            })
                            .doOnError(error -> {
                                log.error("❌ JWT Decoder: Failed to decode JWT: {} - {}",
                                        error.getClass().getSimpleName(), error.getMessage());
                            });
                } catch (Exception e) {
                    log.error("❌ JWT Decoder: Exception during decoder creation: {} - {}",
                            e.getClass().getSimpleName(), e.getMessage());
                    return Mono.error(e);
                }
            }
        };
    }
}
