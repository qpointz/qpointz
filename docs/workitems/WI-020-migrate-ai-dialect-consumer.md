# WI-020 — Migrate AI Dialect Consumer to New Typed Runtime

Status: `completed`  
Type: `🔧 refactoring`  
Area: `ai`, `core`  
Backlog refs: `C-7`

## Problem Statement

AI dialect usage is currently tied to legacy dialect handling and does not yet consume the new
typed runtime foundation. Keeping parallel implementations increases drift risk and makes prompt
behavior inconsistent with server-exposed dialect metadata.

## Goal

Migrate AI dialect consumption to the new typed dialect implementation (sourced from
`core/mill-sql`) and remove reliance on legacy dialect access paths.

## In Scope

1. Replace legacy AI dialect reader/adapter path with new typed-model-based adapter.
2. Ensure prompt builder consumes normalized fields from the new model.
3. Preserve or intentionally improve existing prompt semantics with documented diffs.
4. Add regression coverage for representative prompt outputs and capability sections.

## Constraints

- Do not modify `mill-core`.
- New dialect source is `core/mill-sql` model path exposed by current runtime architecture.

## Out of Scope

- New dialect contracts and server endpoint implementation (already delivered by prior WIs).
- Python client migration.
- Remaining dialect migration waves.

## Implementation Plan

1. **Adapter migration**
   - Implement typed adapter from runtime model to AI prompt dialect structure.
2. **Consumer switch**
   - Replace legacy `SpecSqlDialect`-style usage in AI prompt path.
3. **Regression safety**
   - Add snapshot/fixture tests for prompt sections impacted by dialect metadata.
4. **Cleanup**
   - Mark or remove legacy AI dialect plumbing once parity is confirmed.

## Acceptance Criteria

- AI prompt generation uses new typed dialect source path.
- Legacy AI dialect path is no longer required for runtime behavior.
- Regression tests demonstrate parity or documented intentional deltas.

## Test Plan (during implementation)

### Unit

- Adapter mapping tests for identifiers, feature flags, functions, and operators.

### Integration

- End-to-end prompt generation tests for at least migrated dialect set.
- Regression snapshots for key query categories (joins, window, set ops, literals).

## Risks and Mitigations

- **Risk:** Prompt behavior regressions from field mapping differences.  
  **Mitigation:** Snapshot baselines and explicit change notes for intentional differences.

- **Risk:** Hidden dependencies on legacy field names.  
  **Mitigation:** Search-based cleanup and targeted integration tests.

## Deliverables

- This work item definition (`docs/workitems/WI-020-migrate-ai-dialect-consumer.md`).
- AI dialect consumer migrated to typed runtime model path.
- Regression and parity test coverage for prompt behavior.
