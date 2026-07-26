#!/usr/bin/env bash
set -e

echo "🧹 Cleaning Maven build artifacts across all modules..."
mvn clean

echo "🗑️ Removing local Docker volumes and orphaned containers..."
docker compose down -v --remove-orphans

echo "✨ Repository and local environment reset to clean state!"
