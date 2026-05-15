# Enterprise production best practices (CasBytes Core)

This checklist complements `README.md` and `docs/ARCHITECTURE.md`.

## Configuration

1. Use **Spring profiles** (`dev`, `stage`, `prod`) and **environment variables** for secrets.
2. Keep **defaults safe**: production disables Swagger UI (`application-prod.yml`).
3. Prefer **12-factor** style config: build artifacts are immutable; runtime differences are injected externally.

## Security

1. Enforce **TLS everywhere** at ingress; terminate at the mesh/ingress controller where possible.
2. Rotate **JWT signing keys** and database credentials on a schedule.
3. Enable **OAuth2 Resource Server** (`casbytes.security.oauth2`) for `stage`/`prod` when `permit-api-without-auth=false` (see `docs/OAUTH2_AND_CASBIN.md`).
4. Lock down **Actuator** endpoints with network policies and authenticated access (except health endpoints required by probes).

## Data

1. Run **Flyway** during deploy; never rely on `ddl-auto=update` in production.
2. Take **verified backups** and test restores regularly.
3. Plan **tenant isolation** early (row-level security, schema strategy, or dedicated DB per tier).

## Messaging

1. Kafka topics should be **pre-created** in production with retention & compaction policies aligned to compliance needs.
2. Use **dead-letter topics** and replay tooling for poison messages.

## Observability

1. Centralize logs with correlation IDs; alert on error budget burn.
2. Trace critical flows (Auth → Core → downstream services) once OpenTelemetry is standardized.

## Containers & Kubernetes

1. Run containers as **non-root** (Dockerfile uses a dedicated user).
2. Define **requests/limits** for CPU/memory; validate with load tests.
3. Use **PodDisruptionBudgets** for HA services.

## Change management

1. Feature flags for risky rollouts.
2. Progressive delivery (canary) for customer-visible changes.
