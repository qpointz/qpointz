# AI annotation facet type

**Status:** Normative — platform facet contract  
**Story:** [`ai-annotations-facet`](../../workitems/in-progress/ai-annotations-facet/STORY.md) (WI-383)  
**Gaps:** [`GAPS.md`](../../workitems/in-progress/ai-annotations-facet/GAPS.md) — all locked  
**Related:** [`schema-facet-ai-tool-field-mapping.md`](schema-facet-ai-tool-field-mapping.md), [`facet-type-descriptor-formats.md`](facet-type-descriptor-formats.md), [`metadata-facet-catalog-v3.md`](../agentic/metadata-facet-catalog-v3.md)

## Summary

**`ai-annotation`** stores **entity-scoped procedural instructions** for agents — SQL habits,
projection rules, and tool-output conventions — on **schema**, **table**, and **attribute**
entities. Cardinality is **MULTIPLE** (many instructions per entity).

| Topic | Rule |
|-------|------|
| Facet URN | `urn:mill/metadata/facet-type:ai-annotation` |
| Category | `ai` |
| Cardinality | **MULTIPLE** |
| `applicableTo` | `urn:mill/metadata/entity-type:schema`, `table`, `attribute` |
| Capture | `metadata-authoring.propose_facet_assignment` with `facetTypeKey=ai-annotation` |
| Agent read | `schema.list_schemas` / `list_tables` / `list_columns` → `aiAnnotations[]` |

## Purpose vs other facet types

| Facet | Cardinality | Purpose | Example |
|-------|-------------|---------|---------|
| **`descriptive`** | SINGLE | Human catalog text (title, description, tags) | “Segments stores flight legs between cities” |
| **`ai-annotation`** | MULTIPLE | **How the agent should behave** when using the entity | “When querying segments, join cities and return names instead of ids” |
| **`relation`** | MULTIPLE | Declarative join structure | `segments.origin` → `cities.id` |
| **`concept`** | MULTIPLE (model root only) | Model-level business semantics | VIP passenger definition |
| **`dq-*`** | MULTIPLE | Data-quality validation rules | “origin must not be null” |

**Authoring rule:** imperative agent/SQL habits → **`ai-annotation`**; catalog narrative →
**`descriptive`**. Do not store agent rules in `descriptive.description`.

## Payload (`contentSchema`)

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `title` | STRING | no | Short operator label |
| `instruction` | STRING | **yes** | Free-form agent instruction |
| `kind` | ENUM | no | `sql_generation` (default), `tool_output`, `general` |
| `tags` | ARRAY STRING | no | `stereotype: tags` |
| `enabled` | BOOLEAN | no | Missing ⇒ **true**; `false` hides from agent surfaces |

Platform seed: [`platform-bootstrap.yaml`](../../../metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml).

## Agent tool extension

Only the **`schema`** capability projects per-entity facets into tool results today. This story
extends the three tools that already carry **`descriptive`** fields:

| Tool | Extend? | `aiAnnotations` source |
|------|---------|------------------------|
| `list_schemas` | **Yes** | Assignments on **schema** entity |
| `list_tables` | **Yes** | Assignments on **table** entity |
| `list_columns` | **Yes** | Assignments on **attribute** entity |
| `list_relations` | No | `relation` facet only |
| `resolve_metadata_entity` | No | URN resolution only |
| `metadata.*` | No | Facet type catalog |
| `metadata-authoring.*` | Capture | `propose_facet_assignment` only |
| `sql-query.*` | Indirect | Honors `aiAnnotations` from schema tools in-turn |

**Scope (GAP-4):** **Exact entity only** — no inheritance from schema → table → column. Table
instructions appear on the `list_tables` row; column instructions on `list_columns` only.

### Wire shape

On each `list_*` item:

```json
"aiAnnotations": [
  {
    "title": "City name projection",
    "instruction": "When this table is used in SQL, join skymill.cities twice …",
    "kind": "sql_generation",
    "tags": []
  }
]
```

| Rule | Detail |
|------|--------|
| Source | Enabled `ai-annotation` rows from `facetsResolved` (MULTIPLE) |
| `enabled: false` | **Omitted** from `aiAnnotations[]` |
| Wire fields | `title`, `instruction`, `kind`, `tags` — **no `enabled`** on wire (GAP-2) |
| Order | Stable `facetsResolved` merge order (GAP-3) |
| Duplicates | Allowed in v1 |

Implementation: [`SchemaFacetCatalogAdapter`](../../../ai/mill-ai-data/src/main/kotlin/io/qpointz/mill/ai/data/schema/SchemaFacetCatalogAdapter.kt) reads `facetsResolved` directly — **not** typed [`SchemaFacets`](../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacets.kt) converters.

## Agent consumption

### SQL generation (`kind=sql_generation`, default)

When `aiAnnotations` is non-empty on an entity returned by `list_tables` or `list_columns` in the
same turn, agents **must** treat instructions as mandatory context before emitting SQL — unless the
**current user message** explicitly contradicts them (GAP-5).

**Precedence (highest first):**

1. Hard constraints — SQL dialect validity, authorization, safety
2. **Explicit user request in the current turn**
3. **`ai-annotation`** on entities touched in this turn
4. **`relation`** join metadata and **`descriptive`** catalog text
5. Model-level **`concept`** facets (separate capability)

Normative prompt phrase: *“Apply enabled `aiAnnotations` unless the user’s current message explicitly requests otherwise.”*

`sql-query` does **not** call metadata ports — it consumes `aiAnnotations` already present on schema
tool results.

### Authoring capture

See WI-388. Signal phrases include: *when you query*, *always join*, *the agent should*, *for SQL*,
*instead of ids show*, *every time this table is used*.

## Skymill reference scenario (GAP-1)

| Item | Value |
|------|--------|
| Assignment target | `urn:mill/model/table:skymill.segments` |
| Lookup table | `skymill.cities` |
| FK columns | `origin`, `destination` |

**Fixture payload** ([`skymill-meta-seed-canonical.yaml`](../../../test/datasets/skymill/skymill-meta-seed-canonical.yaml)):

```yaml
title: City name projection
instruction: >
  When this table is used in SQL, join skymill.cities twice for origin and destination
  (origin and destination) and prefer city names in SELECT output instead of raw ids,
  unless the user explicitly requests ids.
kind: sql_generation
```

## See also

- [`value-mapping-indexing-facet-types.md`](value-mapping-indexing-facet-types.md) — other `ai` category facets
- [`platform-standard-facet-types.md`](platform-standard-facet-types.md) — seed inventory
