package com.tintsteps.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/oauth2/**", "/.well-known/**")
                        .uri("lb://auth-service"))
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .uri("lb://user-service"))
                .build();
    }
}
