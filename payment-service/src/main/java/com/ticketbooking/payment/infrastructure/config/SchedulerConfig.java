package com.ticketbooking.payment.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring configuration enabling background scheduled tasks for outbox event publishing.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {}
