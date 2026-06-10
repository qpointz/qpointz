# WI-221 — Core WebFlux dependencies (Phase 0)

Status: `planned`  
Type: `refactoring`  
Area: `platform`  
Backlog refs: **P-34**, **P-1**

## Goal

Replace servlet-first starters in **core** modules with WebFlux-aligned dependencies so downstream services do not pull MVC transitively for this migration track.

## Scope (per [`webflux-migration-plan.md`](../../../design/platform/webflux-migration-plan.md) Phase 0)

1. **`core/mill-security-core`**: `spring-boot-starter-web` → **`spring-boot-starter-webflux`** (including test scope where applicable).
2. **`core/mill-service-core`**: remove or replace **`jakarta.servlet-api`** usage with **`ServerWebExchange`** (or equivalent) for any types that block a clean WebFlux graph.
3. **`core/mill-test-kit`**: `spring-boot-starter-web` → **`spring-boot-starter-webflux`** for shared test harnesses.

## Acceptance

- `./gradlew` compiles for all modules that depended on these artifacts (fix obvious breakage in the same WI).
- No intentional regression in modules **outside** this story’s scope beyond what is required by dependency changes.

## Depends on

**WI-220** (inventory context optional; **WI-221** may start once story branch exists).
