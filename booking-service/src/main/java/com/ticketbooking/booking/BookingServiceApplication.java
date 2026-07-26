package com.ticketbooking.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for the Booking & Saga Orchestrator Microservice.
 */
@SpringBootApplication
public class BookingServiceApplication {

    /**
     * Application main method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
