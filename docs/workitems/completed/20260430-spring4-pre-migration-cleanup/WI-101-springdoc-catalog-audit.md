# WI-101 — SpringDoc / OpenAPI version consistency

Status: `done`  
Type: `refactoring`  
Area: `build`, `services`  
Backlog refs: `P-5`  
Depends on: none

## Problem Statement

Spring Boot 4 will require **SpringDoc OpenAPI 3.x**. On Boot 3.5.x we stay on SpringDoc **2.x**,
but coordinates should still be **centralized** in `libs.versions.toml` with **no stray hardcoded**
OpenAPI versions in module `build.gradle.kts` files.

## Goal

- Every module that uses springdoc uses **catalog** coordinates (`springDoc` version).
- **No** `org.springdoc:*` dependency pins bypassing the catalog (except documented exceptions with a
  one-line rationale).

## Scope

1. Grep for `springdoc`, `springDoc`, `org.springdoc` across `*.kts` / `*.toml`.
2. Replace hardcoded versions with `libs.springdoc.*` (or add missing catalog aliases).
3. **Do not** bump the catalog to SpringDoc 3.x in this WI — that pairs with the Boot 4 bump
   (backlog **P-8**).

## Acceptance Criteria

- `./gradlew build` passes; OpenAPI-related modules resolve springdoc only via the version catalog.
- Short note in WI completion (or **WI-104**) that SpringDoc 3.x wait-list is **P-8 / Boot 4**.

## Completion notes (WI-101)

- Grep of **`*.kts`** / **`*.toml`**: all SpringDoc usage is **`libs.springdoc.*`** with **`version.ref = "springDoc"`** in **`libs.versions.toml`**; no hardcoded **`org.springdoc:*`** dependency lines in build scripts.
- **`docs/design/platform/webflux-migration-plan.md`** example updated to show **`libs.springdoc.openapi.starter.webmvc.ui`** instead of a literal version pin.
- SpringDoc OpenAPI **3.x** upgrade remains backlog **P-8** / Spring Boot 4 (per migration plan §5).

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) — §5, §11
