# CasBytes Core Service

Enterprise foundation for the **CasBytes** ERP SaaS platform. This repository is the **Core** service (`casbytes-core-service`), designed as a modular monolith today and as a **microservice-ready** domain boundary tomorrow.

- **Company:** CasBytes — https://casbytes.com  
- **Maven coordinates:** `com.casbytes:casbytes-core-service`  
- **Default database:** `casbytes_core_db` (PostgreSQL, schema `public`)

## Mandatory onboarding

Before writing production code, read:

1. `docs/ONBOARDING.md` (**required**)
2. `docs/CODING_STANDARDS.md`
3. `docs/ARCHITECTURE.md`
4. `docs/GIT_CONVENTIONS.md`
5. `docs/PRODUCTION_BEST_PRACTICES.md`
6. `docs/OAUTH2_AND_CASBIN.md` (OAuth2 RS, Casbin JDBC, Testcontainers)

## Technology stack

| Area | Choice |
|------|--------|
| Runtime | Java **21** (LTS), Spring Boot **4.0.x** |
| API | Spring Web MVC, Bean Validation, OpenAPI (springdoc **3.x**) |
| Persistence | Spring Data JPA, Hibernate, Flyway |
| Cache | Spring Data Redis |
| Messaging | Spring for Apache Kafka |
| Security | Spring Security (stateless), JWT hooks (future Auth service) |
| Authorization | Casbin (`jcasbin`) with classpath bootstrap policy |
| Observability | Actuator, Micrometer Prometheus, structured logging (Logback), correlation IDs |
| Mapping | MapStruct |

> **Note:** Spring Boot 4 uses **Jackson 3** (`tools.jackson.*`) for the HTTP JSON mapper. Example customization lives in `JacksonConfiguration`.

## Configuration model (no `.env` files)

Configuration is **YAML + Spring profiles** plus **OS environment variables** / Kubernetes secrets.  
**Never commit secrets** (passwords, private keys, tokens). Use placeholders such as `${CASBYTES_DATASOURCE_PASSWORD:}` and inject values via your shell, systemd, Docker, or Kubernetes.

Example local PostgreSQL URL (matches your target topology):

`jdbc:postgresql://localhost:5432/casbytes_core_db?currentSchema=public` (or `localhost:5433` when using the bundled `docker-compose.yml`, which maps Postgres to **5433** to avoid clashes with a system install).

Set credentials in the environment (example for interactive dev shells only):

```bash
export CASBYTES_DATASOURCE_USERNAME="ayra"
export CASBYTES_DATASOURCE_PASSWORD='(set locally; do not commit)'
```

## Spring profiles

| Profile | Purpose |
|---------|---------|
| `dev` | Local developer workstation; verbose SQL logging; `casbytes.security.permit-api-without-auth=true` |
| `stage` | Pre-production integration; closer to prod defaults |
| `prod` | Production hardening; Swagger UI disabled |
| `test` | Unit/integration tests (H2, Kafka/Redis infra disabled) |

### Sample startup commands

```bash
# Local development (PostgreSQL + Redis + Kafka must match application-dev overrides)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Explicit JVM + profile (typical in servers)
java -jar target/casbytes-core-service-0.1.0-SNAPSHOT.jar --spring.profiles.active=stage

# Production-style jar run
java -XX:MaxRAMPercentage=75.0 -jar target/casbytes-core-service-0.1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## API versioning

- **URI versioning:** all first-party REST APIs live under `/api/v1/...`
- Add `/api/v2` when breaking changes are introduced; keep v1 until consumers migrate.

## Standard HTTP response envelope

### Success

```json
{
  "success": true,
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "code": "DEMO-001",
    "name": "Demo reference item",
    "active": true,
    "createdAt": "2026-05-15T10:15:30Z",
    "updatedAt": "2026-05-15T10:15:30Z"
  },
  "error": null,
  "meta": {
    "timestamp": "2026-05-15T10:15:30.123Z",
    "correlationId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "path": "/api/v1/reference/items/3fa85f64-5717-4562-b3fc-2c963f66afa6"
  }
}
```

### Error

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Validation failed",
    "details": [
      {
        "field": "code",
        "message": "must not be blank",
        "rejectedValue": ""
      }
    ],
    "traceId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "path": "/api/v1/reference/items"
  },
  "meta": {
    "timestamp": "2026-05-15T10:15:30.456Z",
    "correlationId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "path": "/api/v1/reference/items"
  }
}
```

## Enterprise health endpoints (application API)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/health` | Aggregated checks (database, redis, kafka) |
| `GET` | `/api/v1/health/db` | Database probe (`SELECT 1`) |
| `GET` | `/api/v1/health/redis` | Redis `PING` (or `SKIPPED` if Redis is not configured) |
| `GET` | `/api/v1/health/kafka` | Kafka cluster metadata probe (or `SKIPPED` if disabled) |

