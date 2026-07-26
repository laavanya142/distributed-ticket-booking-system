package com.ticketbooking.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot 3 application class for Auth Service.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.ticketbooking.auth", "com.ticketbooking.common"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
