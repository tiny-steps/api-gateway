package com.tintsteps.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TokenBlacklistFilter implements GlobalFilter, Ordered {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        final String bearerPrefix = "Bearer ";

        // Perform a case-insensitive check for the "Bearer " prefix.
        if (authHeader != null && authHeader.toLowerCase().startsWith(bearerPrefix.toLowerCase())) {
            String jwt = authHeader.substring(bearerPrefix.length());
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

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Run before Spring Security's authentication filters
        return -100;
    }
}
