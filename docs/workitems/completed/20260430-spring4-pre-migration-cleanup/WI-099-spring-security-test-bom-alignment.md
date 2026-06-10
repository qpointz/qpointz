# WI-099 — `spring-security-test` coordinate alignment

Status: `done`  
Type: `refactoring`  
Area: `platform`, `build`  
Backlog refs: `P-5`  
Depends on: none

## Problem Statement

`libs.versions.toml` pins `spring-security-test` to a **fixed** version (e.g. `6.5.7`) while Spring
Boot manages the rest of the Spring Security stack via its BOM. That pin can **drift** from the
Boot-managed Security version and masks issues until a Boot upgrade.

## Goal

On **Spring Boot 3.5.x**, let the **Spring Boot BOM** drive `spring-security-test` where possible, or
replace the pin with `version.ref = "springBoot"`-aligned strategy documented in this WI.

## Scope

1. Identify every consumer of `libs.spring.security.test` (or equivalent alias).
2. Remove the hardcoded version from the catalog entry **if** Gradle+Spring Dependency Management
  resolve a compatible version from the Boot BOM alone; otherwise align the version with the
  Security version implied by the current Boot release notes.
3. Run tests for modules that use security test support.

## Acceptance Criteria

- No unnecessary hardcoded `spring-security-test` patch version in `libs.versions.toml`, or the
  version matches the Boot 3.5 BOM story with a comment explaining any exception.
- `./gradlew test` / CI green for affected modules.

## Completion notes (WI-099)

- **Consumers:** only **`services/mill-service-common`** (JvmTestSuite test classpath).
- **Change:** Removed the standalone **`spring-security-test`** catalog entry (was pinned **`6.5.7`**). Added **`boot-dependencies`** (`org.springframework.boot:spring-boot-dependencies`, **`version.ref = "springBoot"`**) to **`libs.versions.toml`**. The test suite now uses **`implementation(platform(libs.boot.dependencies))`** plus an **unversioned** **`org.springframework.security:spring-security-test`** coordinate so the version always matches the Boot BOM line for the current **`springBoot`** property.
- **Note:** Version catalog entries **without** a version do **not** pick up the Spring Dependency Management plugin’s BOM on this stack; **`platform(...)`** is the reliable alignment mechanism for this dependency.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) — §9
