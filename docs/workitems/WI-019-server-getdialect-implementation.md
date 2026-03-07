# WI-019 — Server `GetDialect` Implementation (gRPC + HTTP)

Status: `completed`  
Type: `✨ feature`  
Area: `data`, `platform`, `core`  
Backlog refs: `D-8`

## Problem Statement

Contracts alone do not deliver value until server endpoints can return actual descriptors from the
new typed runtime model and migrated dialect resources.

## Goal

Implement server-side dialect retrieval over gRPC and HTTP, backed by `core/mill-sql` typed
registry and migrated dialect definitions.

## In Scope

1. Implement gRPC `GetDialect` service logic.
2. Implement HTTP dialect retrieval endpoint with parity behavior.
3. Map typed Kotlin model objects to transport contracts.
4. Add robust errors for unknown dialect ids and invalid server config states.
5. Add integration tests using migrated dialects (`POSTGRES`, `H2`, `CALCITE`, `MYSQL`).

## Constraints

- Source dialect resources come from `core/mill-sql`.
- `mill-core` remains unchanged.

## Out of Scope

- AI consumer migration.
- Python client migration.
- Additional dialect migration waves beyond current set.

## Implementation Plan

1. **Service wiring**
   - Inject dialect registry into gRPC and HTTP service layers.
2. **Mapping**
   - Implement deterministic model->proto and model->HTTP mapping.
3. **Error handling**
   - Return stable structured errors for unknown/unsupported dialect requests.
4. **Integration coverage**
   - Validate endpoint behavior and transport parity for baseline dialect set.

## Acceptance Criteria

- gRPC and HTTP endpoints return dialect descriptors from typed registry.
- Four migrated dialects are retrievable and mapped correctly.
- Unknown dialect requests return predictable structured errors.
- Parity checks pass between gRPC and HTTP responses.

## Test Plan (during implementation)

### Integration

- Request each migrated dialect via gRPC and HTTP and compare key fields.
- Validate handshake capability + endpoint behavior.

### Error-path

- Unknown id request returns expected status and error payload.
- Broken/missing config path produces deterministic startup/test failure behavior.

## Risks and Mitigations

- **Risk:** Divergent mapping logic between gRPC and HTTP paths.  
  **Mitigation:** Shared mapper utilities and parity tests.

- **Risk:** Runtime startup failure due to invalid resource state.  
  **Mitigation:** Fail-fast validation and explicit diagnostics.

## Deliverables

- This work item definition (`docs/workitems/WI-019-server-getdialect-implementation.md`).
- Server gRPC/HTTP dialect endpoints backed by typed model registry.
- Integration and parity test coverage.
