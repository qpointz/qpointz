# WI-023 — ibis Dialect Correctness Validation and Certification

Status: `planned`  
Type: `🧪 test`  
Area: `client`, `data`, `core`  
Backlog refs: `C-3`, `C-4`, `C-5`, `C-10`, `C-11`

## Problem Statement

The dialect schema is now representable and transport-accessible, but ibis backend behavior still
depends on correctness of declared dialect capabilities. Today, several ibis-critical areas
(window functions, statistics, extended math, and related flags) are not validated end-to-end
against live parser/query behavior for each target dialect.

Without a correctness gate, YAML declarations can drift from runtime reality and cause ibis SQL
generation regressions.

## Goal

Establish a repeatable validation and certification flow that verifies dialect capability
correctness for ibis-critical features and promotes only validated capabilities into
dialect-dependent client behavior.

## In Scope

1. Implement or finalize `DialectTester` execution flow against live Mill backends.
2. Validate ibis-critical capability groups:
   - window functions (`functions.window`, `supports-window-functions`)
   - statistical aggregates (`functions.statistics`)
   - extended math functions (`functions.math`)
   - set operations and CTE/QUALIFY/lateral/semi-anti flags where applicable
3. Generate machine-readable and markdown reports from test outcomes.
4. Add CI gates that fail on mismatch between declared YAML capabilities and observed behavior.
5. Define per-dialect certification status output (for example: `certified-for-ibis`).

## Out of Scope

- Full ibis backend implementation details unrelated to dialect validation plumbing.
- Non-ibis consumer behavior changes (SA/JDBC/AI) except shared report reuse.
- Adding brand new dialects beyond current migration scope unless explicitly requested.

## Implementation Plan

1. **Capability test inventory**
   - Lock query inventory for ibis-critical features and map each test to schema path(s).
2. **Execution harness**
   - Run generated SQL via `ParseSql` and targeted runtime checks via `Query` where needed.
3. **Result normalization**
   - Produce structured results (`supported`/`unsupported`/`partial`/`error`) and aggregate flags.
4. **Drift detection**
   - Compare observed support with YAML-declared values and flag divergence.
5. **Certification output**
   - Publish per-dialect certification summary and store as CI artifact.
6. **Governance**
   - Document update flow: when dialect YAML changes, validation must run in same change set.

## Acceptance Criteria

- A deterministic validation suite exists for ibis-critical dialect capabilities.
- CI fails when declared YAML capabilities conflict with observed parser/runtime behavior.
- `py-sql-dialect-report.md` (or equivalent report artifact) is generated from latest test run.
- Each of the four core dialects (`CALCITE`, `POSTGRES`, `H2`, `MYSQL`) has explicit ibis
  validation status.
- ibis backend work can consume validated capability flags instead of assumed defaults.

## Test Plan (during implementation)

### Unit

- Mapping tests from validation results to feature-flag/function-catalog updates.
- Drift detector tests for true/false/null and missing-key scenarios.

### Integration

- Full tester run against live Mill with representative schema.
- Golden report diff checks to detect unexpected capability regressions.
- CI smoke run proving failure on intentionally injected mismatch.

## Risks and Mitigations

- **Risk:** Environment-dependent failures produce flaky validation outcomes.  
  **Mitigation:** Stabilize fixture schema/data and isolate non-deterministic cases.

- **Risk:** Parser success may not reflect runtime execution semantics for some features.  
  **Mitigation:** Use targeted `Query` assertions for semantic cases (for example division).

- **Risk:** Report churn causes noisy reviews.  
  **Mitigation:** Keep report format stable and separate deterministic fields from timestamps.

## Deliverables

- This work item definition (`docs/workitems/WI-023-ibis-dialect-correctness-validation.md`).
- Executable validation harness + CI gate for dialect correctness.
- Generated certification report artifact for core dialect set.
