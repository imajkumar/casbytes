# OAuth2 Resource Server, Casbin JDBC, and integration tests

This document describes the **production-oriented** extensions added to CasBytes Core: JWT validation via Spring Security OAuth2 Resource Server, **JDBC-backed Casbin** policies, policy reload workflow, and **Testcontainers** integration tests.

## 1. OAuth2 Resource Server (JWT / JWKS)

### Configuration (`casbytes.security.oauth2`)

| Property | Description |
|----------|-------------|
| `enabled` | When `true`, registers a `JwtDecoder` and switches HTTP security to **Bearer JWT** for authenticated API routes. |
| `issuer-uri` | OIDC issuer URL (recommended). Spring resolves signing keys from issuer metadata. |
| `jwk-set-uri` | Static JWKS endpoint (alternative to issuer discovery). |
| `audiences` | Optional list; when non-empty, access tokens must include a matching `aud` claim. |
| `map-realm-roles` | When `true`, maps Keycloak-style `realm_access.roles` claims to `ROLE_*` authorities. |

Scopes from the `scope` claim are mapped to `SCOPE_*` authorities (Spring default behaviour via `JwtGrantedAuthoritiesConverter`).

### Stage / production guardrails

For profiles **`stage`** and **`prod`**, if `casbytes.security.permit-api-without-auth=false`, the application **requires** a `JwtDecoder` (i.e. OAuth2 must be configured with a valid issuer or JWKS URI). This fails fast during context startup instead of silently exposing a misconfigured service.

### YAML examples

**Issuer-based (recommended)**

```yaml
casbytes:
  security:
    permit-api-without-auth: false
    oauth2:
      enabled: true
      issuer-uri: https://auth.casbytes.com/realms/casbytes
      audiences:
        - casbytes-core
```

**JWKS-only**

```yaml
casbytes:
  security:
    oauth2:
      enabled: true
      jwk-set-uri: https://auth.casbytes.com/realms/casbytes/protocol/openid-connect/certs
      audiences:
        - casbytes-core
```

## 2. Casbin policy stores

### `classpath` (default)

Policies load from `classpath:casbin/policy.csv` (sample bootstrap). Suitable for local development and tests.

### `jdbc`

Policies load from the application database table `casbin_rule` (see Flyway `V2__casbin_rule.sql`).

| Property | Description |
|----------|-------------|
| `policy-store` | `classpath` (default) or `jdbc`. |
| `jdbc-table-name` | Table name (default `casbin_rule`). |
| `jdbc-auto-create-table` | When `true`, delegates table creation to `org.casbin.adapter.JDBCAdapter`. Prefer `false` when Flyway owns DDL. |

### Policy reload (admin workflow)

When `casbytes.casbin.reload-endpoint-enabled=true`, the service exposes:

- `POST /api/v1/admin/casbin/reload`

This endpoint calls `CasbinPolicyManagementService.reloadPolicies()` (`clearPolicy` + `loadPolicy`).

**Authorization:** `SCOPE_casbin.admin` (configure your Auth service to mint this scope for break-glass / platform operators only).

> Keep this endpoint **disabled** in production unless tightly controlled (OAuth scope + network policy + audit).

## 3. Testcontainers integration tests

### Maven

- **Default:** `./mvnw test` runs fast unit tests and **excludes** JUnit tag `integration`.
- **Integration:** `./mvnw -Pintegration test` runs tests tagged with `@Tag("integration")` (currently `CasbytesContainersIT`).

### Docker requirement

Integration tests use Testcontainers (`PostgreSQLContainer`). They are annotated with `@Testcontainers(disabledWithoutDocker = true)` so environments **without** Docker skip instead of failing.

If Docker is present but the **API client is too old** for your Docker Engine, upgrade Docker or configure Testcontainers per https://java.testcontainers.org/supported_docker_environment/

### What `CasbytesContainersIT` validates

- Flyway migrations apply on a real PostgreSQL instance.
- `casbytes.casbin.policy-store=jdbc` (via `application-integration.yml`) loads Casbin policies from SQL seed data.
- `CasbinAuthorizationService` can evaluate a representative allow rule.

## 4. References

- Spring Security OAuth2 Resource Server: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html  
- Casbin JDBC adapter: https://github.com/jcasbin/jdbc-adapter  
- Testcontainers: https://java.testcontainers.org/
