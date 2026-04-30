# WI-204 — Upgrade SpringDoc OpenAPI to 3.0.3 (Boot 4 compatible)

Status: `planned`  
Type: `refactoring`  
Area: `services`, `platform`  
Backlog refs: `P-8`  
Depends on: WI-202, WI-203

## Goal

Upgrade SpringDoc to **3.0.3** and keep all SpringDoc usage centralized in `libs.versions.toml`.

## Scope

- Update `springDoc` in `libs.versions.toml` to `3.0.3`.
- Ensure all modules use version catalog (`libs.springdoc.*`).
- Fix any runtime/test failures in `apps/mill-service`, metadata/schema services, and AI services.

## Proof commands (run on the implementation branch)

- `./gradlew :apps:mill-service:test`
- `./gradlew :metadata:mill-metadata-service:test`
- `./gradlew :data:mill-data-schema-service:test`
- `./gradlew :ai:mill-ai-v3-service:test`

## Acceptance Criteria

- All proof commands above are green.
- No hardcoded `org.springdoc:*:<version>` dependencies exist in build scripts.
## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) §5.
