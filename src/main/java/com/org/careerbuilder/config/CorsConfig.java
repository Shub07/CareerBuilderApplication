package com.org.careerbuilder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Global CORS configuration for all endpoints
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",      // Vite development server
                        "http://localhost:3000",      // React development server
                        "http://localhost:5174",      // Alternative Vite port
                        "http://localhost:8080",      // Alternative development port
                        "http://127.0.0.1:5173"       // Local machine Vite
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // Specific CORS configuration for MyClass API endpoints
        registry.addMapping("/api/myclasses/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://localhost:5174",
                        "http://localhost:8080",
                        "http://127.0.0.1:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders(
                        "Content-Type",
                        "Authorization",
                        "X-Requested-With",
                        "Accept",
                        "Origin"
                )
                .exposedHeaders(
                        "Content-Type",
                        "Authorization",
                        "X-Total-Count",
                        "X-Page-Number"
                )
                .allowCredentials(true)
                .maxAge(3600);

        // Specific CORS configuration for Student API endpoints (dependency for MyClass)
        registry.addMapping("/api/students/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://localhost:5174",
                        "http://localhost:8080",
                        "http://127.0.0.1:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // Specific CORS configuration for Subject API endpoints (dependency for MyClass)
        registry.addMapping("/api/subjects/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://localhost:5174",
                        "http://localhost:8080",
                        "http://127.0.0.1:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
