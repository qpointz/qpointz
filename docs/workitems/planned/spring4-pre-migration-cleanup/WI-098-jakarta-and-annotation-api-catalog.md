# WI-098 — Jakarta naming and `javax.annotation` catalog

Status: `planned`  
Type: `refactoring`  
Area: `platform`  
Backlog refs: `P-5`  
Depends on: none

## Problem Statement

Spring Boot 3.x is **Jakarta EE**-based. Remaining `javax.servlet`, `javax.persistence`, or
`javax.annotation` (excluding JDK `javax.net.*`, `javax.crypto.*`, etc.) and a **`javax.annotation-api`**
catalog entry add noise and complicate a future Boot 4 migration audit.

## Goal

- Repo uses **Jakarta** APIs everywhere Boot 3 expects them.
- Prefer **`jakarta.annotation-api`** (BOM-managed or aligned with Spring Boot) instead of
  `javax.annotation:javax.annotation-api` in `libs.versions.toml` **if** any module still needs an
  explicit annotation API dependency.

## Scope

1. Grep sources for `javax.annotation`, `javax.servlet`, `javax.persistence` (adjust for false
   positives such as generated code or third-party stubs).
2. Update `libs.versions.toml`: replace or remove `javax-annotation-api`; add `jakarta-annotation-api`
   if a direct dependency is still required; update all `build.gradle.kts` references.
3. Fix any hits under `misc/**`, `ai/**`, and product modules per migration plan §7 (update paths if
   the design doc lists removed trees).

## Out of Scope

- Migrating **Jackson** packages (`com.fasterxml` → `tools.jackson`) — Boot 4 / Jackson 3 work.

## Acceptance Criteria

- No unintended `javax.servlet` / `javax.persistence` / `javax.annotation` imports in project
  Java/Kotlin sources (JDK `javax.*` excepted).
- `./gradlew build` (or CI equivalent) passes.
- `libs.versions.toml` documents only Jakarta-aligned annotation APIs for compile use.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) — §7
