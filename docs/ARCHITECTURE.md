# CasBytes ERP — architecture guidance (Core service)

This document explains **why** the Core service is shaped the way it is, and how CasBytes should scale toward a multi-service ERP.

## 1. Clean architecture & DDD (pragmatic)

We use **package-by-feature** modules under `com.casbytes.core.modules.*`. Each module should contain:

- `api` — MVC controllers + request mapping
- `application` — services, use-cases, transactions
- `domain` — entities, value objects, domain services (pure Java)
- `dto` — request/response contracts
- `mapper` — MapStruct mappers
- `repository` — Spring Data interfaces (persistence detail; acceptable here for pragmatic Spring teams)

**Rule:** domain code must not depend on Spring Web types.

## 2. Synchronous vs asynchronous communication

### Synchronous (HTTP / gRPC)

**When to use**

- User-facing request/response flows that need an immediate answer
- Read-heavy queries and command validation that must complete in the same interaction

**How to evolve toward microservices**

- Introduce an **API gateway / BFF** in front of Core, Auth, HRMS, CRM, etc.
- Prefer **contract-first** OpenAPI for HTTP; consider gRPC for high-throughput internal calls.

### Asynchronous (Kafka)

**When to use**

- Cross-module side effects (e.g., `InventoryReserved` after `SalesOrderPlaced`)
- Notifications, analytics pipelines, AI enrichment, audit fan-out
- Long-running workflows coordinated via sagas

**Guidelines**

- Events are **facts** ("something happened"), not commands.
- Consumers must be **idempotent** (keys + deduplication where needed).

## 3. Kafka event naming

Use **reverse-DNS style** topic names with **past-tense** event names:

```
erp.<domain>.<aggregate>.<event>
```

Examples:

- `erp.core.reference-item.created`
- `erp.billing.invoice.issued`
- `erp.hrms.employee.onboarded`

Payload headers should include:

- `correlationId`
- `tenantId` (future multi-tenant)
- `schemaVersion`

## 4. Redis cache naming

Use hierarchical keys:

```
erp:<service>:<aggregate>:<id-or-signature>
```

Examples:

- `erp:core:reference-item:3fa85f64-5717-4562-b3fc-2c963f66afa6`
- `erp:core:tenant-settings:{tenantId}`

Always define **TTL** policies for hot caches; never cache secrets.

## 5. API naming conventions

- Base path: `/api/v{major}`
- Resources: plural nouns, kebab-case
- Sub-resources: `/api/v1/invoices/{invoiceId}/lines`
- Actions (RPC): `/api/v1/invoices/{id}/actions/void` (use sparingly)

## 6. ERP scaling roadmap

1. **Modular monolith first** — ship velocity with strict module boundaries.
2. **Extract hot paths** — Auth, notifications, AI inference, reporting.
3. **Data per service** — each extracted service owns its database; integrate via events + well-versioned APIs.
4. **Tenant isolation** — plan row-level security / schema-per-tenant / shard keys early, even if v1 is single-tenant.

## 7. Casbin integration

- Model files describe matchers (RBAC/ABAC/ReBAC patterns).
- Policies can load from **classpath** (bootstrap) or **`jdbc`** (`casbin_rule` — see Flyway `V2__casbin_rule.sql`).
- Optional `POST /api/v1/admin/casbin/reload` reloads policies after DB edits (see `docs/OAUTH2_AND_CASBIN.md`).
- Enforce authorization in the **service layer** or dedicated authorization aspect, not scattered `if` statements in controllers.

## 8. Security & JWT (OAuth2 Resource Server)

- When `casbytes.security.oauth2.enabled=true`, Core validates **Bearer JWTs** using issuer discovery or a static JWKS URL.
- When OAuth2 is **disabled**, `JwtAuthenticationFilter` remains a no-op extension point for bespoke token parsing (not recommended for production).
- See `docs/OAUTH2_AND_CASBIN.md` for YAML examples, audiences, and Keycloak realm role mapping.

## 9. Observability

- Correlate logs, traces, and metrics with the same correlation identifier.
- Export RED/USE metrics per endpoint in production once baseline SLOs are defined.

## 10. Kubernetes readiness

See `README.md` for probe guidance. Treat `/actuator/health` as the **platform** health contract and `/api/v1/health/*` as the **business-visible** diagnostics surface for operators and support tooling.
