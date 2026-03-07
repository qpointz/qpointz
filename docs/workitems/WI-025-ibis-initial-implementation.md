# WI-025 — Python ibis Initial Implementation (BaseBackend + SQL Compilation)

Status: `planned`  
Type: `✨ feature`  
Area: `client`  
Backlog refs: `C-10`, `C-11`, `C-22`

## Problem Statement

ibis is a target Python consumer, but there is no dedicated implementation WI that defines an
initial backend scope and success criteria. Current design is blocked less by schema shape and
more by correctness validation of dialect capabilities.

## Goal

Deliver an initial ibis backend for Mill that supports a practical subset of expressions and
executes against Mill transports, with explicit feature gating driven by dialect metadata.

## In Scope

1. Implement initial ibis `BaseBackend` integration over Mill client transport.
2. Implement expression-to-SQL compilation path compatible with Mill/Calcite constraints.
3. Wire dialect capability flags into compiler feature gating (CTE, set ops, qualifiers, etc.).
4. Support an initial function/operator subset with explicit unsupported behavior for gaps.
5. Add integration tests for key analytical/query paths and transport parity.
6. Document supported vs unsupported ibis operations for initial release.

## Out of Scope

- Full parity with mature SQL backends in ibis.
- Broad optimization work (cost-based rewrites, advanced pushdown tuning).
- Non-Python client work.

## Dependencies

- WI-021 (remote dialect consumption) for runtime metadata-driven behavior.
- WI-023 (ibis dialect correctness validation) for certified capability usage in CI.

## Implementation Plan

1. **Backend scaffold**
   - Implement connection lifecycle and backend registration.
2. **Compiler baseline**
   - Map initial ibis expression set to SQL (via sqlglot strategy where applicable).
3. **Capability gating**
   - Use dialect flags/catalog to allow/deny features deterministically.
4. **Execution and result mapping**
   - Execute generated SQL and map results to ibis expectations.
5. **Validation/reporting**
   - Integrate correctness validation outputs and fail on unsupported advertised features.
6. **Docs**
   - Publish supported feature matrix and known limitations.

## Acceptance Criteria

- Initial ibis backend can connect and execute representative query workflows.
- Compiler emits SQL aligned with dialect metadata and documented capability flags.
- Unsupported operations fail clearly with actionable messages.
- Integration tests cover core query families and grpc/http parity.
- Initial support matrix is documented and tied to validation output.

## Test Plan (during implementation)

### Unit

- Expression-to-SQL compiler tests for selected ibis nodes.
- Feature-gate tests (flag true/false/null behavior).

### Integration

- End-to-end ibis execution tests against representative schema.
- Transport parity tests between grpc/http.
- Regression checks against dialect correctness report artifacts.

## Risks and Mitigations

- **Risk:** Unsupported ibis expressions produce confusing failures.  
  **Mitigation:** Explicit capability checks and clear exception messages.

- **Risk:** SQL generation drifts from real dialect support.  
  **Mitigation:** Gate CI on WI-023 validation outputs.

- **Risk:** Initial scope grows too large.  
  **Mitigation:** Lock MVP subset and publish unsupported matrix.

## Deliverables

- This work item definition (`docs/workitems/WI-025-ibis-initial-implementation.md`).
- Initial ibis backend implementation and compiler mapping.
- Integration tests and support matrix documentation.
