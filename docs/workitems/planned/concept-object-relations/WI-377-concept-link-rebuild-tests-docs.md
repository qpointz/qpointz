# WI-377 - Rebuild, idempotency, and verification docs

Status: `planned`
Type: `test` / `docs`
Area: `ai`, `core`, `metadata`, `persistence`
Milestone: TBD
Depends on: [WI-375](WI-375-concept-link-event-consumer.md), [WI-376](WI-376-concept-link-read-ui.md)

## Problem Statement

Relation projections derived from artifacts must be rebuildable and verifiable. Without rebuild and
idempotency documentation, link projections can drift from artifact history or behave differently
after retries.

## Goal

Document and test the operational guarantees for concept-object relation projections.

## In Scope

1. Rebuild procedure from accepted artifact history.
2. Idempotency and duplicate-event tests.
3. Retraction/reject replay tests.
4. Documentation updates for event types, relation projection shape, and troubleshooting.
5. Verification commands for touched Gradle modules.

## Out of Scope

- Distributed event transport durability guarantees.
- Full search/index rebuild.

## Acceptance Criteria

- Rebuild from artifact history recreates expected relation projections.
- Replay of duplicate events does not create duplicate projections.
- Reject/retract replay produces deterministic tombstone/delete state.
- Design and operational docs describe the event consumer and rebuild flow.

## Deliverables

- Test coverage, rebuild docs, and final verification notes.
