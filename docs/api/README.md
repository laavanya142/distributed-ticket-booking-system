# API Catalog & OpenAPI Specifications

Every microservice in the Distributed Ticket Booking System exposes OpenAPI 3.0 OpenAPI specifications generated dynamically via `springdoc-openapi`.

## Local Swagger UI Endpoints (When running in dev profile)
* **API Gateway (Aggregated UI):** `http://localhost:8080/swagger-ui.html`
* **Auth Service:** `http://localhost:8081/swagger-ui.html`
* **User Service:** `http://localhost:8082/swagger-ui.html`
* **Event Catalog Service:** `http://localhost:8083/swagger-ui.html`
* **Seat & Show Service:** `http://localhost:8084/swagger-ui.html`
* **Booking Service:** `http://localhost:8085/swagger-ui.html`
* **Payment Service:** `http://localhost:8086/swagger-ui.html`
* **Notification Service:** `http://localhost:8087/swagger-ui.html`
* **Analytics Service:** `http://localhost:8088/swagger-ui.html`

## Versioning Policy
All external REST endpoints strictly enforce URI path versioning: `/api/v1/...`.
Breaking changes must be published under `/api/v2/...` with a 90-day deprecation window.
