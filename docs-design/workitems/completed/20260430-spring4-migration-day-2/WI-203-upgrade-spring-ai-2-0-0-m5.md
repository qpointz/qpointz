# WI-203 — Upgrade Spring AI to 2.0.0-M5 (Boot 4 blocker)

Status: `done`  
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

## Completion notes (2026-04-30)

Proof (repo root, Java 21):

- `./gradlew :ai:test` — **BUILD SUCCESSFUL**
- `./gradlew :ai:testIT` — **BUILD SUCCESSFUL** (`OPENAI_API_KEY` unset; OpenAI-specific ITs skip as designed)
- `./gradlew :apps:mill-service:test` — **BUILD SUCCESSFUL** (module reports `test` NO-SOURCE; task green)

Follow-up fix for v3 service ITs after Boot 4 persistence/JPA wiring: `AiChatServiceITApplication` had excluded `DataJpaRepositoriesAutoConfiguration`. With WI-202/WI-207, Mill persistence registers `io.qpointz.mill.persistence` via `AutoConfigurationPackages` only; repository beans are created by Boot’s `DataJpaRepositoriesAutoConfiguration`. Removing that exclude restores `ConversationRepository` (and siblings) for `AiChatControllerIT`.

## References

- Spring AI release notes (2.0.0-M5) and migration guidance.
- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) §2.
