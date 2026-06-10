# WI-034 — Metadata Complex Type Support in Structural Facets and UI

Status: `planned`  
Type: `✨ feature`  
Area: `metadata`, `data`, `ui`  
Backlog refs: `M-27`

## Problem Statement

The metadata model and browser are currently shaped around scalar fields. Complex types
(`LIST`, `MAP`, `OBJECT`) will require explicit structural facet, API, and UI support so schema
exploration remains accurate once the underlying data/type work lands.

## Goal

Extend metadata structural contracts and UI rendering so complex and nested types are represented
consistently across repository, service, and browser layers.

## In Scope

1. Extend `StructuralFacet` and related DTO/API contracts for complex and nested field shapes.
2. Define rendering rules for nested metadata in the metadata browser.
3. Add tests for repository serialization and service/UI exposure of complex shapes.

## Out of Scope

- Implementing the underlying data/type system support itself.
- Advanced facet work unrelated to structural representation.

## Dependencies

- Depends on data-layer complex type work (`D-2`, `D-3`, `D-4`) being available or stubbed for
  contract validation.

## Implementation Plan

1. **Contract design**
   - Define structural representation for nested/list/map/object shapes.
2. **API exposure**
   - Extend metadata DTOs and service responses.
3. **UI rendering**
   - Add nested-shape rendering in metadata browser views.
4. **Verification**
   - Add fixture-driven tests for representative complex schemas.

## Acceptance Criteria

- Metadata service can represent nested and complex field structures without flattening away
  important shape information.
- UI can render complex shapes in a readable way.
- Tests cover repository, API, and UI behavior for representative `LIST`, `MAP`, and `OBJECT`
  cases.

## Test Plan (during implementation)

### Unit

- Structural facet serialization tests for nested types.

### Integration

- Metadata API tests over complex-type fixtures.
- UI tests for nested shape rendering.

## Risks and Mitigations

- **Risk:** metadata shape diverges from underlying type-system contracts.  
  **Mitigation:** keep this WI explicitly dependent on the data-layer complex type contracts.

- **Risk:** nested rendering becomes too dense in the browser.  
  **Mitigation:** define a constrained, expandable presentation model.

## Deliverables

- This work item definition (`docs/workitems/WI-034-metadata-complex-type-support.md`).
- Recorded implementation target for metadata complex type adaptation.
