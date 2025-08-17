package com.tintsteps.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/auth/**", "/oauth2/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(new CookieAndHeaderBearerTokenResolver())
                );

        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http.build();
    }
}

/**
 * A custom resolver that checks for a bearer token in the "Authorization" header first,
 * and if not found, falls back to a cookie named "jwt".
 */
class CookieAndHeaderBearerTokenResolver implements ServerBearerTokenResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        // First, try to resolve from the Authorization header
        Mono<String> fromHeader = Mono.justOrEmpty(
                Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("Authorization"))
                        .filter(header -> header.startsWith("Bearer "))
                        .map(header -> header.substring(7))
        );

        // If not found in header, try to resolve from the "jwt" cookie
        return fromHeader.switchIfEmpty(Mono.defer(() -> {
            HttpCookie cookie = exchange.getRequest().getCookies().getFirst("jwt");
            if (cookie != null) {
                return Mono.just(cookie.getValue());
            }
            return Mono.empty();
        }));
    }
}
