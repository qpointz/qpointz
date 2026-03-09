# WI-032 — Metadata Context Promotion Workflow

Status: `planned`  
Type: `✨ feature`  
Area: `metadata`, `ui`  
Backlog refs: `M-20`

## Problem Statement

Once user-scoped metadata contexts exist, the platform also needs a controlled way to promote
metadata from user visibility into broader shared or global visibility. That promotion path is a
separate workflow with its own review, audit, and conflict-resolution concerns.

## Goal

Define and deliver the promotion workflow that takes metadata from a user-owned context and
publishes it into a shared/global context in a controlled, auditable way.

## In Scope

1. Define promotion targets (`team`, `shared`, `global`, or equivalent) and eligibility rules.
2. Implement service/API support for promoting metadata between contexts/scopes.
3. Define review/conflict behavior when promoted metadata overlaps existing shared/global data.
4. Add UI flow for selecting items/context and submitting or approving promotion.
5. Record audit trail for promotion history.

## Out of Scope

- Advanced facet families unrelated to promotion.
- Broad semantic/vector features.
- Complex-type metadata adaptation.

## Dependencies

- WI-029 must provide relational persistence and audit support.
- WI-030 must provide user editing/write behavior.
- WI-031 must define isolated scopes and context composition semantics.

## Implementation Plan

1. **Promotion model**
   - Define promotion states, targets, and conflict rules.
2. **Service/API path**
   - Implement promotion requests, validation, approval, and apply behavior.
3. **UI workflow**
   - Add promotion actions and review UX.
4. **Verification**
   - Test promotion from user scope to shared/global contexts with overlap scenarios.

## Acceptance Criteria

- User-owned metadata can be promoted to broader visibility through a controlled workflow.
- Promotion records preserve source context, target context, actor, and timestamps.
- Conflict behavior is deterministic and test-covered.
- Promoted metadata becomes visible through normal context-composition rules.

## Test Plan (during implementation)

### Unit

- Promotion validation and conflict-resolution tests.
- Audit record creation tests.

### Integration

- End-to-end user-to-global promotion tests.
- UI tests for promotion request/review/apply flows.

## Risks and Mitigations

- **Risk:** promotion overwrites shared metadata unexpectedly.  
  **Mitigation:** require explicit conflict handling and review semantics.

- **Risk:** promotion becomes inseparable from general editing.  
  **Mitigation:** keep it as a distinct workflow with its own audit model.

## Deliverables

- This work item definition (`docs/workitems/WI-032-metadata-promotion-workflow.md`).
- Recorded plan for promoting user metadata into broader visibility contexts.
