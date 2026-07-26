package com.ticketbooking.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for the User Microservice.
 */
@SpringBootApplication
public class UserServiceApplication {

    /**
     * Application main method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
