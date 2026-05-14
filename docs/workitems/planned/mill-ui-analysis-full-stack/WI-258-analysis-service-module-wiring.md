# WI-258 — Analysis queries HTTP module + `mill-service` wiring

Status: `planned`  
Type: `feature`  
Area: `services`, `apps`  
Backlog refs: **U-13**

## Goal

Introduce a **neutral** Gradle module (e.g. `mill-analysis-queries-service` or equivalent name) that hosts REST controllers for **`/api/v1/queries`**, registered when **`DataOperationDispatcher`** is available (**`@ConditionalOnBean(DataOperationDispatcher.class)`** — do **not** require **`@ConditionalOnAiEnabled`**).

## Scope

1. Module `build.gradle.kts`, Spring Boot autoconfiguration entry if needed.
2. Add implementation dependency to [`apps/mill-service`](../../../../apps/mill-service/build.gradle.kts); register in [`settings.gradle.kts`](../../../../settings.gradle.kts).
3. CORS / error handling consistent with existing REST surfaces.

## Acceptance

- Application context starts with **`minimal` or `ai`** edition and exposes `/api/v1/queries` when data plane is present.
- **WI-257** controller classes live here (or split documented in WI-257/STORY).

## Depends on

**WI-256** (persistence beans available for wiring).

## Notes

**WI-257** adds the public **saved-query** controllers (list + get by id only) into this module (or documents split if service interface is separate). **Execution** stays **`/api/v1/query/**`** (closed **`query-result-execution-service`**).