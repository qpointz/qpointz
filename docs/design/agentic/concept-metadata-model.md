# Concept metadata model (v3)

Normative contract for **business concepts** as model-level metadata facets in mill-ai v3.

**Story:** [`ai-concepts`](../../../workitems/completed/20260701-ai-concepts/STORY.md) (WI-366–370, WI-372).

**See also:** [`v3-foundation-decisions.md`](v3-foundation-decisions.md) §5.3 / §7.3, [`metadata-facet-catalog-v3.md`](metadata-facet-catalog-v3.md).

## Summary

Business concepts are **model-level metadata**: each concept is one `concept` facet assignment on the
logical model root entity [`ModelEntityUrn.MODEL_ENTITY_ID`](../../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/ModelEntityUrn.kt)
(`urn:mill/model/model:model-entity`). The model root is the only v1 assignment target because
concepts may span schemas, tables, and attributes.

| Topic | Rule |
|-------|------|
| Platform facet type | `urn:mill/metadata/facet-type:concept` — `targetCardinality: MULTIPLE`, `applicableTo: urn:mill/metadata/entity-type:model` |
| Assignment cardinality | Many `concept` facet rows on `MODEL_ENTITY_ID`; **one business concept per assignment** |
| Payload `concepts[]` | Exactly one entry (`[0]`) per assignment (wire compatibility with `ConceptFacet` / v1 enrich-model) |
| Logical concept id | `urn:mill/model/concept:<slug>` — kebab-case slug (e.g. `vip-passengers`) |
| Physical catalog resolution | [`MetadataEntityIds`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/metadata/MetadataEntityIds.kt) — schema/table/attribute paths and model root only |
| Concept ref resolution | [`ConceptRefs`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/concept/ConceptRefs.kt) — full concept URN only; not dot-separated catalog paths |
| Capture | `metadata-authoring.propose_facet_assignment` with `facetTypeKey=concept` on `MODEL_ENTITY_ID` in chat write scope (`w`) |
| v1 `targets[]` | Read for legacy data only; **never written** on capture in v1 |

## Model root assignment

All concepts attach to **`MODEL_ENTITY_ID`** only:

```text
urn:mill/model/model:model-entity
```

Facet tools resolve the model root via `metadataEntityId` (full URN) or catalog alias `model-entity`
(see `MetadataEntityIds`).

Standalone `type: CONCEPT` metadata entities (legacy seeds) are **not** assignment targets in v1.
New fixtures and tests use model-level facet assignments only.

## Concept logical ref (`conceptRef`)

Canonical id for tools, catalog, and capture:

```text
urn:mill/model/concept:<slug>
```

| Rule | Detail |
|------|--------|
| `<slug>` | kebab-case identifier derived from concept name (e.g. `VIP Passengers` → `vip-passengers`) |
| Assignment time | `conceptRef` is assigned when the facet proposal is built, **before** accept materializes the row |
| Payload | Include `conceptRef` in the proposed facet body alongside `concepts[0]` |
| Lookup | Slug → matching `concept` facet assignment on `MODEL_ENTITY_ID` in the active read scope |

[`ConceptRefs`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/concept/ConceptRefs.kt) parses and validates concept URNs. Do **not** route concept refs through `MetadataEntityIds`.

## Facet payload (v1 capture)

Platform `contentSchema` fields per assignment:

| Field | Required | Notes |
|-------|----------|-------|
| `conceptRef` | recommended at capture | Stable logical id; may be omitted on legacy rows (slug derived from `concepts[0].name`) |
| `concepts[]` | yes | Exactly **one** entry |
| `concepts[0].name` | yes | Human-readable name |
| `concepts[0].description` | yes | Metadata-quality narrative |
| `concepts[0].sql` | no | Indicative SQL hint for reasoning — not validated execution SQL |
| `concepts[0].tags` | no | Discovery / filter tags |

Excluded from v1 capture: `category`, `targets[]` (legacy compatibility only on old data).

## Cardinality

| Layer | Cardinality |
|-------|-------------|
| `concept` facet assignments on model root | **MULTIPLE** (one per business concept) |
| `concepts[]` inside each assignment | **SINGLE** entry |
| Multiple concepts inferred in one turn | **N parallel** `propose_facet_assignment` calls — never pack multiple concepts into one assignment |

## Read scope (`w` context)

Chat capture scopes concepts into the active write scope (`w` — chat metadata scope). Published or
seeded concepts may also appear under `global` or other read scopes. All reads enumerate `concept`
facet assignments on `MODEL_ENTITY_ID` within the scopes visible to the agent turn
(`AgentContext.readableScopesParam()`).

| Tool (WI-367) | Source |
|---------------|--------|
| `get_model_concepts` | All `concept` facets on `MODEL_ENTITY_ID` in active read scope |
| `list_concepts` / `list_concept_tags` / `search_concepts` | Same enumeration; tag filter and lexical search over name, description, tags |
| `get_concept` | Resolve `conceptRef` → facet assignment on model root |

## Candidate concept-to-object links (capture envelope)

When grounding is available at capture time, **candidate links** live in the artifact **envelope**
outside `serializedPayload`:

| Key | Purpose |
|-----|---------|
| `conceptRef` | Stable logical concept id for the link row |
| `parentFacetArtifactId` | Correlates to the facet-proposal artifact before accept |
| `targetRef` | Grounded metadata entity URN (schema/table/attribute) |
| `linkKind` | Relation intent (deferred relate pipeline — `concept-object-relations` story) |

Relate event producers/consumers are **out of scope** for the ai-concepts story.

## ID resolution split

```
Physical catalog path / schema URN  →  MetadataEntityIds  →  schema | table | attribute
Model root URN or "model-entity"    →  MetadataEntityIds  →  model
Concept URN urn:mill/model/concept:* →  ConceptRefs       →  slug → facet on MODEL_ENTITY_ID
```

## Test fixtures

Story fixtures seed 2–3 model-level `concept` facet assignments on `MODEL_ENTITY_ID` (see
[`ConceptModelFixtures`](../../../metadata/mill-metadata-core/src/test/kotlin/io/qpointz/mill/metadata/fixtures/ConceptModelFixtures.kt)).
Primary seed concept: **VIP Passengers** (`urn:mill/model/concept:vip-passengers`).
