# WI-031 — Metadata Scopes and Context Composition

Status: `planned`  
Type: `✨ feature`  
Area: `metadata`  
Backlog refs: `M-15`, `M-19`

## Problem Statement

Metadata scope handling is currently too limited for collaborative use. There is no clear tracked
definition for "isolated" metadata sets, how they compose into a runtime context, or how those
contexts are selected and resolved.

## Goal

Introduce explicit metadata scopes as isolated sets of metadata that can be combined into a
runtime context with deterministic precedence and visibility rules.

## In Scope

1. Define metadata scope as a first-class object/set, not only an attribute on a facet.
2. Define how multiple scopes are composed into an effective context for reads and edits.
3. Wire user/security context into metadata scope resolution.
4. Define precedence, conflict resolution, and visibility rules for composed contexts.
5. Add the minimum search/browse support needed to work with scoped contexts.

## Out of Scope

- Advanced vector/semantic search.
- Promotion workflow from user-scoped context to global visibility.
- Advanced facet feature work unrelated to contexts.

## Dependencies

- WI-029 should provide the persistence model for scope ownership and composition membership.

## Implementation Plan

1. **Scope model**
   - Define isolated scope sets, identifiers, ownership, and lifecycle.
2. **Context composition**
   - Define how one or more scopes are assembled into an effective context.
3. **Security integration**
   - Propagate user/team/role identity into context selection and visibility rules.
4. **Browse/search support**
   - Add minimal discovery APIs for available scopes and active composed context.
5. **Verification**
   - Add tests for precedence, visibility, and context composition behavior.

## Acceptance Criteria

- Metadata scopes are first-class, isolated sets with clear ownership and visibility metadata.
- A runtime context can be composed from multiple scopes using documented precedence rules.
- Security/user context can influence available scopes and effective metadata without leaking data.
- Context-composition behavior is integration-tested with representative multi-scope fixtures.

## Test Plan (during implementation)

### Unit

- Scope precedence and merge tests.
- Context-composition tests for overlapping metadata.

### Integration

- Multi-scope fixture tests for isolated sets and composed contexts.
- Integration tests for active-context selection and read behavior.

## Risks and Mitigations

- **Risk:** context composition rules become too implicit or magical.  
  **Mitigation:** model scopes and contexts explicitly, with deterministic precedence.

- **Risk:** scope design is too tied to current security roles.  
  **Mitigation:** keep ownership/visibility generic enough for user, team, and global cases.

## Deliverables

- This work item definition (`docs/workitems/WI-031-metadata-scopes-and-contexts.md`).
- Recorded plan for isolated metadata scopes and composed runtime contexts.
