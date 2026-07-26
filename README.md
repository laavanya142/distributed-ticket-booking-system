# Distributed Ticket Booking System (BookMyShow / Ticketmaster Clone)

A production-grade, highly scalable, and fault-tolerant distributed ticket booking system built with **Java 21**, **Spring Boot 3**, and **Apache Kafka**. Designed to handle **100,000 concurrent active users**, **10,000 booking requests per second**, and guarantee **zero double bookings** during high-density flash sales.

---

## 🏛️ Architecture & Tech Stack

* **Language:** Java 21 (with Project Loom / Virtual Threads enabled via `spring.threads.virtual.enabled=true`)
* **Framework:** Spring Boot 3.3+, Spring Cloud Gateway, Resilience4j
* **Messaging & Events:** Apache Kafka (Outbox / Inbox patterns, Dead Letter Queues)
* **Relational Storage:** PostgreSQL 16 (Strict Database per Service isolation)
* **In-Memory & Locking:** Redis 7 Cluster (Atomic Lua Scripting, Redlock, Token Bucket rate limiting)
* **Search & Logging:** OpenSearch (Catalog full-text search & centralized JSON logging)
* **Observability:** OpenTelemetry (W3C Trace Context), Micrometer, Prometheus, Grafana, Zipkin/Jaeger
* **Code Quality:** Spotless (Palantir Java Format), Checkstyle, MapStruct, Lombok

---

## 📦 Microservice Inventory & Clean Architecture

This repository is structured as a Maven multi-module project:

```text
distributed-ticket-booking/
├── common-library/          # Pure infrastructure utilities (DTOs, OTel, Correlation ID, Exceptions)
├── api-gateway/             # Reactive Spring Cloud Gateway (Netty, Redis Rate Limiter, JWT Auth)
├── auth-service/            # User authentication, RS256 JWT issuance, and refresh token rotation
├── user-service/            # User profiles and GDPR preferences
├── event-service/           # Event catalog, venue layouts, and OpenSearch full-text discovery
├── seat-service/            # Real-time seating inventory, Redis availability bitmaps, and atomic locks
├── booking-service/         # Checkout Saga Orchestrator, Transactional Outbox, and order management
├── payment-service/         # Mock payment gateway integration with embedded Chaos Engineering
├── notification-service/    # Multi-channel transactional notifications (SMS, Email, Push)
└── analytics-service/       # Real-time event velocity and revenue reporting
```

Every microservice strictly follows **Clean / Hexagonal Architecture**:
* `domain`: Pure domain models, entities, value objects, exceptions, and ports (Zero framework imports).
* `application`: Use case orchestrators, Saga step definitions, and DTOs.
* `infrastructure`: Database adapters (JPA/Hibernate), Kafka messaging adapters, and external integrations.
* `interfaces`: REST APIs, web filters, and Kafka event consumers.
* `config`: Spring Boot configuration and OpenAPI bean definitions.

---

## 🚀 Prerequisites & Getting Started

### Prerequisites
* **JDK 21** (Eclipse Temurin or OpenJDK 21+)
* **Apache Maven 3.9+**
* **Docker & Docker Compose** (for local infrastructure orchestration)

### 1. Build the Entire Project
To compile all modules, run annotation processors (MapStruct/Lombok), and verify Spotless/Checkstyle quality gates:
```bash
mvn clean package -DskipTests
```
To automatically apply Palantir code formatting across all Java files:
```bash
mvn spotless:apply
```

### 2. Start Local Infrastructure
Use the provided shell scripts or Docker Compose to boot PostgreSQL, Redis Cluster, Kafka, Zookeeper, Prometheus, Grafana, and OpenTelemetry Collector:
```bash
./scripts/start.sh
# OR manually:
docker compose up -d
```
* **PostgreSQL:** `localhost:5432` (Auto-creates 7 isolated databases: `auth_db`, `user_db`, `event_db`, `seat_db`, `booking_db`, `payment_db`, `analytics_db`)
* **Redis Cluster:** `localhost:6379`
* **Kafka:** `localhost:9092`
* **Prometheus:** `http://localhost:9090`
* **Grafana:** `http://localhost:3000` (Default credentials: `admin` / `admin`)
* **Zipkin Tracing:** `http://localhost:9411`

### 3. Run Microservices
Each microservice can be started via Maven Spring Boot plugin or directly from your IDE:
```bash
# Example: Starting API Gateway
cd api-gateway && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 📚 Documentation
* Detailed Architecture Specification: [`ARCHITECTURE.md`](./ARCHITECTURE.md)
* Architectural Decisions & ADRs: [`docs/architecture/README.md`](./docs/architecture/README.md)
* API Catalog & Swagger Definitions: [`docs/api/README.md`](./docs/api/README.md)
* System & Sequence Diagrams: [`docs/diagrams/README.md`](./docs/diagrams/README.md)
