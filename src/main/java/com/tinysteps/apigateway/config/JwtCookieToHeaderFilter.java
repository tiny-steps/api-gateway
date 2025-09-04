package com.tinysteps.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtCookieToHeaderFilter implements WebFilter {
    private static final String COOKIE_NAME = "jwt";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Logger logger = LoggerFactory.getLogger(JwtCookieToHeaderFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Attempt to get the JWT from the cookie
        HttpCookie jwtCookie = exchange.getRequest().getCookies().getFirst(COOKIE_NAME);

        // If the cookie is present, always use it to set the Authorization header.
        // This corrects the logic by overwriting any pre-existing, potentially malformed header.
        if (jwtCookie != null) {
            String jwt = jwtCookie.getValue();
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(AUTH_HEADER, BEARER_PREFIX + jwt)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        // If no JWT cookie is found, continue the filter chain without modification.
        return chain.filter(exchange);
    }
}
