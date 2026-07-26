package com.ticketbooking.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for the reactive Spring Cloud API Gateway Service.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * Application main method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
