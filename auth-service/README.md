# Auth Service (`auth-service`)

Production-grade Authentication and Authorization microservice built with **Clean Architecture**, **Spring Security 6**, **RS256 JSON Web Tokens (JWT)**, **Refresh Token Rotation**, **BCrypt**, and **Flyway migrations**.

## Architecture & Responsibilities
* **User Registration:** Validates and creates user credentials with BCrypt password hashing.
* **Authentication (Login):** Issues RS256 asymmetric signed JWT access tokens (15-min expiry) and stateful refresh tokens (7-day expiry).
* **Token Rotation & Revocation:** Replaces refresh tokens on use and supports explicit logout revocation.
* **JWKS Endpoint:** Exposes `/.well-known/jwks.json` containing RSA public keys in JSON Web Key Set format so downstream services and the API Gateway can verify tokens statelessly without querying the auth database.

## Endpoints
* `POST /api/v1/auth/register` — Register a new account
* `POST /api/v1/auth/login` — Authenticate and obtain JWT + Refresh Token
* `POST /api/v1/auth/refresh` — Rotate refresh token and obtain new JWT
* `POST /api/v1/auth/logout` — Revoke refresh token
* `POST /api/v1/auth/validate` — Validate JWT token validity and claims
* `GET /.well-known/jwks.json` — Retrieve RS256 JSON Web Key Set (public keys)
* `GET /actuator/health` — Service liveness and readiness probe
