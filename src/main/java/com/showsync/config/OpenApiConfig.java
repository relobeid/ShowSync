package com.showsync.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for ShowSync API documentation.
 * 
 * Provides comprehensive API documentation accessible at:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-17
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI showSyncOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShowSync API")
                        .description("AI-powered social media discovery platform for TV shows, movies, and books")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("ShowSync Development Team")
                                .email("ramezelobeid@icloud.com")
                                .url("https://github.com/relobeid/ShowSync"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token (without 'Bearer ' prefix)")));
    }
} 