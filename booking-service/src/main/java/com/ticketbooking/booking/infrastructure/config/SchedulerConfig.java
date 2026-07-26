package com.ticketbooking.booking.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring configuration enabling background scheduled tasks for outbox relay and expiration sweeper.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {}
