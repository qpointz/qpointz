# WI-373 - Concept link protocol and relation model

Status: `planned`
Type: `docs` / `feature`
Area: `ai`, `metadata`, `persistence`
Milestone: TBD

## Problem Statement

Concept capture needs a way to represent links between model-level concepts and concrete metadata
objects without storing LLM-inferred targets inside `ConceptFacet`. Relate events are **orthogonal**
to assign-facet (`artifact.facet.persisted`): link candidates may be emitted at capture time before
a concept facet row exists, so the protocol must key links by **proposed `conceptRef`**, not facet
`uid` or a standalone concept entity id (see [`GAPS.md`](../ai-concepts/GAPS.md) GAP-7 **locked**).

## Goal

Define the protocol/artifact and relation model for concept-to-object links.

## In Scope

1. Design note for concept candidate links and relation projections.
2. Candidate link wire shape outside `serializedPayload`, including:
   - `conceptRef` — full URN `urn:mill/model/concept:<slug>` assigned at proposal time (WI-370)
   - `targetRef` — metadata object URN (schema/table/attribute)
   - `linkKind`
   - confidence/evidence
   - `parentFacetArtifactId` / `sourceArtifactId` for correlation before facet accept
   - source session
3. Relation identity and idempotency rules (`conceptRef` + `targetRef` + `linkKind`, not facet `uid`
   at proposal time).
4. Accepted/rejected lifecycle semantics, distinct from `artifact.facet.persisted`.
5. Compatibility notes for legacy `ConceptFacet.targets`.

## Out of Scope

- Implementing event consumers.
- Persisting relation projections.
- Changing `ConceptFacet` payload shape.

## Acceptance Criteria

- Design states that concept facet payload remains independent from candidate links.
- Candidate link envelope shape is documented with `conceptRef` and `parentFacetArtifactId`.
- Relation projection identity and lifecycle rules are documented separately from assign-facet.
- Legacy `targets` are explicitly compatibility-only for first-iteration capture.

## Deliverables

- Work item definition.
- Design doc updates under `docs/design/agentic/` and/or `docs/design/persistence/`.
