# WI-375 - Event consumer and relation projection persistence

Status: `planned`
Type: `feature`
Area: `core`, `metadata`, `persistence`
Milestone: TBD
Depends on: [WI-373](WI-373-concept-link-protocol-model.md), [WI-374](WI-374-concept-link-artifact-events.md)

## Problem Statement

Candidate concept links need to become durable relation/link projections after acceptance. The
consumer resolves links by **`conceptRef`** on `MODEL_ENTITY_ID` (after assign-facet has run), not by
facet `uid` at proposal time ([`GAPS.md`](../ai-concepts/GAPS.md) GAP-7 **locked**).

## Goal

Implement an idempotent `core/mill-events` consumer that processes concept-link lifecycle events and
updates relation/link projections.

## In Scope

1. Relation projection store or adapter selection based on existing persistence primitives.
2. Event consumer subscribed per **relate** event type (orthogonal to `FacetProposalEventConsumers`).
3. Idempotent upsert by `(sourceArtifactId, conceptRef, targetRef, linkKind)`; resolve
   `conceptRef` → facet assignment on `MODEL_ENTITY_ID` when materializing.
4. Retraction/delete behavior for rejected or retracted link artifacts.
5. Unit and integration tests for duplicate events, updates, and retractions.

## Out of Scope

- Search indexing.
- UI navigation.
- Event broker/outbox.

## Acceptance Criteria

- Accepted candidate links create or update relation projections.
- Rejected/retracted candidate links remove or tombstone relation projections.
- Duplicate events are idempotent.
- Consumer failures are isolated according to `core/mill-events` behavior.

## Deliverables

- Event consumer, projection persistence wiring, and tests.
