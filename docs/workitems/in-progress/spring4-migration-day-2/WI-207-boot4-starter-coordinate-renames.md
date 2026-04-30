# WI-207 — Boot 4 starter coordinate renames

Status: `planned`  
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