Kubernetes **liveness/readiness** should still prefer Spring Boot Actuator (`/actuator/health`) on the **management port** (`8081` by default) for standard probes.

## Actuator & management port

- **Application port:** `8080` (`server.port`)
- **Management port:** `8081` (`management.server.port`)

Exposed actuator web endpoints (base path `/actuator`): `health`, `info`, `prometheus`, `metrics`, `loggers` (tune per environment).

## OpenAPI / Swagger UI

- **OpenAPI JSON:** `/v3/api-docs`
- **Swagger UI:** `/swagger-ui.html` (springdoc 3 defaults)

Production profile disables springdoc by default.

## Request tracing

- Incoming header: `X-Correlation-Id` (fallback: `X-Trace-Id`)
- Response echoes the same headers.
- Logback pattern includes MDC `correlationId`.

## Docker

```bash
./mvnw -DskipTests package
docker build -t casbytes/core-service:dev .
```

`docker-compose.yml` starts **PostgreSQL**, **Redis**, **Apache Zookeeper**, **Apache Kafka** (Confluent Community images), **[Kafka UI](https://github.com/kafbat/kafka-ui)**, **Elasticsearch**, and **Kibana** for local development. Export `CASBYTES_DATASOURCE_PASSWORD` before `docker compose up` (the compose file intentionally requires it).

**Published host ports (defaults avoid common local installs):** Postgres **5433**→5432, Redis **6380**→6379, Zookeeper **2182**→2181, Kafka **9094**→9094 (`PLAINTEXT_HOST` for apps on the host), **Kafka UI** **8099**→8080 (`http://localhost:8099`), **Elasticsearch** **9201**→9200 (`http://localhost:9201`), **Kibana** **5602**→5601 (`http://localhost:5602`). Override with `CASBYTES_COMPOSE_POSTGRES_PORT`, `CASBYTES_COMPOSE_REDIS_PORT`, `CASBYTES_COMPOSE_ZOOKEEPER_PORT`, `CASBYTES_COMPOSE_KAFKA_HOST_PORT`, `CASBYTES_COMPOSE_KAFKA_UI_PORT`, `CASBYTES_COMPOSE_ELASTICSEARCH_PORT`, `CASBYTES_COMPOSE_KIBANA_PORT` if needed. The `dev` Spring profile (`application-dev.yml`) lines up JDBC, Redis, Kafka bootstrap, and Elasticsearch with these defaults.

## Project structure (high level)

```
com.casbytes.core
├── CasbytesCoreServiceApplication
├── audit                 # @Auditable + aspect-driven audit logs
├── configuration         # cross-cutting Spring configuration
├── infrastructure        # redis, kafka, casbin adapters
├── modules               # package-by-feature ERP modules
│   └── reference         # sample vertical slice (template)
├── observability         # correlation id filter
├── platform              # health aggregation
├── security              # SecurityFilterChain + JWT extension point
└── shared                # api envelope, exceptions, utilities
```

Full file listing is generated in CI or locally via:

```bash
find . -type f \( -path './target/*' -o -path './.git/*' \) -prune -o -type f -print | sed 's|^\./||' | sort
```

## Production & Kubernetes readiness (summary)

1. **Ports:** expose `8080` (traffic) and `8081` (management) separately in the Service mesh / ingress policy.
2. **Probes:** Liveness `GET http://127.0.0.1:8081/actuator/health/liveness` (when enabled) or `/actuator/health`; Readiness should include downstream checks as appropriate.
3. **Secrets:** mount as env vars or files from a secret manager (Vault, AWS Secrets Manager, GSM).
4. **Config:** use `SPRING_PROFILES_ACTIVE=prod` and externalize overrides via environment variables matching the `CASBYTES_*` and `SPRING_*` placeholders in `application.yml`.
5. **Observability:** scrape Prometheus from `:8081/actuator/prometheus` via network policy–limited monitoring namespaces.
6. **HPA:** CPU + memory + custom metrics (Kafka lag, HTTP latency) once baselines exist.

## Build & tests

```bash
./mvnw clean verify
```

- **Unit tests (default):** `./mvnw test` — excludes the JUnit tag `integration`.
- **Integration tests (Docker):** `./mvnw -Pintegration test` — runs `CasbytesContainersIT` (PostgreSQL via Testcontainers). Tests are skipped when Docker is unavailable (`@Testcontainers(disabledWithoutDocker = true)`).

See `docs/OAUTH2_AND_CASBIN.md` for OAuth2 issuer/JWKS settings, Casbin JDBC/reload, and CI notes.

## License

Proprietary — CasBytes. All rights reserved.
