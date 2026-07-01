# WI-366 — Concept metadata model contract and seed fixtures

Status: `done`  
Type: `📝 docs` / `✨ feature`  
Area: `metadata`, `ai`  
Milestone: 0.8.0

## Problem Statement

Business **concepts** are model-level metadata. The platform already has `ConceptFacet`, concept
entity URNs, and grinder-ui / mill-ui Knowledge views, but there is no single **normative
contract** for how concepts are stored in the `w` context, how chat `contextId` maps to entity
URNs, how the platform `concept` facet type is defined, and how AI tools should resolve concept
references. Implementation work (capability, injection, authoring) needs this clarity first.

**Open decisions:** [`GAPS.md`](GAPS.md) — GAP-1–7 **locked**; WI-367 may proceed after design note.

## Goal

Document and fixture the **canonical concept metadata model** so downstream WIs share one
representation.

## In Scope (high level)

1. Design note `docs/design/agentic/concept-metadata-model.md` — model-level facet assignment,
   `w` context persistence, facet payload, **`conceptRef` assignment at proposal time** (GAP-7
   **locked**), candidate link envelope keys (`conceptRef`, `parentFacetArtifactId`), URN /
   `contextId` mapping, `source` provenance.
2. Define the platform `concept` facet type in platform facets (**done** — GAP-4 **locked**):
   `metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml` and
   `metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json`.
3. Replace the placeholder/empty reference descriptor with a real `contentSchema` aligned to
   `ConceptFacet`: `targetCardinality: MULTIPLE` on the model root, with strictly **one concept per
   facet assignment** — `concepts[]` with exactly one entry (name, rewritten description, tags,
   indicative SQL, source, source session).
4. Set `applicableTo` to **`urn:mill/metadata/entity-type:model`** only (**done** in platform seed)
   — facets attach to `ModelEntityUrn.MODEL_ENTITY_ID` (GAP-1, GAP-4 **locked**).
5. Test fixtures: 2-3 model-level concept facets for `mill-ai-test` / metadata IT, including
   descriptions, tags, and indicative SQL.
6. Specify **`ConceptRefs`** resolution contract in design: full concept URN only; do **not**
   extend [`MetadataEntityIds`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/MetadataEntityIds.kt)
   (GAP-5 **locked**; implement + tests in WI-367).

## Out of Scope

- `concept` capability implementation (WI-367)
- Knowledge / contextual chat injection (deferred WI-368)

## Acceptance Criteria

- Design doc reviewed and linked from story `STORY.md`.
- Platform facets include a loaded `FacetTypeDefinition` for
  `urn:mill/metadata/facet-type:concept`.
- `platform-facet-types.json` and `platform-bootstrap.yaml` agree on concept title, description,
  `targetCardinality: MULTIPLE`, `applicableTo`, schema version, and payload schema.
- `contentSchema` captures the first-iteration concept fields, enforces one entry in `concepts[]`
  per assignment (GAP-3 **locked**), and excludes LLM-inferred targets.
- Seed/fixture concepts load in metadata IT from the `w` context and are listable through the
  concept read path.
- ID resolution documented: `ConceptRefs` accepts `urn:mill/model/concept:<slug>` only;
  `MetadataEntityIds` unchanged for physical catalog paths (GAP-5 **locked**).
- Design explicitly states all concepts are assigned only to `ModelEntityUrn.MODEL_ENTITY_ID`;
  inferred object links/targets are protocol or artifact metadata processed separately from the facet.
- Design documents `conceptRef` (`urn:mill/model/concept:<slug>`) as the stable logical id assigned
  at proposal time, before facet accept materializes the assignment row (GAP-7 **locked**).

## Deliverables

- This work item definition.
- Design doc + fixture updates on the story branch.
