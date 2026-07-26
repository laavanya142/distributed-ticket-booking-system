# Notification Service (`notification-service`)

Stateless multi-channel notification dispatcher listening to Kafka domain events and delivering email, SMS, and push notifications.

## Port & Architecture
* **Port:** `8087`
* **Architecture:** Stateless event consumer (No relational DB required)
* **Events Consumed:** `ticket.booking.confirmed`, `ticket.booking.cancelled`, `ticket.notification.send`
