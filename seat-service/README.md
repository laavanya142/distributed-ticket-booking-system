# Seat & Show Inventory Service (`seat-service`)

Manages real-time seating layouts, pricing tiers, availability bitmaps in Redis, and atomic seat locks via Lua scripting.

## Port & Database
* **Port:** `8084`
* **Database:** `seat_db` (PostgreSQL)
* **Lock Store:** Redis Cluster
* **Events:** Apache Kafka (`ticket.seat.locked`, `ticket.seat.unlocked`)
