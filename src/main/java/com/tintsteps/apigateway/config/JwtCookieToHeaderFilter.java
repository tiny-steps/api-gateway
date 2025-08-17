package com.tintsteps.apigateway.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtCookieToHeaderFilter implements WebFilter {
    private static final String COOKIE_NAME = "jwt";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Logger logger = LoggerFactory.getLogger(JwtCookieToHeaderFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        logger.info("JwtCookieToHeaderFilter invoked");
        logger.info("Incoming cookies: {}", exchange.getRequest().getCookies());
        logger.info("Incoming headers: {}", exchange.getRequest().getHeaders());
        List<String> cookies = exchange.getRequest().getCookies().containsKey(COOKIE_NAME)
                ? exchange.getRequest().getCookies().get(COOKIE_NAME).stream().map(c -> c.getValue()).toList()
                : List.of();
        if (!cookies.isEmpty() && exchange.getRequest().getHeaders().getFirst(AUTH_HEADER) == null) {
            String jwt = cookies.get(0);
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(AUTH_HEADER, BEARER_PREFIX + jwt)
                    .build();
            logger.info("Added Authorization header: {}{}", BEARER_PREFIX, jwt);
            logger.info("Mutated headers: {}", mutatedRequest.getHeaders());
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }
        return chain.filter(exchange);
    }
}
