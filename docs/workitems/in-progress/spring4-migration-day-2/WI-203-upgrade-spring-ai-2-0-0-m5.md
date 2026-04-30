# WI-203 — Upgrade Spring AI to 2.0.0-M5 (Boot 4 blocker)

Status: `planned`  
Type: `refactoring`  
Area: `ai`, `platform`  
Backlog refs: `P-8`  
Depends on: WI-202

## Goal

Upgrade Spring AI to **2.0.0-M5** and make all AI modules compile and pass tests under Boot 4.

## Scope

- Update `springAi` in `libs.versions.toml` to `2.0.0-M5`.
- Repair artifact coordinate changes and API breakages.
- Ensure AI services and `apps/mill-service` wiring remains valid.

## Proof commands (run on the implementation branch)

- AI-only:
  - `./gradlew :ai:test`
  - `./gradlew :ai:testIT`
- Aggregator smoke:
  - `./gradlew :apps:mill-service:test`

## Acceptance Criteria

- All proof commands above are green.
- No AI modules are excluded/disabled to “get green” (full repo green is required by WI-209).
## References

- Spring AI release notes (2.0.0-M5) and migration guidance.
- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) §2.

