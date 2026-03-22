# WI-028 — Metadata Value Mapping API and UI Surface

Status: `planned`  
Type: `✨ feature`  
Area: `metadata`, `ui`  
Backlog refs: `M-6`, `M-7`

## Problem Statement

Even with facet-backed value mappings available in the repository, there is no dedicated WI that
defines how those mappings are exposed through the metadata service and presented in the metadata
browser UI.

## Goal

Expose value mappings as first-class metadata API resources and make them visible in the metadata
browser so users can inspect how business terms resolve to physical values.

## In Scope

1. Add value-mapping REST endpoints to the metadata service.
2. Define DTOs and OpenAPI surface needed by the UI.
3. Show value mappings in the metadata browser entity/details view.
4. Support read-path term resolution for a given entity/attribute through the API.

## Out of Scope

- General metadata editing workflows for all facet types.
- Production approval workflow for enrichments.
- Persistence changes beyond what is needed to read existing mappings.

## Dependencies

- WI-027 must provide a stable `ValueMappingFacet` contract and resolver path.

## Implementation Plan

1. **REST contract**
   - Define endpoints for listing mappings and resolving a user term for an entity.
2. **Service wiring**
   - Route requests through metadata/value-mapping service components.
3. **UI integration**
   - Extend metadata browser details/facet rendering for value mappings.
4. **Verification**
   - Add endpoint tests and UI tests for empty, populated, and error states.

## Acceptance Criteria

- Metadata service exposes documented value-mapping endpoints for entity-scoped access.
- UI can display value mappings without custom/manual payload shaping.
- Users can inspect mapping entries and see missing/empty states clearly.
- Term-resolution API is covered by tests and returns stable, typed responses.

## Test Plan (during implementation)

### Unit

- DTO mapping and controller/service tests for value-mapping endpoints.

### Integration

- End-to-end API tests over representative metadata fixtures.
- UI tests covering rendering of mappings, no mappings, and resolution failures.

## Risks and Mitigations

- **Risk:** API shape becomes too UI-specific.  
  **Mitigation:** keep entity-scoped, reusable service contracts and generate UI client code.

- **Risk:** value-mapping displays expose implementation detail instead of user meaning.  
  **Mitigation:** present both user term and resolved database value with labels/descriptions.

## Deliverables

- This work item definition (`docs/workitems/WI-028-metadata-value-mapping-api-and-ui.md`).
- Metadata service value-mapping endpoints and browser UI support.
