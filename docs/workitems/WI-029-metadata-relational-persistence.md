# WI-029 — Metadata Relational Persistence and Repository Transition

Status: `planned`  
Type: `✨ feature`  
Area: `metadata`  
Backlog refs: `M-11`, `M-12`

## Problem Statement

The current metadata implementation is still centered on file-backed repository flows. That is
not sufficient for user-authored metadata, collaborative editing, or promotion workflows that
require durable transactional storage and auditable updates.

## Goal

Establish relational-database persistence as the primary metadata storage model, with the
repository and synchronization changes needed to move metadata operations off file-based storage.

## In Scope

1. Implement relational persistence for metadata entities, facets, audit fields, and ownership.
2. Define repository behavior for create/update/delete operations against relational storage.
3. Introduce composite or synchronization behavior only where needed for migration or physical
   schema import.
4. Define runtime configuration so relational persistence is the primary operational mode.

## Out of Scope

- Metadata authoring UX.
- Scope/context composition semantics beyond what persistence must store.
- Advanced facets such as semantic, lineage, or data quality.

## Dependencies

- Metadata entity and facet contracts must be stable enough to persist relationally.

## Implementation Plan

1. **Schema and repository model**
   - Define relational schema for entities, facets, scope ownership, and audit history.
2. **Write path**
   - Implement create/update/delete behavior and optimistic-concurrency rules.
3. **Migration/bootstrap**
   - Define file-to-db import/bootstrap path and any required sync hooks.
4. **Runtime adoption**
   - Make relational persistence the primary repository mode for metadata operations.

## Acceptance Criteria

- Metadata can be created, updated, deleted, and read through a relational repository.
- Persisted metadata includes audit information required for user editing and promotion flows.
- Bootstrap/migration from file-backed metadata is deterministic and tested.
- Runtime configuration supports operating metadata primarily from relational storage.

## Test Plan (during implementation)

### Unit

- Repository mapping tests for entity, facet, and audit persistence.
- Concurrency/versioning tests for metadata writes.

### Integration

- Relational repository integration tests against representative metadata fixtures.
- File-to-db bootstrap or migration tests for seeded metadata.

## Risks and Mitigations

- **Risk:** an over-flexible persistence model makes querying and promotion workflows harder.  
  **Mitigation:** define a clear relational ownership/audit model up front, not only blob storage.

- **Risk:** migration from file-backed metadata causes drift or partial adoption.  
  **Mitigation:** add explicit bootstrap/import flows and fixture-based parity checks.

## Deliverables

- This work item definition (`docs/workitems/WI-029-metadata-relational-persistence.md`).
- Recorded plan for moving metadata persistence from files to relational storage.
