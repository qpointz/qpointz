# WI-201 — Boot 4 migration day 2: plan and gates

Status: `done`  
Type: `docs`  
Area: `platform`  
Backlog refs: `P-5`, `P-7`, `P-8`, `P-9`  
Depends on: none

## Goal

Lock the **target versions**, the **MR/test gates**, and the **execution order** so the implementation
branch does not thrash on version selection.

## Decisions / targets

- **Spring Boot**: `4.0.6`
- **Spring AI**: `2.0.0-M5` (milestone)
- **SpringDoc OpenAPI**: `3.0.3`
- **Jackson**: `3.0` line (via Boot 4) with explicit catalog cleanup (`tools.jackson:*`)

## MR gates (must be green by WI-209)

- `./gradlew build`
- `./gradlew test`

## Manual verification (record confirmation in WI-209 notes / MR description)

- `./gradlew testIT`

## Execution order (default)

1. Boot BOM bump first (compile breaks surface early).
2. Starter renames and residual dependency graph cleanup (reduce downstream churn).
3. Spring AI milestone upgrade (known blocker).
4. SpringDoc 3.x bump (Boot 4 compatible; depends on AI service compilation).
5. Jackson 3 mechanical refactor (broadest sweep).
6. Spring Security 7 fixes.
7. Transport + client re-validation (gRPC + JDBC + HTTP).

## Acceptance Criteria

- Story WIs reflect the order above and have concrete “proof” commands.
- The implementation branch can execute WIs in order with a monotonic “more green” trajectory on the
  **defined per-WI proof commands** (full-repo green is required only at **WI-209**).

## Completion notes (WI-201)

- WIs ordered in `STORY.md` to satisfy `Depends on:` edges (notably: starter renames before transport
  re-validation; SpringDoc depends on Spring AI).
- MR gates clarified: `build` + `test` are hard gates; `testIT` is required manual verification
  recorded in WI-209 / MR description.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md)
- [`docs/design/platform/spring4-boot4-jump-start-inventory.md`](../../../design/platform/spring4-boot4-jump-start-inventory.md)

