package com.tintsteps.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class TokenBlacklistFilter implements GlobalFilter, Ordered {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String BEARER_PREFIX = "Bearer ";

    // Define the list of public endpoints that do not require authentication.
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password" // Assuming you will add this endpoint
    );

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String jwt = extractToken(exchange);

        // If a token is present, always check the blacklist.
        if (jwt != null) {
            String key = BLACKLIST_PREFIX + jwt;
            log.info("Token found for path: {}. Checking blacklist with key: {}", path, key);

            return reactiveRedisTemplate.hasKey(key)
                    .flatMap(isBlacklisted -> {
                        if (Boolean.TRUE.equals(isBlacklisted)) {
                            log.warn("Blacklisted token received. Rejecting request for key: {}", key);
                            return rejectRequest(exchange, HttpStatus.UNAUTHORIZED);
                        }
                        // Token is not blacklisted, let it pass for further verification.
                        return chain.filter(exchange);
                    });
        }

        // NO TOKEN FOUND. Now, check if the endpoint is public.
        if (isPublicEndpoint(path)) {
            log.info("No token found, but path '{}' is public. Allowing request.", path);
            return chain.filter(exchange);
        }

        // No token found for a protected endpoint. Reject the request.
        log.warn("No token found for protected path: '{}'. Rejecting request.", path);
        return rejectRequest(exchange, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Extracts the JWT from the cookie or Authorization header.
     */
    private String extractToken(ServerWebExchange exchange) {
        HttpCookie jwtCookie = exchange.getRequest().getCookies().getFirst("jwt");
        if (jwtCookie != null) {
            return jwtCookie.getValue();
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.toLowerCase().startsWith(BEARER_PREFIX.toLowerCase())) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Checks if the requested path is in the public endpoints whitelist.
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    /**
     * Helper method to reject a request with a specific HTTP status.
     */
    private Mono<Void> rejectRequest(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }


    @Override
    public int getOrder() {
        // Run this filter before Spring Security's authentication filters.
        return -100;
    }
}
