# CasBytes Core — coding standards

These rules keep the codebase consistent as the ERP surface area grows.

## Naming conventions

| Artifact | Convention | Example |
|----------|------------|---------|
| Java package | lowercase, no underscores | `com.casbytes.core.modules.hr.leave` |
| Class / interface | PascalCase | `LeaveRequestService` |
| Method / field | camelCase | `approveLeaveRequest` |
| Constants | UPPER_SNAKE | `API_V1` |
| REST path segments | kebab-case plural nouns | `/api/v1/leave-requests` |
| JSON fields | camelCase | `createdAt` |
| Database tables | snake_case plural | `leave_requests` |
| Database columns | snake_case | `approved_at` |
| Flyway scripts | `V{number}__description.sql` | `V12__add_leave_request_indexes.sql` |
| Kafka topics | dot-separated domain hierarchy | `erp.hrms.leave.requested` |
| Redis keys | colon-separated, lowercase | `erp:core:reference-item:{id}` |

## Class conventions

1. **Controllers** end with `*Controller`, live under `...api`, and must stay thin (HTTP mapping + validation only).
2. **Services** end with `*Service`, contain orchestration and transactions.
3. **Repositories** end with `*Repository`, are Spring Data interfaces.
4. **Entities** are plain JPA types in `domain` (no DTOs leaking outward).
5. **DTOs** suffix with `Request`, `Response`, `Command`, or `Query` depending on role.
6. **Mappers** end with `*Mapper` (MapStruct preferred).
7. **Configuration** types end with `*Configuration` or `*Properties`.

## API naming

1. Use **nouns** for resources, HTTP verbs for semantics.
2. Prefer **pagination** (`page`, `size`, `sort`) on list endpoints once datasets grow.
3. Avoid verbs in URLs except for documented RPC-style operations (`/actions/approve`).

## Service naming

1. One **application service** per aggregate use-case group (`InvoicePaymentService`, not `DoStuffService`).
2. Cross-module orchestration belongs in a dedicated application service or saga orchestrator (future), not random controllers.

## DTO rules

1. Request DTOs live next to the module; annotate with Bean Validation.
2. Never expose JPA entities directly from controllers.

## Database rules

1. **UUID** primary keys for externally exposed entities (aligns with multi-region ID generation).
2. Always include auditing columns (`created_at`, `updated_at`) for operational tables.
3. Use **constraints** in the database, not only application checks.

## Exception rules

1. Throw **domain-specific** exceptions extending `BusinessException` where appropriate.
2. Do not catch-and-swallow `Exception` in business code; rely on `GlobalExceptionHandler` for unknown failures.

## Logging & audit

1. Use structured key=value fields in log messages where practical.
2. Annotate high-risk service entry points with `@Auditable` (payments, permissions, master data writes).

## Testing

1. Prefer slice tests for repositories; `@SpringBootTest` for critical flows.
2. Keep `test` profile fast: no Docker dependency by default.
