# Booking Service (`booking-service`)

Checkout Saga Orchestrator managing order creation, transactional outbox persistence, payment coordination, and booking state transitions.

## Port & Database
* **Port:** `8085`
* **Database:** `booking_db` (PostgreSQL)
* **Events:** Apache Kafka (`ticket.booking.created`, `ticket.booking.confirmed`, `ticket.booking.cancelled`)
