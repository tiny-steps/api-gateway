package com.tintsteps.apigateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String authServerUrl;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Permit all auth-related and service discovery endpoints
                        .pathMatchers("/api/auth/**", "/oauth2/**", "/eureka/**").permitAll()
                        // All other requests must be authenticated
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .publicKey((RSAPublicKey) publicKey())
                        )
                );
        return http.build();
    }

    @Bean
    public PublicKey publicKey() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // The JWKS endpoint for Spring Authorization Server defaults to /oauth2/jwks
            String jwksResponse = restTemplate.getForObject(authServerUrl + "/oauth2/jwks", String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jwks = objectMapper.readTree(jwksResponse);
            JsonNode key = jwks.get("keys").get(0);

            String n = key.get("n").asText();
            String e = key.get("e").asText();

            RSAPublicKeySpec spec = new RSAPublicKeySpec(
                    new BigInteger(1, Base64.getUrlDecoder().decode(n)),
                    new BigInteger(1, Base64.getUrlDecoder().decode(e))
            );

            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (Exception e) {
            // Throw a runtime exception if the key can't be loaded at startup
            throw new RuntimeException("Failed to load public key from auth server", e);
        }
    }
}
