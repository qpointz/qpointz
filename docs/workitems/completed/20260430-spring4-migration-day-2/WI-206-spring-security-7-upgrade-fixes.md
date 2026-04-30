# WI-206 — Spring Security 7 upgrade fixes + tests

Status: `done`  
Type: `refactoring`  
Area: `security`  
Backlog refs: `P-9`  
Depends on: WI-202

## Goal

Resolve Spring Security **7.0** breaking changes and restore green tests.

## Scope

- Update security modules and their configuration classes.
- Fix test suites relying on prior defaults (CSRF/headers/OAuth2 wiring changes).
- Preserve the existing “BOM-aligned test dependency” pattern:
  - Use `platform(libs.boot.dependencies)` where needed for test-only alignments.

## Proof commands (run on the implementation branch)

- `./gradlew :security:mill-security:test :security:mill-security-auth-service:test :security:mill-security-autoconfigure:test :security:mill-security-persistence:test :security:mill-service-security:test`
- `./gradlew :security:mill-security-auth-service:testIT :security:mill-security-autoconfigure:testIT :security:mill-security-persistence:testIT`
- `./gradlew :services:mill-data-grpc-service:testIT` (gRPC security interceptor behavior)

## Acceptance Criteria

- All proof commands above are green.
- No ad-hoc version pins for security artifacts drift away from the Boot BOM line.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) §9.
- [`docs/design/platform/spring4-boot4-jump-start-inventory.md`](../../../design/platform/spring4-boot4-jump-start-inventory.md) (paths list).

## Completion notes (2026-04-30)

Key fixes:

- Spring Boot 4: removed usages of `@AutoConfigureTestDatabase` in persistence testIT suites (type removed in Boot 4).
- Spring Boot 4: migrated `@EntityScan` imports to `org.springframework.boot.persistence.autoconfigure.EntityScan`.
- Spring Boot 4: `TestRestTemplate` is no longer available; migrated `mill-security-auth-service` integration tests to `WebTestClient` and added `spring-webflux` to the testIT classpath.
- Spring Framework 7: updated 422 constant expectation to `HttpStatus.UNPROCESSABLE_CONTENT` in registration IT.

Proof (repo root):

- `./gradlew :security:mill-security:test :security:mill-security-auth-service:test :security:mill-security-autoconfigure:test :security:mill-security-persistence:test :security:mill-service-security:test` — **BUILD SUCCESSFUL**
- `./gradlew :security:mill-security-auth-service:testIT :security:mill-security-autoconfigure:testIT :security:mill-security-persistence:testIT` — **BUILD SUCCESSFUL**
- `./gradlew :services:mill-data-grpc-service:testIT` — **BUILD SUCCESSFUL**
