package com.ticketbooking.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 / Swagger UI configuration for Auth Service.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .description(
                                "Authentication and Authorization microservice issuing RS256 JWTs and managing refresh token rotation")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("https://spring.io")));
    }
}
