#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MODULES=(order-service intake-service admin-bff-service allocation-service delivery-service notification-service image-analysis-service api-gateway)
for m in "${MODULES[@]}"; do
  docker build -f "$ROOT/docker/Dockerfile.service" --build-arg MODULE="$m" -t "oms/$m:0.1.0" "$ROOT"
done
docker build -f "$ROOT/docker/Dockerfile.web" -t oms/web-app:0.1.0 "$ROOT"
echo "Built all OMS images"
