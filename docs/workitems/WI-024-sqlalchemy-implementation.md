# WI-024 — Python SQLAlchemy Implementation (MillDialect + Compiler + Entry Points)

Status: `completed`  
Type: `✨ feature`  
Area: `client`  
Backlog refs: `C-12`, `C-13`, `C-14`

## Problem Statement

The Python client design includes SQLAlchemy support, but there is no dedicated implementation
work item that defines scope, dependencies, and acceptance criteria for delivering a working
Mill SQLAlchemy dialect.

## Goal

Implement a production-usable SQLAlchemy integration for Mill, including:

1. DBAPI compatibility layer integration,
2. SQLAlchemy dialect/compiler classes,
3. schema introspection plumbing,
4. package registration/entry points for both transports.

## In Scope

1. Implement Mill DBAPI glue required by SQLAlchemy engine creation and execution flow.
2. Implement SQLAlchemy dialect class (`MillDialect`) and compiler behavior (`MillSQLCompiler`)
   using server-driven dialect metadata.
3. Implement core introspection paths (`get_columns`, `get_table_names`, schema handling, type
   mapping).
4. Add URL/driver wiring and entry points for:
   - `mill+grpc://...`
   - `mill+http://...`
5. Add integration tests with representative ORM/Core query scenarios.
6. Add docs/examples for engine creation and basic usage.

## Out of Scope

- Full ibis backend implementation.
- Advanced SQLAlchemy extensions beyond initial Mill support surface.
- Non-Python client work.

## Dependencies

- WI-021 (remote dialect consumption) should be available or stubbed with equivalent mapping.
- WI-023 (dialect correctness validation) provides stronger correctness guarantees; this WI can
  start without full certification but must consume validated flags when available.

## Implementation Plan

1. **DBAPI foundation**
   - Finalize DBAPI objects and behavior expected by SQLAlchemy.
2. **Dialect/compiler**
   - Implement dialect class capabilities and compiler overrides based on dialect metadata.
3. **Reflection/introspection**
   - Map Mill schema APIs into SQLAlchemy inspector contracts.
4. **Packaging**
   - Register `sqlalchemy.dialects` entry points for grpc/http schemes.
5. **Verification**
   - Add unit and integration tests for Core + ORM paths.

## Acceptance Criteria

- `create_engine("mill+grpc://...")` and `create_engine("mill+http://...")` resolve and connect.
- Core SQL compilation/execution works for representative read-only query set.
- Introspection APIs return stable metadata for tables/columns/types.
- SQLAlchemy integration uses dialect metadata (paramstyle, quoting, capability flags) rather than
  hard-coded assumptions.
- Test suite covers compiler, transport, and reflection paths.

## Test Plan (during implementation)

### Unit

- Compiler rendering tests for quoting, paging, set ops, predicates.
- Dialect capability tests (flags -> SQLAlchemy behavior).

### Integration

- Engine/connection smoke tests for both transports.
- Reflection tests (`Inspector`) against representative schema.
- ORM/Core round-trip tests for selected query shapes.

## Risks and Mitigations

- **Risk:** SQLAlchemy behavior diverges across versions.  
  **Mitigation:** Pin/test supported version range and gate in CI.

- **Risk:** Transport-specific differences leak into dialect behavior.  
  **Mitigation:** Shared normalization layer and parity tests for grpc/http.

- **Risk:** Incomplete dialect metadata causes compiler mismatches.  
  **Mitigation:** Consume validated capabilities and keep conservative fallbacks.

## Deliverables

- This work item definition (`docs/workitems/WI-024-sqlalchemy-implementation.md`).
- SQLAlchemy dialect/compiler + DBAPI integration code.
- Entry point registration for grpc/http.
- Test coverage and usage documentation.
