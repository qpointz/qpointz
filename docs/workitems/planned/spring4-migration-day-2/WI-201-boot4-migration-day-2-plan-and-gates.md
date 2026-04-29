# WI-201 — Boot 4 migration day 2: plan and gates

Status: `planned`  
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
- `./gradlew testIT`

## Execution order (default)

1. Boot BOM bump first (compile breaks surface early).
2. Spring AI milestone upgrade (known blocker).
3. SpringDoc 3.x bump (Boot 4 compatible).
4. Jackson 3 mechanical refactor (broadest sweep).
5. Spring Security 7 fixes.
6. Starter renames and residual dependency graph cleanup.
7. Transport + client re-validation (gRPC + JDBC + HTTP).

## Acceptance Criteria

- Story WIs reflect the order above and have concrete “proof” commands.
- The implementation branch can follow WIs in order with a monotonic “more green” trajectory.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md)
- [`docs/design/platform/spring4-boot4-jump-start-inventory.md`](../../../design/platform/spring4-boot4-jump-start-inventory.md)

