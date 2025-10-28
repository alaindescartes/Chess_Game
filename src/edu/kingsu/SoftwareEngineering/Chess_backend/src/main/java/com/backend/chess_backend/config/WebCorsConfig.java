package com.backend.chess_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/**
 * Spring MVC configuration for Cross-Origin Resource Sharing (CORS) during development.
 * <p>
 * Allows a React/Next.js front-end running on {@code http://localhost:3000} (or {@code 127.0.0.1:3000})
 * to call this Spring Boot API from a different origin. This relaxes the browser's same-origin policy
 * for the configured routes and methods. In production, you should restrict the allowed origins to
 * your deployed front-end domain(s).
 * </p>
 *
 * @author Alain Uwishema
 * @since 0.1
 */
public class WebCorsConfig {
    @Bean
    /**
     * Configures global CORS mappings for the application.
     * <p>
     * Current policy:
     * </p>
     * <ul>
     *   <li>Paths: {@code /**} (all endpoints)</li>
     *   <li>Allowed origins: {@code http://localhost:3000}, {@code http://127.0.0.1:3000}</li>
     *   <li>Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS</li>
     *   <li>Headers: all</li>
     * </ul>
     *
     * <p>
     * Note: For production, narrow the origin list (or externalize to configuration) to avoid
     * unintentionally exposing your API cross-origin.
     * </p>
     *
     * @return a {@link WebMvcConfigurer} that registers the application's CORS rules
     */
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
                        .allowedMethods("GET","POST","PUT","DELETE","PATCH","OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
