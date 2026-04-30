# WI-209 — Full repo green + migration doc status

Status: `done`  
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

## Completion notes (2026-04-30)

**CI / verification (green):**

- `./gradlew clean build` — green (CI/CD).
- `./gradlew test` — green (CI/CD).
- `./gradlew testIT` — green (CI/CD including integration suites).

**Design / public docs:**

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) — **Repository version baseline** table aligned with **`libs.versions.toml`** (Boot **4.0.6**, Jackson **3.1.2**, Spring AI **2.0.0-M5**, SpringDoc **3.0.3**, Security **7**); **Phase 1** starter renames marked complete (**WI-207**); **Phases 2–3** checkboxes reconciled with **WI-202**–**WI-208**; **Phase 4** gates satisfied with green CI above; blocker sections (gRPC, Spring AI, Jackson) updated from “future” to “mitigated / done on branch”; JDBC **`testIT`** appendix aligned with **`EmbeddedSkymillGrpcServer`**.
- [`docs/public/src/reference/platform-runtime.md`](../../../public/src/reference/platform-runtime.md) — user-facing stack summary + verify commands.
- [`docs/public/src/installation.md`](../../../public/src/installation.md) — Java **21**, `./gradlew` entry points, links to platform runtime + migration plan.
- [`docs/public/mkdocs.yml`](../../../public/mkdocs.yml) — nav entry for **Platform runtime**.
- [`docs/public/src/index.md`](../../../public/src/index.md) — links under **Documentation**.
- [`docs/workitems/BACKLOG.md`](../../BACKLOG.md) — **P-5**, **P-8**, **P-9** set to **`done`** with story/WI pointers.

## Proof commands (run on the implementation branch)

- `./gradlew clean build`
- `./gradlew test`
- `./gradlew testIT` (manual verification; include confirmation in WI completion notes / MR)

## Acceptance Criteria

- `./gradlew clean build` and `./gradlew test` are green. **Met (CI/CD).**
- `./gradlew testIT` was run manually and the result is recorded (WI completion notes / MR description). **Met (CI/CD).**
- Migration plan is consistent with the codebase after the Boot 4 bump. **Met.**
