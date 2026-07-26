package com.ticketbooking.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application entry point for the Event Catalog Microservice.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.ticketbooking.event", "com.ticketbooking.common"})
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
