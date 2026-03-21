package com.prodpulse.prodpulse_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials — required for WebSocket + SockJS
        config.setAllowCredentials(true);

        // Allow specific origins with credentials
        config.setAllowedOriginPatterns(List.of("*"));

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow all methods
        config.addAllowedMethod("*");

        // Expose headers
        config.addExposedHeader("Authorization");

        // Apply to all endpoints including /ws/**
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}