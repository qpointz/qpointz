# WI-376 - Read API and UI/navigation integration

Status: `planned`
Type: `feature`
Area: `metadata`, `ai`, `ui`
Milestone: TBD
Depends on: [WI-375](WI-375-concept-link-event-consumer.md)

## Problem Statement

Once concept-to-object relation projections exist, users and agents need a read path to navigate
from concepts to related metadata objects and from objects back to related concepts.

## Goal

Expose accepted concept-object relations through read APIs and first-pass UI/navigation surfaces.

## In Scope

1. Read API for concept-to-object links and object-to-concept links.
2. Agent-facing read support where useful for concept reasoning, without replacing
   `get_model_concepts`.
3. mill-ui display/navigation for accepted links from concept detail or model object detail.
4. Tests for API shape and UI service mapping.

## Out of Scope

- Global search.
- Link editing UI beyond accept/reject lifecycle already supported by artifacts.
- Legacy `ui/mill-grinder-ui`.

## Acceptance Criteria

- A concept detail read can include accepted related objects.
- A model object read can include accepted related concepts.
- UI can render/navigate accepted links without reading `ConceptFacet.targets`.
- AI concept reasoning can use the read path without adding overlapping schema tools to the concept
  capability.

## Deliverables

- Read API/service wiring and UI integration.
