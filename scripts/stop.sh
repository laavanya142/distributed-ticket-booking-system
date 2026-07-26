#!/usr/bin/env bash
set -e

echo "🛑 Stopping Distributed Ticket Booking Infrastructure..."
docker compose down

echo "✅ All local infrastructure containers stopped cleanly."
