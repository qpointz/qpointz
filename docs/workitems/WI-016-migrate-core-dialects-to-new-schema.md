# WI-016 — Migrate Core Dialects to New YAML Schema

Status: `planned`  
Type: `🔧 refactoring`  
Area: `core`, `client`, `ai`  
Backlog refs: `C-6`, `C-8`, `C-9`

## Problem Statement

The new schema foundation from WI-015 is not yet validated by real dialect content. Existing
dialect files in `mill-core` are legacy-format and incomplete for several consumer requirements.
We need migrated, schema-complete dialect definitions in `core/mill-sql` before Kotlin runtime
and API work can proceed safely.

## Goal

Migrate the four priority dialects to the new schema format under `core/mill-sql`:

- `POSTGRES`
- `H2`
- `CALCITE`
- `MYSQL`

These migrated files become the runtime baseline for subsequent WIs.

## In Scope

1. Create new-schema dialect YAMLs in `core/mill-sql` for the four target dialects.
2. Port content from legacy `mill-core` YAMLs as seed input, then normalize to new schema.
3. Fix known correctness issues during migration (especially H2 and Calcite gaps).
4. Ensure required sections are populated according to WI-015 schema and coverage rules.
5. Add migration notes and assumptions per dialect (doc block or sidecar docs).
6. Add schema validation checks/tests for these four migrated dialects.

## Out of Scope

- Kotlin typed model and loader implementation.
- gRPC/HTTP `GetDialect` contracts and server endpoints.
- AI or Python runtime consumer switching.
- Migrating remaining non-priority dialects.
- Any edits in `mill-core`.

## Implementation Plan

1. **Seed and structure**
   - Copy baseline dialect content from `mill-core` into `core/mill-sql` resources.
   - Reformat into target schema structure.
2. **Normalize and enrich**
   - Fill missing mandatory sections and flags.
   - Convert deprecated/legacy fields to new equivalents.
3. **Dialect-specific corrections**
   - Apply known H2 and Calcite fixes captured in design docs.
   - Validate function/operator categories against new schema taxonomy.
4. **Validation harness**
   - Add parser/schema checks for all four migrated files.
   - Fail on missing required fields and invalid enums/keys.
5. **Migration artifacts**
   - Document what was carried forward vs corrected vs intentionally deferred.

## Acceptance Criteria

- `POSTGRES`, `H2`, `CALCITE`, and `MYSQL` YAML files exist in `core/mill-sql` in new format.
- Each file passes strict schema validation checks.
- Known high-priority issues from legacy files are corrected or explicitly tracked as follow-up.
- Migrated files are ready for typed Kotlin loading in WI-017.
- `mill-core` remains unchanged.

## Test Plan (during implementation)

### Schema Validation

- Validate each migrated YAML against required schema sections and field constraints.
- Add negative tests for invalid enum/shape/missing-required-field scenarios.

### Content Sanity

- Verify presence of key sections: identifiers, feature-flags, functions, operators, type-info.
- Spot-check high-risk fields (identifier storage, paging, null sorting, window/statistics flags).

## Risks and Mitigations

- **Risk:** Hidden semantic drift while reformatting legacy YAMLs.  
  **Mitigation:** Keep per-dialect migration notes and compare critical capabilities before/after.

- **Risk:** Overfilling fields with unverified values.  
  **Mitigation:** Use explicit unknown/conservative defaults where evidence is missing.

## Deliverables

- This work item definition (`docs/workitems/WI-016-migrate-core-dialects-to-new-schema.md`).
- Four migrated dialect YAMLs in `core/mill-sql`.
- Validation checks for the migrated set.
