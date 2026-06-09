# WI-258 — Analysis queries HTTP module + `mill-service` wiring

Status: `planned`  
Type: `feature`  
Area: `services`, `apps`  
Backlog refs: **U-13**

## Goal

Introduce a **neutral** Gradle module **`services/mill-analysis-queries-service`** that hosts REST controllers for **`/api/v1/queries`**, registered when **`DataOperationDispatcher`** is available (**`@ConditionalOnBean(DataOperationDispatcher.class)`** — do **not** require **`@ConditionalOnAiEnabled`**).

## Scope

1. Module `build.gradle.kts`; Spring Boot autoconfiguration via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (pattern: [`mill-data-query-service`](../../../../services/mill-data-query-service)).
2. `implementation` dependency on **WI-256** persistence module (`mill-analysis-persistence`) and port module (`mill-analysis-queries`).
3. Register in [`settings.gradle.kts`](../../../../settings.gradle.kts).
4. **Direct** `implementation(project(":services:mill-analysis-queries-service"))` in [`apps/mill-service/build.gradle.kts`](../../../../apps/mill-service/build.gradle.kts) — not buried in `data-services` edition feature (same pattern as metadata).
5. Error handling consistent with existing `/api/**` REST (e.g. [`QueryResultExceptionHandler`](../../../../services/mill-data-query-service/src/main/kotlin/io/qpointz/mill/data/query/web/QueryResultExceptionHandler.kt) style).

## Acceptance

- `mill-service` starts with **`minimal` or `ai`** edition when data plane is present.
- Beans register when `DataOperationDispatcher` exists; no hard dependency on AI modules.
- **WI-257** controller classes live in this module.

## Depends on

**WI-256** (persistence beans and `SavedQueryCatalog` available for wiring).

## Notes

**WI-257** adds public **saved-query** controllers (list + get by id only). **Execution** stays **`/api/v1/query/**`** (closed **`query-result-execution-service`**).
