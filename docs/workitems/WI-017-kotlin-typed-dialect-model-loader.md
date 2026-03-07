# WI-017 — Kotlin Typed Dialect Model + YAML Loader

Status: `completed`  
Type: `✨ feature`  
Area: `core`  
Backlog refs: `D-8` (foundation)

## Problem Statement

Even with migrated YAMLs, dialect handling is not production-ready until runtime uses a strict
typed Kotlin model. Dynamic map-style access is fragile and makes contracts harder to evolve and
validate.

## Goal

Implement a full typed Kotlin dialect runtime model in `core/mill-sql` and load dialect metadata
from migrated YAML resources.

## In Scope

1. Add strict Kotlin model classes/records matching the full YAML schema.
2. Implement YAML parsing + validation + defaulting into typed model objects.
3. Add `DialectRegistry` (or equivalent) to expose loaded dialect descriptors.
4. Add startup/load diagnostics for missing/invalid dialect files.
5. Add comprehensive unit tests for model binding and validation behavior.

## Constraints

- Do not modify `mill-core`.
- Runtime read path uses `core/mill-sql` resources only.

## Out of Scope

- `GetDialect` proto/HTTP contract definition.
- Server endpoint implementation.
- AI consumer migration and Python consumer migration.
- Migration of remaining dialects beyond WI-016 scope.

## Implementation Plan

1. **Model implementation**
   - Create typed classes for all schema blocks (identifiers, catalog/schema, transactions, limits,
     feature flags, operators, functions, type info, result-set caps, paging).
2. **Loader/validator**
   - Implement YAML deserializer and structural validator with explicit error messages.
   - Apply defaults where schema allows omission.
3. **Registry**
   - Build runtime registry that indexes dialects by id and returns immutable descriptors.
4. **Testing**
   - Golden tests for successful loading of four migrated dialects.
   - Failure tests for malformed YAML and invalid values.

## Acceptance Criteria

- Full typed Kotlin model exists in `core/mill-sql`.
- All four migrated dialects load into typed model successfully.
- Invalid YAML inputs fail with actionable diagnostics.
- No runtime dependency on legacy map-based dialect access.
- `mill-core` remains unchanged.

## Test Plan (during implementation)

### Unit

- Per-section binding tests for all major schema blocks.
- Defaulting tests for optional fields.
- Validation tests for bad enum values, missing required fields, unknown shapes.

### Integration (module-level)

- End-to-end load test for `POSTGRES`, `H2`, `CALCITE`, `MYSQL` from resources.

## Risks and Mitigations

- **Risk:** Model/schema mismatch between docs and implementation.  
  **Mitigation:** Keep one-to-one mapping table in tests and fail on unmapped schema fields.

- **Risk:** Overly permissive parser masks bad configs.  
  **Mitigation:** Prefer strict parsing and explicit fallback defaults only where documented.

## Deliverables

- This work item definition (`docs/workitems/WI-017-kotlin-typed-dialect-model-loader.md`).
- Typed Kotlin dialect model and loader in `core/mill-sql`.
- Registry and tests for migrated dialect set.
