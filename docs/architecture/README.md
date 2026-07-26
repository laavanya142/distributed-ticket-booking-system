# Architecture & Architecture Decision Records (ADRs)

This directory houses detailed architectural specifications, domain models, and formal Architecture Decision Records (ADRs) for the Distributed Ticket Booking System.

## Primary Blueprint
* [System Architecture & Engineering Specification](../../ARCHITECTURE.md)

## Core Architecture Decision Records (ADRs)
1. **ADR-001: Polyglot Persistence with Database per Service**
   * **Status:** Accepted
   * **Context:** High-throughput transactional booking and full-text event discovery have conflicting storage requirements.
   * **Decision:** Use PostgreSQL for transactional ACID domains, Redis Cluster for distributed locking/bitmaps, and OpenSearch for catalog search.
2. **ADR-002: Flash Sale Concurrency Control via Redis Lua Scripting**
   * **Status:** Accepted
   * **Context:** Relational database row locks (`FOR UPDATE`) fail under 100,000 concurrent user requests during flash sales.
   * **Decision:** Decouple temporary reservation from DB commits using atomic Redis Lua scripts with 10-minute TTLs and monotonic Fencing Tokens.
3. **ADR-003: Java 21 Project Loom (Virtual Threads) Adoption**
   * **Status:** Accepted
   * **Context:** OS thread-per-request models saturate thread pools during blocking JDBC and HTTP calls under heavy load.
   * **Decision:** Enable `spring.threads.virtual.enabled=true` across all Spring MVC microservices.
