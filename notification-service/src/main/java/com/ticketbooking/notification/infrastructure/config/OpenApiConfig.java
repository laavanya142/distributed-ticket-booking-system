package com.ticketbooking.notification.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 configuration for Notification Service API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Dispatch Service API")
                        .description(
                                "Event-driven multi-channel notification dispatcher (Email, SMS, Push) driven by Kafka topics with read-only notification history query endpoints.")
                        .version("1.0.0")
                        .contact(new Contact().name("Engineering Team").email("support@ticketbooking.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
