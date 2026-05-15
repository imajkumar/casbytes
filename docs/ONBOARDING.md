# CasBytes Core — Developer onboarding

**Before starting development, every engineer must read this document in full.**

This service is the **Core** boundary of the CasBytes ERP SaaS platform. Treat it as a **modular monolith** with explicit domain modules (`com.casbytes.core.modules.*`) that can later be extracted into independent deployables.

## 1. Security & secrets

1. **Do not commit secrets** (passwords, API keys, private keys, JWT signing material).
2. This project **does not use `.env` files**. Use:
   - OS environment variables, or
   - Kubernetes Secrets / external secret managers, or
   - Spring `SPRING_APPLICATION_JSON` in controlled automation (still not committed to git).
3. For local PostgreSQL, set at minimum:
   - `CASBYTES_DATASOURCE_PASSWORD`
   - optionally override `CASBYTES_DATASOURCE_USERNAME`

## 2. Profiles & ports

| Profile | Typical use |
|---------|-------------|
| `dev` | Local workstation |
| `stage` | shared integration |
| `prod` | production |
| `test` | automated tests only |

Default ports:

- `8080` — HTTP API (`server.port`)
- `8081` — Actuator (`management.server.port`)

## 3. Local dependencies

For full functionality (database, cache, messaging):

1. Start infrastructure (recommended). Compose maps **Postgres to host 5433** and **Redis to 6380** by default so they do not conflict with a local PostgreSQL/Redis on 5432/6379:

   ```bash
   export CASBYTES_DATASOURCE_PASSWORD='(local secret)'
   docker compose up -d
   ```

2. Run the service with the **dev** profile so JDBC/Redis targets match the compose ports:

   ```bash
   export CASBYTES_DATASOURCE_PASSWORD='(same local secret)'
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

Kafka bootstrap defaults to `localhost:9094` (see `docker-compose.yml`: Kafka `PLAINTEXT_HOST` listener; Zookeeper is on host **2182**). **Kafka UI** is at `http://localhost:8099` by default (`CASBYTES_COMPOSE_KAFKA_UI_PORT`). **Elasticsearch** is at `http://localhost:9201` and **Kibana** at `http://localhost:5602` (`CASBYTES_COMPOSE_ELASTICSEARCH_PORT`, `CASBYTES_COMPOSE_KIBANA_PORT`). The app reads `spring.elasticsearch.uris` (see `application.yml` / `application-dev.yml`).

## 4. API conventions

1. All REST APIs are versioned under `/api/v1`.
2. Responses use the shared `ApiResponse<T>` envelope (`success`, `data`, `error`, `meta`).
3. Pass `X-Correlation-Id` from upstream gateways and BFFs; the service will generate one if absent.

## 5. Architecture expectations

1. **Package-by-feature** under `com.casbytes.core.modules.<bounded-context>`.
2. Inside a module, keep **inward dependencies** (API → application → domain). Infrastructure implements persistence and integrations.
3. Cross-cutting concerns belong in `shared`, `configuration`, `infrastructure`, `security`, `audit`, `observability`, or `platform`.

## 6. Database migrations

1. Use **Flyway** only (`src/main/resources/db/migration`).
2. Naming: `V{version}__{snake_case_description}.sql` (double underscore).
3. Keep migrations **idempotent in spirit** (forward-only); never rewrite applied migration files.

## 7. Authorization roadmap

1. **Authentication** will be centralized in a future **Auth** service (JWT/OIDC).
2. **Authorization** uses **Casbin** for fine-grained, policy-driven checks. Policies shipped in-repo are **bootstrap samples** only; production should load policies from a durable store with change management.

## 8. Quality gates

1. `./mvnw clean verify` must pass before opening a PR.
2. Follow `docs/CODING_STANDARDS.md` and `docs/GIT_CONVENTIONS.md`.

## 9. OAuth2, Casbin JDBC, and Dockerized integration tests

Before enabling OAuth2 resource server mode, JDBC Casbin, or the Casbin reload endpoint in a shared environment, read **`docs/OAUTH2_AND_CASBIN.md`**.

Integration tests (`./mvnw -Pintegration test`) require a working Docker API compatible with Testcontainers.

## 10. Where to read next

- `README.md` — operations, examples, stack overview  
- `docs/CODING_STANDARDS.md` — naming & structure rules  
- `docs/ARCHITECTURE.md` — integration patterns & ERP scaling guidance  
- `docs/GIT_CONVENTIONS.md` — commits & branching  
- `docs/PRODUCTION_BEST_PRACTICES.md` — production checklist  
- `docs/OAUTH2_AND_CASBIN.md` — JWT resource server, Casbin JDBC/reload, Testcontainers  

Welcome to CasBytes Core — build boring, reliable ERP infrastructure.
