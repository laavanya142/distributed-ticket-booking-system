# API Gateway Service (`api-gateway`)

Reactive Spring Cloud Gateway reverse proxy, traffic load balancing, Redis Token Bucket rate limiting, and JWT authentication termination.

## Core Responsibilities
* SSL/TLS termination and reverse proxying to internal microservices.
* Distributed rate limiting using Redis token buckets.
* RSA-256 JWT validation and header enrichment (`X-User-ID`, `X-User-Roles`).
* W3C Trace Context correlation ID generation and injection.

## Port & Profiles
* **Default Port:** `8080`
* **Supported Profiles:** `dev`, `prod`, `docker`
