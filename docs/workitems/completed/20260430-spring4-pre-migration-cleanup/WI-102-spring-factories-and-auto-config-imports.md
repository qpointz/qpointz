# WI-102 — `META-INF` auto-configuration legacy audit

Status: `done`  
Type: `refactoring`  
Area: `platform`  
Backlog refs: `P-5`  
Depends on: none

## Problem Statement

Spring Boot 4 **ignores** `META-INF/spring.factories` for **auto-configuration** registration. Legacy
files that still register `EnableAutoConfiguration` cause silent drift: works on 3.x, breaks on 4.x.

## Goal

- Ensure every auto-configuration entry uses
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (or is registered
  via Spring Boot 3.5–supported mechanisms).
- Remove redundant `spring.factories` autoconfig keys **or** document a confirmed-empty state repo-wide.

## Scope

1. Grep for `spring.factories` and for `org.springframework.boot.autoconfigure.EnableAutoConfiguration`
   registrations.
2. For each hit: migrate listings to `AutoConfiguration.imports`, or confirm the file is unrelated
   (e.g. non-autoconfig `spring.factories` uses — rare; verify against Boot 3.5 docs).
3. Run a representative `:apps:mill-service` or full `./gradlew build` depending on touched modules.

## Acceptance Criteria

- No `spring.factories` entries remain that register Boot auto-configuration classes for discovery,
  unless Spring Boot 3.5 still **requires** them (justify in code review).
- `./gradlew build` / CI green.

## Completion notes (WI-102)

- **`find … -name 'spring.factories'`** (excluding `build/`, `.gradle/`): **zero** files — no legacy **`META-INF/spring.factories`** autoconfig registration in the repo.
- **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`** is present in: `metadata/mill-metadata-autoconfigure`, `data/mill-data-autoconfigure`, `ai/mill-ai-v3-autoconfigure`, `ai/mill-ai-v3-persistence`, `security/mill-security-autoconfigure`, `security/mill-security-persistence`, `security/mill-security-auth-service`, `persistence/mill-persistence-autoconfigure`.
- **`@EnableAutoConfiguration`** in Java/Kotlin appears only in **tests** (slice / integration setup), not in **`META-INF`** — acceptable; unrelated to **`spring.factories`** discovery.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) — §8
