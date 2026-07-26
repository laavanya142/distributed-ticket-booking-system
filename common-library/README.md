# Common Library Module

This module contains **strictly infrastructure and cross-cutting concerns** shared across the 9 microservices.

## Scope & Governance
* **ALLOWED:** Common DTOs (`ApiResponse`, `ErrorResponse`), W3C correlation ID filters, OpenTelemetry configuration utilities, global exception handlers, HTTP header constants, base Kafka event payloads, and shared enums.
* **FORBIDDEN:** Zero business logic, repository interfaces, domain models, JPA entities, or service-specific DTOs are permitted in this library. Every microservice must independently own its bounded domain model.
