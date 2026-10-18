# Order Management System (order-ms)

Java 21 + Spring Boot 3.x drop-shipping OMS from `.kiro/specs/order-management-system/tasks.md`.

## Modules

| Module | Port | Role |
|--------|------|------|
| `oms-common` / `oms-eventing` | — | Shared types, after-commit Kafka publish, idempotency |
| `order-service` | 8081 | Authoritative orders |
| `intake-service` | 8082 | Website intake + image upload |
| `admin-bff-service` | 8083 | Admin aggregation / short-TTL cache |
| `allocation-service` | 8084 | Bulk purchase + allocation |
| `delivery-service` | 8085 | Lifecycle + delivery marks |
| `notification-service` | 8086 | Bounded-retry notifications |
| `image-analysis-service` | 8087 | Vision matching (stub provider) |
| `api-gateway` | 8080 | Spring Cloud Gateway edge |
| `web-app` | 4200 (dev) | Angular customer + admin UI |
| `mobile-app` | — | Capacitor shell over `web-app` |

## Build backend

```bash
# Maven is under %USERPROFILE%\tools\apache-maven-3.9.9\bin if not on PATH
mvn clean install -DskipTests
```

## Build web

```bash
cd web-app
npm install
npm start          # http://localhost:4200
npm run build      # dist/web-app
```

## Local infra

```bash
docker compose up -d
```

## Containers & Kubernetes

```powershell
.\scripts\build-images.ps1
kubectl apply -f k8s/
```

See `k8s/README.md` and `docs/event-wiring.md`.

## Mobile

```bash
cd mobile-app
npm install
npm run sync
```

Reuses the Angular UI and the same API Gateway contracts.
