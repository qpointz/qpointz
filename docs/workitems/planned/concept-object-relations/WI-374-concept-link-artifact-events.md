# WI-374 - Artifact envelope and event producer

Status: `planned`
Type: `feature`
Area: `ai`, `core`
Milestone: TBD
Depends on: [WI-373](WI-373-concept-link-protocol-model.md)

## Problem Statement

Accepted concept capture artifacts persist facet proposals via `artifact.facet.persisted` today.
Candidate concept-to-object links are a **separate relate lifecycle**: event consumers need distinct
`concept.link.*` events (not facet-assign events) with stable `conceptRef` keys from WI-370 capture.

## Goal

Emit **relate** lifecycle events through `core/mill-events` when link candidates are persisted,
accepted, rejected, or retracted — orthogonal to `MetadataEventTypes.FACET_PROPOSAL_PERSISTED`.

## In Scope

1. Consume candidate link envelope shape from WI-373 (outside `serializedPayload`).
2. Define **relate** event types and payload DTOs (separate from `artifact.facet.persisted`).
3. Publish events from the link accept/reject lifecycle at the right transaction boundary.
4. Preserve the existing facet proposal / assign-facet contract unchanged.
5. Tests proving events carry `conceptRef`, `targetRef`, `parentFacetArtifactId` or
   `sourceArtifactId`, and lifecycle status.

## Out of Scope

- Relation projection persistence.
- UI rendering.
- Distributed event transport.

## Acceptance Criteria

- Candidate links are present outside concept facet `serializedPayload`, keyed by `conceptRef`.
- `core/mill-events` receives **relate** lifecycle events distinct from assign-facet events.
- Event payloads are versioned and include `conceptRef` plus correlation artifact identifiers.
- Existing facet proposal / `artifact.facet.persisted` behavior remains unchanged.

## Deliverables

- Event type/payload definitions.
- Producer wiring and tests.
