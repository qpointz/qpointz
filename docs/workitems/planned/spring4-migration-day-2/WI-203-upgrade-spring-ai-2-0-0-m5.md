# WI-203 — Upgrade Spring AI to 2.0.0-M5 (Boot 4 blocker)

Status: `planned`  
Type: `refactoring`  
Area: `ai`, `platform`  
Backlog refs: `P-8`  
Depends on: WI-202

## Goal

Upgrade Spring AI to **2.0.0-M5** and make all AI modules compile and pass tests under Boot 4.

## Scope

- Update `springAi` in `libs.versions.toml` to `2.0.0-M5`.\n- Repair artifact coordinate changes and API breakages.\n- Ensure AI services and `apps/mill-service` wiring remains valid.\n
## Proof commands (run on the implementation branch)

- AI-only:\n  - `./gradlew :ai:test`\n  - `./gradlew :ai:testIT`\n- Aggregator smoke:\n  - `./gradlew :apps:mill-service:test`\n
## Acceptance Criteria

- `ai/*` builds and tests are green under Boot 4.\n- No temporary “skip AI” gates remain (full repo green is required by WI-209).\n
## References

- Spring AI release notes (2.0.0-M5) and migration guidance.\n- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) §2.

