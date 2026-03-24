# WI-104 — Refresh `spring4-migration-plan.md` Phase 1 status

Status: `planned`  
Type: `docs`  
Area: `platform`  
Backlog refs: `P-5`  
Depends on: **WI-097**–**WI-103** (content should reflect completed WIs)

## Problem Statement

[`docs/design/platform/spring4-migration-plan.md`](../../design/platform/spring4-migration-plan.md) Phase 1
checkboxes, module paths, and Appendix A still describe **older layout** (`data/mill-data-grpc-service`,
side-by-side `mill-data-grpc-service-v2`, `mill.services.grpc.*` in places) and may mark items undone
that the repo already finished (e.g. `spring.factories` removal).

## Goal

- Align the design document with **current repository facts** after this story’s WIs land.
- Clearly separate: **done on 3.5.x pre-migration story** vs **requires Boot 4 bump** (Jackson 3,
  Spring AI 2, SpringDoc 3, Security 7, starter renames).

## Scope

1. Update paths: `services/mill-data-grpc-service`, `io.qpointz.mill.data.backend.grpc.*`,
   `mill.data.services.grpc.*` property prefix where applicable.
2. Phase 1 checklist: check off completed items; move deferred items to “Boot 4 day” if appropriate.
3. Phase 2: correct the gRPC bullet — raw **grpc-java** (per Appendix A), not “Spring gRPC 1.0.0”, unless
   product direction explicitly chooses Spring gRPC later.
4. Appendix A: mark production inventory as **superseded** by current module layout or keep as
   historical with a banner.

## Acceptance Criteria

- A reader can tell **what remains before** vs **only after** `springBoot` → `4.0.x` in `libs.versions.toml`.
- No stale path references to removed modules without an explicit “historical” label.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../design/platform/spring4-migration-plan.md)
