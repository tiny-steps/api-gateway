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
                // Auth Service routes
                .route("ts-auth-service-oauth", r -> r.path("/oauth2/**", "/.well-known/**")
                        .uri("lb://ts-auth-service"))
                .route("ts-auth-service-api", r -> r.path("/api/auth/**")
                        .uri("lb://ts-auth-service"))

                // User Service routes
                .route("ts-user-service", r -> r.path("/api/v1/users/**")
                        .uri("lb://ts-user-service"))

                // Address Service routes
                .route("ts-address-service", r -> r.path("/api/v1/addresses/**")
                        .uri("lb://ts-address-service"))

                // Doctor Service routes
                .route("ts-doctor-service-doctors", r -> r.path("/api/v1/doctors/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-awards", r -> r.path("/api/v1/awards/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-memberships", r -> r.path("/api/v1/memberships/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-registrations", r -> r.path("/api/v1/registrations/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-qualifications", r -> r.path("/api/v1/qualifications/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-specializations", r -> r.path("/api/v1/specializations/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-pricing", r -> r.path("/api/v1/pricing/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-organizations", r -> r.path("/api/v1/organizations/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-practices", r -> r.path("/api/v1/practices/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-photos", r -> r.path("/api/v1/photos/**")
                        .uri("lb://ts-doctor-service"))
                .route("ts-doctor-service-recommendations", r -> r.path("/api/v1/recommendations/**")
                        .uri("lb://ts-doctor-service"))

                // Patient Service routes
                .route("ts-patient-service-patients", r -> r.path("/api/v1/patients/**")
                        .uri("lb://ts-patient-service"))
                .route("ts-patient-service-medications", r -> r.path("/api/v1/patient-medications/**")
                        .uri("lb://ts-patient-service"))
                .route("ts-patient-service-addresses", r -> r.path("/api/v1/patient-addresses/**")
                        .uri("lb://ts-patient-service"))
                .route("ts-patient-service-emergency-contacts", r -> r.path("/api/v1/patient-emergency-contacts/**")
                        .uri("lb://ts-patient-service"))
                .route("ts-patient-service-medical-history", r -> r.path("/api/v1/patient-medical-history/**")
                        .uri("lb://ts-patient-service"))
                .route("ts-patient-service-insurance", r -> r.path("/api/v1/patient-insurance/**")
                        .uri("lb://ts-patient-service"))
                .route("ts-patient-service-appointments", r -> r.path("/api/v1/patient-appointments/**")
                        .uri("lb://ts-patient-service"))
                .route("ts-patient-service-health-summary", r -> r.path("/api/v1/patient-health-summary/**")
                        .uri("lb://ts-patient-service"))
                .route("ts-patient-service-advanced-search", r -> r.path("/api/v1/patient-advanced-search/**")
                        .uri("lb://ts-patient-service"))
                .route("ts-patient-service-allergies", r -> r.path("/api/v1/patient-allergies/**")
                        .uri("lb://ts-patient-service"))

                // Timing Service routes
                .route("ts-timing-service", r -> r.path("/api/v1/timing/**")
                        .uri("lb://ts-timing-service"))

                // Consultation Service routes
                .route("ts-consultation-service", r -> r.path("/api/v1/consultations/**")
                        .uri("lb://ts-consultation-service"))

                // Schedule Service routes
                .route("ts-schedule-service", r -> r.path("/api/v1/schedules/**")
                        .uri("lb://ts-schedule-service"))

                // Session Service routes
                .route("ts-session-service", r -> r.path("/api/v1/sessions/**")
                        .uri("lb://ts-session-service"))

                // Payment Service routes
                .route("ts-payment-service", r -> r.path("/api/v1/payments/**")
                        .uri("lb://ts-payment-service"))

                // Report Service routes
                .route("ts-report-service", r -> r.path("/api/v1/reports/**")
                        .uri("lb://ts-report-service"))

                // Notification Service routes
                .route("ts-notification-service", r -> r.path("/api/v1/notifications/**")
                        .uri("lb://ts-notification-service"))

            .build();
    }
}
