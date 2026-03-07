# WI-015 — Core `mill-sql` Bootstrap + Feature-Complete YAML Schema

Status: `completed`  
Type: `✨ feature`  
Area: `core`, `client`, `ai`  
Backlog refs: `C-6`, `C-8`, `C-9` (foundation), `D-8` (downstream)

## Problem Statement

Current dialect definitions were introduced primarily for AI prompt generation and do not fully
cover the requirements captured in section 3 of
`docs/design/client/py-sql-dialect-plan.md`. At the same time, runtime consumers are expected to
move to a strict typed Kotlin model, and the team wants to avoid modifying `mill-core` during
this migration program.

## Goal

Establish a new dialect foundation in `core/mill-sql` by:

1. bootstrapping a dedicated module for dialect ownership,
2. defining the full-feature YAML schema as the target format,
3. proving coverage against section 3 and section 4 requirements (representability-first),
4. seeding new resources from current `mill-core` YAMLs as starting input only.

`mill-core` remains unchanged in this WI.

## In Scope

1. Create new Gradle module `core/mill-sql` (minimal/empty Kotlin code is acceptable in this WI).
2. Add resource layout in `core/mill-sql` for dialect YAMLs and schema-related artifacts.
3. Define/lock the target YAML schema in `docs/design/client/sql-dialect-yaml-schema.md`
   as the canonical format for future runtime loading.
4. Add a feature coverage matrix mapping section 3 requirements from
   `docs/design/client/py-sql-dialect-plan.md` to schema fields.
5. Add a gap coverage matrix mapping section 4 gaps (B1..B8, numeric gaps) to schema fields.
6. Seed dialect YAMLs in `core/mill-sql` from existing `mill-core` dialect YAMLs
   (starting point only; no runtime migration yet).
7. Document migration rules:
   - old format is legacy,
   - new format is target,
   - migration waves happen in later WIs.
8. Add basic validation guidance/test scaffolding (schema validation test plan and sample checks).

## Out of Scope

- Implementing the strict Kotlin runtime dialect object model and YAML loader.
- Adding `GetDialect` gRPC/HTTP contracts.
- Wiring server endpoints to serve dialect metadata.
- Migrating AI runtime consumption to the new typed implementation.
- Switching Python client to remote dialect fetch.
- Editing files under `mill-core`.

## Implementation Plan

1. **Module bootstrap**
   - Create `core/mill-sql` Gradle module and include it in the multi-module build.
   - Add placeholder package structure for future typed model implementation.
2. **Resource baseline**
   - Create dialect resources folder under `core/mill-sql`.
   - Copy seed YAMLs from `mill-core` dialect resources as input baseline (unchanged semantics).
3. **Schema finalization**
   - Update `sql-dialect-yaml-schema.md` to represent the final target schema (v2).
   - Mark legacy keys/sections as deprecated, with explicit removal plan in future WI.
4. **Coverage proof**
   - Add requirement coverage table mapping each section-3 aspect/gap to schema fields.
   - Add gap coverage table mapping each section-4 gap id (`B1..B8`, `11`, `12`, `16`, `24`,
     `27`, `29`, `30`, `33`, `34`, `35`, `36`, `37`, `38`) to schema fields.
   - Mark each item as either `representable` or `not representable`, with follow-up blocker list.
5. **Migration contract**
   - Document rules for upcoming migration of `POSTGRES`, `H2`, `CALCITE`, `MYSQL`.
   - State that migrated files in `core/mill-sql` become runtime input in later WIs.

## Acceptance Criteria

- `core/mill-sql` module exists and participates in build successfully.
- Dialect resource structure exists in `core/mill-sql`, seeded from existing YAML definitions.
- `sql-dialect-yaml-schema.md` defines a full target format, not a patch-on-legacy shape.
- Coverage matrices demonstrate section-3 and section-4 coverage as representability mapping.
- Every section-3/4 item is explicitly classified as representable or not representable.
- Schema can express dialect metadata categories even when values are still unknown/unverified.
- Migration policy is clear: `mill-core` untouched; new work proceeds in `core/mill-sql`.

## Test Plan (during implementation)

### Build/Module

- Run module build/task discovery to verify `core/mill-sql` integration.

### Schema/Resources

- Validate YAML examples for parseability and structural conformity to documented schema.
- Add lightweight checks ensuring seeded resource files are discoverable in `core/mill-sql`.

### Documentation Quality

- Verify every section-3 and section-4 item is either mapped to schema fields or marked open.
- Verify deprecated legacy fields are explicitly documented with migration intent.

## Risks and Mitigations

- **Risk:** Schema over-expansion without realistic runtime implementation path.  
  **Mitigation:** Keep schema fields tied directly to section-3 consumer requirements.

- **Risk:** Ambiguity between seed YAMLs and final YAML schema shape.  
  **Mitigation:** Clearly separate “seed baseline” from “migrated target format” states.

- **Risk:** Accidental edits to `mill-core`.  
  **Mitigation:** Explicit no-change constraint for `mill-core` in WI scope and review checklist.

## Deliverables

- This work item definition (`docs/workitems/WI-015-core-mill-sql-schema-foundation.md`).
- New module scaffold under `core/mill-sql`.
- Updated schema design document and coverage mapping.
- Seed dialect resources in `core/mill-sql` copied from `mill-core` as baseline.
