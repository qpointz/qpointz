# WI-202 — Bump Spring Boot to 4.0.6 and fix immediate breakage

Status: `done`  
Type: `refactoring`  
Area: `platform`, `build`  
Backlog refs: `P-5`  
Depends on: WI-201

## Goal

Move `springBoot` in the version catalog to **4.0.6** and restore compilation/tests enough that
subsequent WIs can proceed.

## Scope

- Update `springBoot` in `libs.versions.toml`.
- Fix first-wave compilation and dependency-resolution failures (expected after a major BOM bump).
- Keep changes minimal and mechanical; defer deeper refactors (Jackson 3, Security 7) to their WIs.

## Proof commands (run on the implementation branch)

- Fast signal:
  - `./gradlew :apps:mill-service:compileJava` (or Kotlin compile tasks in affected modules)
  - `./gradlew :services:mill-data-grpc-service:compileJava :services:mill-data-grpc-service:compileKotlin`
- Minimal verification (must be green for WI completion):
  - `./gradlew :services:mill-service-common:test`
  - `./gradlew :services:mill-data-http-service:test`

## Acceptance Criteria

- The proof commands above are green.
- `springBoot` is pinned to **4.0.6** for the story (no version churn / flip-flopping).
## Notes

If Gradle resolution requires explicit BOM import in modules, prefer the existing pattern used for
`boot-dependencies` alignment (introduced during WI-099 pre-migration cleanup).

## Completion notes (WI-202)

- Bumped `springBoot` to **4.0.6** in `libs.versions.toml`.
- Fixed first-wave Boot 4 package moves:
  - `OAuth2ResourceServerProperties` → `org.springframework.boot.security.oauth2.server.resource.autoconfigure.*`
  - `EntityScan` / `EntityScanPackages` → `org.springframework.boot.persistence.autoconfigure.*`
- Kept ordering-only autoconfig dependency on `JpaRepositoriesAutoConfiguration` out of the code
  (removed `@AutoConfigureAfter` in `AiV3ValueMappingStateAutoConfiguration`) to avoid relying on
  non-stable internal auto-config class names across Boot 4 modularization.
- Proof commands are green:
  - `./gradlew :apps:mill-service:compileJava`
  - `./gradlew :services:mill-data-grpc-service:compileJava :services:mill-data-grpc-service:compileKotlin`
  - `./gradlew :services:mill-service-common:test`
  - `./gradlew :services:mill-data-http-service:test`

