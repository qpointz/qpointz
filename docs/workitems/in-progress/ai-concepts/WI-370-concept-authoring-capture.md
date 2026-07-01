# WI-370 — Concept authoring and v1 enrich-model capture

Status: `done`  
Type: `✨ feature`  
Area: `ai`, `metadata`  
Milestone: 0.8.0  
Depends on: [WI-366](WI-366-concept-metadata-model.md), [WI-367](WI-367-concept-catalog-capability.md)

## Problem Statement

AI v1 **`enrich-model`** produced structured concept enrichments in chat JSON but never persisted
them to metadata (`ConceptSource.NL2SQL` exists but was unused). v3 has **`metadata-authoring`**
and facet proposal artifacts, but no flow to recognize concept-definition intent, generate the
concept payload, and persist it as a model-level `ConceptFacet` in the `w` context.

## Goal

Enable **chat-driven concept enrichment**: propose new or updated concept definitions, persist via
existing artifact accept/reject lifecycle, aligned with v1 enrich-model semantics.

## In Scope (high level)

1. Recognize user intent to define or refine one or more business concepts.
2. Generate or normalize the concept name and assign **`conceptRef`**:
   `urn:mill/model/concept:<kebab-slug>` (GAP-2, GAP-7 **locked**).
3. Summarize/rewrite the concept description into metadata-quality prose.
4. Generate indicative tags.
5. Generate an indicative SQL hint for extracting the concept from the DB; prefer valid SQL.
6. Map each generated concept (and v1 `enrich-model` `type: "concept"` item) to a separate
   `ConceptFacet.Concept` payload with a single `concepts[0]` entry — no LLM-inferred `targets`
   inside the facet; include the assigned `conceptRef` in the proposed facet body.
7. Persist through the generic `metadata-authoring` `propose_facet_assignment` CAPTURE lifecycle on
   `MODEL_ENTITY_ID` in `w` — **one facet assignment per concept** (GAP-3 **locked**). When the
   LLM infers **multiple concepts in one turn**, emit **parallel** `propose_facet_assignment`
   calls (batch `results[]` / multi-artifact lifecycle per WI-351); never combine multiple concepts
   into one assignment. Do not add a typed `capture_concept` tool.
8. When grounding is available, include **candidate concept-to-object links** in the artifact
   **envelope** (outside `serializedPayload`), keyed by `conceptRef` and correlated via
   `parentFacetArtifactId` to the facet-proposal artifact (GAP-7 **locked**). Do **not** implement
   relate event producers or consumers here — those belong to
   [`concept-object-relations`](../concept-object-relations/STORY.md) (WI-374–375).
9. Profile — extend `data-analysis` (or dedicated capture profile) with `concept` +
   `metadata-authoring` for general-chat concept definition turns.
10. Set `source=NL2SQL`, `sourceSession=<chatId>` on captured concepts.
11. Tests: propose → artifact → accept → hydrate/present like other facets and read via
    `get_concept`.

## Out of Scope

- grinder-ui enrich-model UI port
- Bulk import / YAML authoring UX
- Non-concept enrich-model types (may reuse existing descriptive/DQ/relation authoring)
- Relate event types, producers, consumers, and relation projection persistence (WI-373–377)

## Acceptance Criteria (draft — refine later)

- Agent can propose one or more business concepts from chat; each concept is a separate facet
  proposal with a stable `conceptRef`; user accept persists to metadata.
- Persisted concept visible via WI-367 read tools and WI-369 SQL grounding in general chat.
- Concept capture reuses the existing facet-proposal artifact lifecycle and does not introduce
  overlapping typed capture tools.
- Prompt tests prove concept authoring guidance does not duplicate generic
  `metadata-authoring.intent` ownership.
- Candidate object links are outside `serializedPayload`, keyed by `conceptRef` and
  `parentFacetArtifactId`; relate lifecycle is deferred to WI-374–375.

## Deliverables

- This work item definition.
- Authoring tools, profile, tests on the story branch.
