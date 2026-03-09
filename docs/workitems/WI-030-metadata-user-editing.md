# WI-030 — Metadata User Editing and Authoring Workflow

Status: `planned`  
Type: `✨ feature`  
Area: `metadata`, `ai`, `ui`  
Backlog refs: `M-10`, `M-20`

## Problem Statement

The metadata system is largely read-only today. There is no tracked WI that defines how a user
can create, edit, review, and save metadata through the service and UI layers.

## Goal

Define and deliver the first end-to-end user editing workflow for metadata, including editable
facet types, write APIs, and UI surfaces for authoring and review.

## In Scope

1. Define which metadata entities/facets are user-editable in the first slice.
2. Add write APIs for create/update/delete of metadata content.
3. Add UI editing flows for the selected metadata scope.
4. Record audit/version information for user changes.
5. Support AI-generated enrichments only where they fit the same authoring model.

## Out of Scope

- Broad advanced search rollout.
- Structural complex-type adaptation.
- Full semantic/vector metadata feature work.

## Dependencies

- WI-029 should provide the relational persistence path required for metadata writes.
- Scope/context semantics from WI-031/WI-032 must be compatible with the authoring model.

## Implementation Plan

1. **Editable model**
   - Define editable entities, facets, validation rules, and write constraints.
2. **Service/API write path**
   - Implement create/update/delete endpoints and optimistic concurrency behavior.
3. **UI authoring**
   - Add editing/review flows for the targeted metadata types.
4. **Audit and review**
   - Record author, timestamps, revision metadata, and change history.
5. **Verification**
   - Test direct user editing and any AI-assisted draft/edit path built on top.

## Acceptance Criteria

- Users can create and modify the targeted metadata entities/facets through supported APIs and UI.
- Write flows enforce validation, concurrency, and audit requirements.
- Edited metadata becomes visible through normal metadata read paths after save.
- If AI suggestions are included, they reuse the same authoring and audit model.

## Test Plan (during implementation)

### Unit

- Facet validation tests for editable payloads.
- Metadata write validation and revision tests.

### Integration

- End-to-end user edit/save/read-back tests.
- UI tests for edit/create/delete/review flows.

## Risks and Mitigations

- **Risk:** authoring scope expands into a full CMS too early.  
  **Mitigation:** lock a small editable facet/entity set for the first release.

- **Risk:** write APIs are introduced before persistence and audit semantics are solid.  
  **Mitigation:** keep this WI dependent on relational persistence and revision tracking.

## Deliverables

- This work item definition (`docs/workitems/WI-030-metadata-user-editing.md`).
- Recorded plan for first-class user editing of metadata.
