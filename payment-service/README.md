# Payment Service (`payment-service`)

Mock payment gateway integration simulating external credit card processing, webhook callbacks, and configurable Chaos Engineering parameters.

## Chaos Engineering Controls
Configurable via Spring Actuator or configuration properties:
* Artificial network latency delays (0 - 5000ms)
* Simulated gateway error rates (e.g., 10% failure rate)
* Duplicate webhook generation for testing idempotency
* Random process crashing simulation

## Port & Database
* **Port:** `8086`
* **Database:** `payment_db` (PostgreSQL)
* **Events:** Apache Kafka (`ticket.payment.requested`, `ticket.payment.completed`, `ticket.payment.failed`)
