# WI-207 — Boot 4 starter coordinate renames

Status: `planned`  
Type: `refactoring`  
Area: `build`, `platform`  
Backlog refs: `P-5`  
Depends on: WI-202

## Goal

Apply Spring Boot 4 starter coordinate renames across the version catalog and all module build files.

## Scope

- Update `libs.versions.toml` starter aliases per Boot 4 migration guidance.\n- Replace all `build.gradle.kts` usages accordingly.\n- Ensure no deprecated starter names remain as direct dependencies.\n
## Proof commands (run on the implementation branch)

- `./gradlew :apps:mill-service:dependencies` (spot-check classpath)\n- `./gradlew build`\n
## Acceptance Criteria

- All modules resolve on Boot 4 without depending on deprecated starter coordinates.\n- Build scripts remain version-catalog-driven.\n
