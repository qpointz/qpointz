# WI-207 — Boot 4 starter coordinate renames

Status: `done`  
Type: `refactoring`  
Area: `build`, `platform`  
Backlog refs: `P-5`  
Depends on: WI-202

## Goal

Apply Spring Boot 4 starter coordinate renames across the version catalog and all module build files.

## Scope

- Update `libs.versions.toml` starter aliases per Boot 4 migration guidance.
- Replace all `build.gradle.kts` usages accordingly.
- Ensure no deprecated starter names remain as direct dependencies.

## Proof commands (run on the implementation branch)

- `./gradlew :apps:mill-service:dependencies` (spot-check classpath)
- `./gradlew build`

## Acceptance Criteria

- All proof commands above are green.
- Build scripts remain version-catalog-driven.

## Completion notes (2026-04-30)

- Applied Spring Boot 4 starter coordinate renames in `libs.versions.toml`:
  - `spring-boot-starter-web` → `spring-boot-starter-webmvc` (`boot-starter-webmvc` alias; removed the `boot-starter-web` alias)
  - `spring-boot-starter-oauth2-client` → `spring-boot-starter-security-oauth2-client`
  - `spring-boot-starter-oauth2-resource-server` → `spring-boot-starter-security-oauth2-resource-server`
- Updated all Gradle build scripts to use `libs.boot.starter.webmvc` instead of `libs.boot.starter.web`.
- Proof commands are green:
  - `./gradlew :apps:mill-service:dependencies`
  - `./gradlew build`
