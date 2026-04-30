# WI-206 — Spring Security 7 upgrade fixes + tests

Status: `planned`  
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

- `./gradlew :security:test`
- `./gradlew :security:testIT`
- `./gradlew :services:mill-data-grpc-service:testIT` (gRPC security interceptor behavior)

## Acceptance Criteria

- All proof commands above are green.
- No ad-hoc version pins for security artifacts drift away from the Boot BOM line.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) §9.
- [`docs/design/platform/spring4-boot4-jump-start-inventory.md`](../../../design/platform/spring4-boot4-jump-start-inventory.md) (paths list).
