#!/usr/bin/env bash
set -e

echo "🚀 Starting Distributed Ticket Booking Infrastructure (Docker Compose)..."
docker compose up -d

echo "⏳ Waiting for PostgreSQL and Kafka to be healthy..."
sleep 5

echo "✅ Infrastructure is running!"
echo "   - PostgreSQL: localhost:5432"
echo "   - Redis Cluster: localhost:6379"
echo "   - Kafka Broker: localhost:9092"
echo "   - Prometheus: http://localhost:9090"
echo "   - Grafana: http://localhost:3000 (admin/admin)"
echo "   - Zipkin Tracing: http://localhost:9411"
