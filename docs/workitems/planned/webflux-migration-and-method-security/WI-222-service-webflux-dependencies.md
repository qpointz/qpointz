# WI-222 — Service module WebFlux dependencies (Phase 1)

Status: `planned`  
Type: `refactoring`  
Area: `platform`  
Backlog refs: **P-34**, **P-1**

## Goal

Align **service** and **starter** Gradle modules with WebFlux and Springdoc WebFlux where the migration plan requires it.

## Scope (per [`webflux-migration-plan.md`](../../../design/platform/webflux-migration-plan.md) Phase 1)

1. **`services/mill-metadata-service`**: WebFlux + Springdoc OpenAPI WebFlux starter (replace WebMVC UI starter as per plan).
2. **`services/mill-jet-http-service`**: add WebFlux (including resolving overlap with **`mill-starter-backends`** / inherited web starter).
3. **`core/mill-starter-service`**: expose **`spring-boot-starter-webflux`** instead of `web`.
4. **`services/mill-ui-service`**: `spring-boot-starter-web` → **`spring-boot-starter-webflux`**.

## Acceptance

- Affected modules **assemble**; conflicting `spring-boot-starter-web` + `spring-boot-starter-webflux` on the same app classpath is **resolved intentionally** (one reactive stack per application).

## Depends on

**WI-221**
