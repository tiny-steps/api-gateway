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
            .route("auth-service-oauth", r -> r.path("/oauth2/**", "/.well-known/**")
                .uri("lb://auth-service"))
            .route("auth-service-api", r -> r.path("/api/auth/**")
                .uri("lb://auth-service"))
            .route("ts-user-service", r -> r.path("/api/v1/users/**")
                .uri("lb://ts-user-service"))
            .build();
    }
}
