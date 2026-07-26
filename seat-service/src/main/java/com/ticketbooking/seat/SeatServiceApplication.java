package com.ticketbooking.seat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application entry point for the Seat & Show Inventory Microservice.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.ticketbooking.seat", "com.ticketbooking.common"})
public class SeatServiceApplication {

    /**
     * Application main method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(SeatServiceApplication.class, args);
    }
}
