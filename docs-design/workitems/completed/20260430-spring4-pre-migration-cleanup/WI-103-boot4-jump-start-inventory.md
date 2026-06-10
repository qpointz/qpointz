# WI-103 — Boot 4 jump-start inventory (grep-only checklist)

Status: `done`  
Type: `docs`  
Area: `platform`  
Backlog refs: `P-5`  
Depends on: none

## Problem Statement

Phase 3 of the migration plan calls out **mechanical** breakages: Jackson 3 packages, `PropertyMapper`
API, Boot internal package moves, Spring Security 7 tweaks. A **pre-upgrade inventory** reduces thrash
on the day the BOM jumps to 4.0.x.

## Goal

Produce a **single markdown artifact** (under `docs/design/platform/` or attached to this story)
listing:

- Files importing `com.fasterxml.jackson.databind.ObjectMapper` (candidates for `JsonMapper` / Jackson 3).
- Usages of `org.springframework.boot.context.config.PropertyMapper` (Boot 3.5 → 4 behavior change).
- Imports from historically internal Boot packages called out in the Boot 4 migration guide
  (`EnvironmentPostProcessor`, `BootstrapRegistry`, etc.) — adjust list per current guide.
- Spring Security configuration classes aligned with **§9** of the migration plan (paths only).

## Scope

1. Run structured greps; capture **file paths + line counts** (no code changes required in this WI).
2. Link the inventory from `spring4-migration-plan.md` Phase 3 (or fold into **WI-104** if preferred).

## Out of Scope

- Applying Boot 4 or changing dependencies.

## Acceptance Criteria

- Inventory file exists and is linked from the migration plan (or STORY closure notes).
- Reviewed once by a maintainer for obvious false positives.

## Completion notes (WI-103)

- Artifact: [`docs/design/platform/spring4-boot4-jump-start-inventory.md`](../../../design/platform/spring4-boot4-jump-start-inventory.md), linked from **`spring4-migration-plan.md`** Phase 3 and References.
- **False positives:** broader `ObjectMapper` file count includes tests and helper types — re-run greps at migration time. No `PropertyMapper` / `EnvironmentPostProcessor` / `BootstrapRegistry` hits in application sources at inventory time.

## References

- [`docs/design/platform/spring4-migration-plan.md`](../../../design/platform/spring4-migration-plan.md) — Phase 3, §9–§12
