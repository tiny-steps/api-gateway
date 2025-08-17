// In api-gateway/src/main/java/com/tintsteps/apigateway/config/SecurityConfig.java
package com.tintsteps.apigateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Import this class
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.WebFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Autowired
    private JwtCookieToHeaderFilter jwtCookieToHeaderFilter;

    @Bean
    // Change the order to run before Spring Security's filters
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter jwtCookieToHeaderWebFilter() {
        return jwtCookieToHeaderFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/auth/**", "/oauth2/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(spec -> spec.jwt(Customizer.withDefaults()));

        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http.build();
    }
}
