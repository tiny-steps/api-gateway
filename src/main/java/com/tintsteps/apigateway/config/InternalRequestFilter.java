package com.tintsteps.apigateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

@Configuration
public class InternalRequestFilter {

    // Define your secret key. In a real app, this should come from a secure config server or environment variable.
    private final String internalSecret = "902ffc52f4ab670361e543008a5f8e86005edadfbc1df36f5cc375b433448aff";

    @Bean
    @Order(-1) // Run this filter before any routing happens.
    public GlobalFilter addInternalSecretHeader() {
        return (exchange, chain) -> {
            // Create a new request with the secret header
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Internal-Secret", internalSecret)
                    .build();

            // Create a new exchange with the mutated request
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            return chain.filter(mutatedExchange);
        };
    }
}
