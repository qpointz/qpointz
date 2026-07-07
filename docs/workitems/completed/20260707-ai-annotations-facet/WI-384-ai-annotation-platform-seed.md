# WI-384 — Platform seed and Skymill fixture

| Field | Value |
|--------|--------|
| **Story** | [`ai-annotations-facet`](STORY.md) |
| **Status** | `done` |
| **Type** | `docs` / `feature` (seed YAML) |
| **Area** | `metadata` |
| **Depends on** | [**WI-383**](WI-383-ai-annotation-facet-design.md) — stable `contentSchema`; [`GAPS.md`](GAPS.md) GAP-1, GAP-7 |
| **Enables** | [**WI-385**](WI-385-ai-annotation-schema-tools.md), [**WI-388**](WI-388-metadata-authoring-ai-annotation-capture.md) |

## Problem

Facet types defined only in design prose are not loaded into **`FacetCatalog`** until
**`FacetTypeDefinition`** seed documents exist. Tests and Skymill scenarios need at least one
real **`ai-annotation`** assignment on `skymill.segments` before schema tools can expose
`aiAnnotations`.

## Goal

Register **`urn:mill/metadata/facet-type:ai-annotation`** in platform seeds and add a Skymill
table-level fixture for the cities join / city-name projection instruction.

## Deliver

### 1. Platform `FacetTypeDefinition`

**Files:**

- [`metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml)
- [`metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-facet-types.json)

**Manifest fields:**

| Field | Value |
|-------|-------|
| `facetTypeUrn` | `urn:mill/metadata/facet-type:ai-annotation` |
| `title` | AI Annotation (or agreed product title) |
| `description` | Entity-scoped procedural instructions for agents (SQL habits, projection rules) |
| `category` | `ai` |
| `targetCardinality` | `MULTIPLE` |
| `applicableTo` | `schema`, `table`, `attribute` entity-type URNs |
| `contentSchema` | Per WI-383 / design doc |
| `schemaVersion` | `"1.0"` |

Update bootstrap header comment listing authoritative facet types.

### 2. Seed unit test

Pattern: [`PlatformDqL1FacetTypesSeedTest`](../../../metadata/mill-metadata-core/src/test/kotlin/io/qpointz/mill/metadata/domain/facet/PlatformDqL1FacetTypesSeedTest.kt) or inline bootstrap assertion.

Assert loaded definition: URN, category, cardinality, `applicableTo`, required `instruction` field.

### 3. Skymill fixture

Add **`ai-annotation`** facet assignment on **`urn:mill/model/table:skymill.segments`** in
[`test/datasets/skymill/skymill-meta-seed-canonical.yaml`](../../../../test/datasets/skymill/skymill-meta-seed-canonical.yaml)
(GAP-7 **locked** — consumed by `SchemaFacetServiceSkyMillIT`).

**Payload (reference — GAP-1):**

```yaml
title: City name projection
instruction: >
  When this table is used in SQL, join skymill.cities twice for origin and destination
  (origin and destination) and prefer city names in SELECT output instead of raw ids,
  unless the user explicitly requests ids.
kind: sql_generation
```

### 4. Inventory doc

Update [`docs/design/metadata/platform-standard-facet-types.md`](../../../design/metadata/platform-standard-facet-types.md) — add `ai-annotation` row under `platform-bootstrap.yaml`.

## Out of scope

- `SchemaFacetCatalogAdapter` changes (WI-385)
- `MetadataUrns` constant in `mill-metadata-core` (add if project convention requires for WI-385)

## Acceptance criteria

- `FacetCatalog` loads `ai-annotation` from bootstrap in metadata unit tests.
- JSON mirror agrees with YAML on title, cardinality, `applicableTo`, schema version.
- Skymill fixture assignment persists and is readable via metadata read path for `skymill.segments`.
- [`platform-standard-facet-types.md`](../../../design/metadata/platform-standard-facet-types.md) lists the new type.

## Deliverables

- This work item definition.
- Seed YAML/JSON + tests + fixture on the story branch.
