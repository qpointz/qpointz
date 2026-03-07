# WI-026 — JDBC Full `DatabaseMetaData` Implementation

Status: `completed`  
Type: `✨ feature`  
Area: `client`, `data`, `core`  
Backlog refs: `D-8`, `C-23`

## Problem Statement

The JDBC driver currently has partial/stubbed metadata behavior in several capability areas.
BI tools and JDBC consumers depend on accurate `DatabaseMetaData` responses across identifier
rules, catalog/schema topology, limits, transaction semantics, result-set capabilities, feature
flags, and type information. Incomplete metadata causes tool incompatibility and incorrect SQL
feature assumptions.

## Goal

Implement full JDBC metadata behavior so the driver exposes a complete and consistent
`DatabaseMetaData` surface backed by the v2 dialect runtime (`core/mill-sql`) and transport
contracts (`GetDialect`).

## In Scope

1. Implement/complete `DatabaseMetaData` methods using dialect descriptor values:
   - identifier quoting/casing/storage methods
   - schema/catalog support methods
   - transaction capability/isolation methods
   - limits (`getMax*`) methods
   - result-set type/concurrency capability methods
   - SQL grammar/capability methods
2. Implement/complete `getTypeInfo()` from dialect `type-info` entries.
3. Ensure metadata responses are consistent with server `GetDialect` values.
4. Add compatibility tests for common JDBC consumers and metadata probes.
5. Remove legacy hard-coded defaults that conflict with dialect-driven values.

## Out of Scope

- Non-JDBC client work (Python/ibis/SQLAlchemy implementation details).
- New SQL execution engine behavior unrelated to metadata reporting.
- UI or AI metadata consumers.

## Dependencies

- WI-018 / WI-019 completed (`GetDialect` contracts + server implementation).
- WI-021 (Python remote dialect consumption) is not a hard dependency but should follow same
  dialect-contract semantics.

## Implementation Plan

1. **Inventory and mapping**
   - Enumerate `DatabaseMetaData` methods and map each to dialect model fields or conservative
     fallbacks.
2. **Dialect-driven metadata layer**
   - Add normalization/mapping helpers from `DialectDescriptor`/`SqlDialectSpec` to JDBC methods.
3. **Type info**
   - Implement `getTypeInfo()` row generation from dialect `type-info`.
4. **Conformance testing**
   - Add tests for critical JDBC metadata calls used by BI tools and SQL clients.
5. **Regression hardening**
   - Compare behavior against prior known issues and ensure no stubbed false defaults remain.

## Acceptance Criteria

- Core `DatabaseMetaData` capability methods return dialect-consistent values.
- `getTypeInfo()` returns stable rows mapped from dialect type metadata.
- Limits, transactions, identifier semantics, and result-set capabilities are fully populated.
- Integration tests cover representative metadata probes and pass on both grpc/http transports.
- Documented known JDBC metadata stubs are removed or explicitly justified.

## Test Plan (during implementation)

### Unit

- Method-level tests for metadata mapping logic.
- Type-info conversion tests (jdbc code, precision/scale, flags).

### Integration

- Driver integration tests running metadata probes against live Mill service.
- Cross-check tests asserting parity between `GetDialect` payload and `DatabaseMetaData` values.
- Compatibility smoke tests for representative JDBC tooling patterns.

## Risks and Mitigations

- **Risk:** Ambiguity where JDBC API expects values not present in dialect model.  
  **Mitigation:** Define conservative defaults and document explicit mapping policy.

- **Risk:** Divergent behavior across transports or server versions.  
  **Mitigation:** Add transport parity checks and versioned capability handling.

- **Risk:** Large metadata surface leads to missed methods.  
  **Mitigation:** Use an explicit method inventory checklist with test coverage mapping.

## Deliverables

- This work item definition (`docs/workitems/WI-026-jdbc-full-metadata-implementation.md`).
- JDBC metadata implementation updates in driver modules.
- Conformance and regression test coverage for metadata behavior.
