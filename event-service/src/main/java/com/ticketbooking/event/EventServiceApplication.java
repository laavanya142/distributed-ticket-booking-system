package com.ticketbooking.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for the Event Catalog Microservice.
 */
@SpringBootApplication
public class EventServiceApplication {

    /**
     * Application main method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
