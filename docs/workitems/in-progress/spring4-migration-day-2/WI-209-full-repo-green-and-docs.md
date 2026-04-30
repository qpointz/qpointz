# WI-209 — Full repo green + migration doc status

Status: `planned`  
Type: `refactoring`  
Area: `platform`, `docs`  
Backlog refs: `P-5`, `P-7`, `P-8`, `P-9`  
Depends on: WI-202, WI-203, WI-204, WI-205, WI-206, WI-207, WI-208

## Goal

Make the entire repository green on Boot 4 and update platform documentation to reflect the new state.

## Scope

- Run full verification suite, fix remaining failures.
- Update [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md):
  - Phase 2/3 checkboxes
  - version table
  - notes that are now “done” vs “Boot 4 day”
- Update backlog status rows if your workflow requires it at story closure (per `docs/workitems/RULES.md`).

## Proof commands (run on the implementation branch)

- `./gradlew clean build`
- `./gradlew test`
- `./gradlew testIT` (manual verification; include confirmation in WI completion notes / MR)

## Acceptance Criteria

- `./gradlew clean build` and `./gradlew test` are green.
- `./gradlew testIT` was run manually and the result is recorded (WI completion notes / MR description).
- Migration plan is consistent with the codebase after the Boot 4 bump.
