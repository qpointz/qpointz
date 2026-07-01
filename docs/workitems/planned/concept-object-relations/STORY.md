# Concept object relations

**Branch:** TBD
**Milestone:** TBD

Build the follow-on **relate** pipeline for concept capture: WI-370 may emit candidate
concept-to-object links outside the `ConceptFacet` payload (keyed by proposed `conceptRef`), and
this story owns relate event types, producers, consumers, and durable relation projections.
Assign-facet (`artifact.facet.persisted`) remains in the `ai-concepts` / metadata path.

This story deliberately keeps concept meaning separate from object links. The concept facet remains
model-level metadata in the `w` context, owned by the `ai-concepts` story. Candidate links describe
how a concept appears to relate to schemas, tables, attributes, SQL artifacts, or other metadata
objects. They are reviewable evidence and projections, not concept truth.

**Design references:**

- [`docs/design/platform/general-event-bus.md`](../../../design/platform/general-event-bus.md)
- [`docs/design/agentic/metadata-facet-catalog-v3.md`](../../../design/agentic/metadata-facet-catalog-v3.md)
- [`../ai-concepts/STORY.md`](../ai-concepts/STORY.md)

## Scope

In scope:

- Protocol/artifact shape for candidate concept-to-object links outside `serializedPayload`.
- Event payloads and producers for accepted concept artifacts or accepted link proposals.
- `core/mill-events` consumers that derive relation/link projections.
- Idempotent relation persistence/rebuild behavior.
- Read-side access for UI/navigation and concept reasoning.

Out of scope:

- Storing LLM-inferred targets inside `ConceptFacet`.
- Replacing schema relation facets (`relation`, `relation-source`, `relation-target`).
- Full global search or vector indexing.
- Distributed event transport/outbox.
- `ui/mill-grinder-ui` changes.

## Work Items

- [ ] WI-373 - Concept link protocol and relation model (`WI-373-concept-link-protocol-model.md`)
- [ ] WI-374 - Artifact envelope and event producer (`WI-374-concept-link-artifact-events.md`)
- [ ] WI-375 - Event consumer and relation projection persistence (`WI-375-concept-link-event-consumer.md`)
- [ ] WI-376 - Read API and UI/navigation integration (`WI-376-concept-link-read-ui.md`)
- [ ] WI-377 - Rebuild, idempotency, and verification docs (`WI-377-concept-link-rebuild-tests-docs.md`)

## Delivery Notes

- Assign-facet and relate are **orthogonal events** — see [`GAPS.md`](../ai-concepts/GAPS.md) GAP-7.
- Candidate links are emitted alongside facet proposals (WI-370), keyed by `conceptRef` and
  `parentFacetArtifactId`, not inside the concept facet payload.
- Link acceptance/rejection should follow the same artifact lifecycle principles as facet proposals
  where possible.
- Relate event consumers must be idempotent by artifact id, `conceptRef`, and candidate link identity.
- Relation projections should be rebuildable from artifact history when possible.
