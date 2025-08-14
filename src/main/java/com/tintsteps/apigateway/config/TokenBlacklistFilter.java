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

@Component
@Slf4j
public class TokenBlacklistFilter implements GlobalFilter, Ordered {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // First, try to get the token from the "jwt" cookie
        HttpCookie jwtCookie = exchange.getRequest().getCookies().getFirst("jwt");
        String jwt = null;

        if (jwtCookie != null) {
            jwt = jwtCookie.getValue();
        } else {
            // If the cookie is not present, fall back to the Authorization header
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.toLowerCase().startsWith(BEARER_PREFIX.toLowerCase())) {
                jwt = authHeader.substring(BEARER_PREFIX.length());
            }
        }

        if (jwt != null) {
            String key = BLACKLIST_PREFIX + jwt;
            log.info("Checking if token is blacklisted with key: {}", key);

            return reactiveRedisTemplate.hasKey(key)
                    .flatMap(isBlacklisted -> {
                        log.info("Is token with key '{}' blacklisted? {}", key, isBlacklisted);
                        if (Boolean.TRUE.equals(isBlacklisted)) {
                            log.warn("Blacklisted token received. Rejecting request.");
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    });
        }

        // If no token is found in either the cookie or the header, continue the filter chain
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run before Spring Security's authentication filters
        return -100;
    }
}
