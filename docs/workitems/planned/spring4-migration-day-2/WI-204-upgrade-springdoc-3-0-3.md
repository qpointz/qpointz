# WI-204 — Upgrade SpringDoc OpenAPI to 3.0.3 (Boot 4 compatible)

Status: `planned`  
Type: `refactoring`  
Area: `services`, `platform`  
Backlog refs: `P-8`  
Depends on: WI-202

## Goal

Upgrade SpringDoc to **3.0.3** and keep all SpringDoc usage centralized in `libs.versions.toml`.

## Scope

- Update `springDoc` in `libs.versions.toml` to `3.0.3`.\n- Ensure all modules use version catalog (`libs.springdoc.*`).\n- Fix any runtime/test failures in `apps/mill-service`, metadata/schema services, and AI services.\n
## Proof commands (run on the implementation branch)

- `./gradlew :apps:mill-service:test`\n- `./gradlew :metadata:mill-metadata-service:test`\n- `./gradlew :data:mill-data-schema-service:test`\n- `./gradlew :ai:mill-ai-v3-service:test`\n
## Acceptance Criteria

- All SpringDoc modules compile and tests pass under Boot 4.\n- No hardcoded `org.springdoc:*:<version>` dependencies exist in build scripts.\n
## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) §5.\n
