# Analytics Service (`analytics-service`)

Consumes domain events across Kafka to calculate real-time ticket velocity, popular shows, and platform revenue metrics.

## Port & Database
* **Port:** `8088`
* **Database:** `analytics_db` (PostgreSQL)
* **Events Consumed:** `ticket.booking.created`, `ticket.booking.confirmed`, `ticket.payment.completed`
