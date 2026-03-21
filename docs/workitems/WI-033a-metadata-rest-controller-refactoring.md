# WI-033a - Metadata REST Controller Refactoring

Status: `planned`  
Type: `refactoring`  
Area: `metadata`, `platform`  
Backlog refs: `M-21`, `M-22`

## Problem Statement

The current metadata REST module mixes several concerns in controllers:

- HTTP response branching with `ResponseEntity.notFound()` / `badRequest()`
- transport-facing error decisions inside controller methods
- partial business/application orchestration in controller code
- inconsistent failure semantics caused by `Optional` returns and generic
  `IllegalArgumentException`

This makes the metadata REST layer a good candidate for refactoring before the same patterns
spread to upcoming APIs.

## Goal

Refactor metadata REST controllers toward the repository-wide target pattern:

- thin controllers
- service or facade orchestration boundary
- shared status-exception handling
- centralized HTTP advice

## Scope

This work item targets `metadata/mill-metadata-service`, especially:

- [MetadataController.kt](/C:/Users/vm/wip/qpointz/qpointz/metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/MetadataController.kt)
- [FacetController.kt](/C:/Users/vm/wip/qpointz/qpointz/metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/FacetController.kt)
- [FacetTypeController.kt](/C:/Users/vm/wip/qpointz/qpointz/metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/FacetTypeController.kt)
- [SchemaExplorerController.kt](/C:/Users/vm/wip/qpointz/qpointz/metadata/mill-metadata-service/src/main/kotlin/io/qpointz/mill/metadata/api/SchemaExplorerController.kt)

## In Scope

1. Replace controller-local HTTP status branching with service-thrown semantic exceptions.
2. Introduce explicit application-service or facade boundaries where controllers currently hold
   orchestration logic.
3. Align metadata REST failure handling with the shared REST exception/status pattern.
4. Clarify HTTP semantics for metadata failures that are currently collapsed into generic `400`
   responses.
5. Update controller and service test strategy to match the target layering.

## Out of Scope

- Full metadata domain redesign.
- Query or UI refactoring outside metadata REST services.
- Mandatory migration of every older REST service in the repository.

## Dependencies

- [REST Exception Handling Pattern](/C:/Users/vm/wip/qpointz/qpointz/docs/design/platform/rest-exception-handling-pattern.md)

## Current Assessment

### Good candidates for direct adoption

- `MetadataController`
  Mostly repetitive `Optional -> 404` controller branching.
- `FacetController`
  Similar repetitive not-found handling with minimal additional logic.
- `FacetTypeController`
  Strong candidate because distinct business failures are currently flattened into generic
  `400 Bad Request`.

### Candidate needing deeper refactoring

- `SchemaExplorerController`
  Needs more than exception cleanup because controller code currently performs filtering,
  tree construction, search shaping, and response assembly that belongs in an application
  facade/service.

## Key Refactoring Decisions

1. Controllers should only:
   - accept request parameters
   - call a service/facade
   - return successful DTO payloads

2. Controllers should not:
   - decide `404` / `400` / `409` inline
   - catch `IllegalArgumentException` as HTTP logic
   - contain metadata search/tree orchestration

3. Services/facades should:
   - throw semantic status exceptions for API-visible failures
   - hide `Optional` and repository-specific failure semantics from controllers

4. Metadata failure semantics should be made more precise, for example:
   - not found -> `404`
   - duplicate registration -> `409`
   - invalid descriptor/content -> `400` or `422`
   - forbidden state transition such as deleting mandatory type -> `409` or `422`

## Implementation Plan

1. **Controller audit**
   - Identify endpoints that only need thin-controller cleanup versus endpoints that also need
     a new facade/service boundary.
2. **Exception semantics**
   - Replace generic `IllegalArgumentException`-driven HTTP decisions with explicit semantic
     status exceptions.
3. **Facade extraction**
   - Introduce an explorer/application facade for schema search/tree/lineage shaping where
     controller logic is currently too heavy.
4. **HTTP advice alignment**
   - Move metadata REST responses onto the shared exception-to-HTTP pattern.
5. **Test realignment**
   - Controller tests use mocked service/facade dependencies.
   - Service/facade integration tests become the first real integration level.

## Acceptance Criteria

- Metadata REST controllers are thin and no longer perform controller-local status branching for
  standard domain failures.
- Metadata REST failures align with the shared status-exception and HTTP advice pattern.
- `FacetTypeController` no longer maps multiple distinct business failures to the same generic
  `400` behavior.
- `SchemaExplorerController` orchestration is reduced or moved behind a service/facade boundary.
- The resulting structure can serve as a concrete refactoring reference for future REST APIs.

## Test Plan (during implementation)

### Unit

- Controller tests with mocked service/facade dependencies.
- Advice tests for metadata error mapping.

### Integration

- Service/facade integration tests for metadata lookup, facet retrieval, facet-type mutation,
  and explorer flows.
- Failure-path tests proving correct semantic status behavior.

## Risks and Mitigations

- **Risk:** refactoring stops at controller syntax cleanup while leaving service semantics vague.  
  **Mitigation:** explicitly refactor failure semantics and facade boundaries, not only return
  statements.

- **Risk:** explorer endpoints remain controller-heavy.  
  **Mitigation:** treat `SchemaExplorerController` as a separate extraction target inside this WI.

- **Risk:** metadata adopts a one-off exception approach again.  
  **Mitigation:** make the platform design doc an explicit dependency.

## Deliverables

- This work item definition (`docs/workitems/WI-033a-metadata-rest-controller-refactoring.md`).
- A documented metadata-specific refactoring plan aligned with the platform REST pattern.
