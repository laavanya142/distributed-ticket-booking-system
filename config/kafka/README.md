# Kafka Cluster & Topic Provisioning

The local Docker Compose environment provisions **Confluent Apache Kafka 7.5.0** alongside Zookeeper.

## Connection Strings
* **Internal Docker Network (between services):** `kafka:29092`
* **Host Machine / Local IDE:** `localhost:9092`

## Standard Topic Catalog
Topics are auto-created or provisioned via Spring Kafka admin beans during service startup:
* `ticket.seat.locked`
* `ticket.seat.unlocked`
* `ticket.booking.created`
* `ticket.payment.requested`
* `ticket.payment.completed`
* `ticket.payment.failed`
* `ticket.booking.confirmed`
* `ticket.booking.cancelled`
* `ticket.notification.send`
