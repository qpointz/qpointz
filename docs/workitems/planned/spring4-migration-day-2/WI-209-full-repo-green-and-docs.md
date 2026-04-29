# WI-209 — Full repo green + migration doc status

Status: `planned`  
Type: `refactoring`  
Area: `platform`, `docs`  
Backlog refs: `P-5`, `P-7`, `P-8`, `P-9`  
Depends on: WI-202–WI-208

## Goal

Make the entire repository green on Boot 4 and update platform documentation to reflect the new state.

## Scope

- Run full verification suite, fix remaining failures.\n- Update [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md):\n  - Phase 2/3 checkboxes\n  - version table\n  - notes that are now “done” vs “Boot 4 day”\n- Update backlog status rows if your workflow requires it at story closure (per `docs/workitems/RULES.md`).\n
## Proof commands (run on the implementation branch)

- `./gradlew clean build`\n- `./gradlew test`\n- `./gradlew testIT`\n
## Acceptance Criteria

- All three proof commands above are green.\n- Migration plan is consistent with the codebase after the Boot 4 bump.\n
