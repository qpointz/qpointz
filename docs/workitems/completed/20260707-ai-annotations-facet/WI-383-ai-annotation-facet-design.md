# WI-383 — AI annotation facet design contract

| Field | Value |
|--------|--------|
| **Story** | [`ai-annotations-facet`](STORY.md) |
| **Status** | `done` |
| **Type** | `docs` |
| **Area** | `metadata`, `ai` |
| **Enables** | [**WI-384**](WI-384-ai-annotation-platform-seed.md), [**WI-385**](WI-385-ai-annotation-schema-tools.md), [**WI-388**](WI-388-metadata-authoring-ai-annotation-capture.md) |

## Problem

Catalog entities need **entity-specific agent instructions** (procedural SQL habits, projection
rules, tool-output conventions) that are not expressible as:

- **`descriptive`** — SINGLE cardinality; display name / narrative / tags for humans
- **`relation`** — declarative join structure, not behavioral “always do X when querying” rules
- **`concept`** — model-level business semantics on `model-entity`, not per-table habits

Only **`schema.list_schemas`**, **`list_tables`**, and **`list_columns`** project per-entity facet
data to agents today (`descriptive` fields). There is no normative contract for a new facet type or
for how agents must consume instructions.

## Goal

Publish [`docs/design/metadata/ai-annotation-facet-type.md`](../../../design/metadata/ai-annotation-facet-type.md) as the normative contract for platform facet type
**`urn:mill/metadata/facet-type:ai-annotation`**.

## Deliver

### 1. Design doc sections

**File:** [`docs/design/metadata/ai-annotation-facet-type.md`](../../../design/metadata/ai-annotation-facet-type.md) (new)

| Section | Content |
|---------|---------|
| Purpose and scope | Entity-scoped agent instructions; MULTIPLE per entity |
| vs other facets | Table comparing `descriptive`, `relation`, `concept`, `ai-annotation` |
| Facet manifest | URN, category `ai`, cardinality, `applicableTo` (schema, table, attribute) |
| Payload `contentSchema` | `title`, `instruction` (required), `kind` enum, `tags`, `enabled` |
| Tool extension inventory | Normative table from [`STORY.md`](STORY.md) — which tools gain `aiAnnotations`, which are unchanged |
| Agent consumption | When `kind=sql_generation` (default), non-empty `aiAnnotations` on a `list_*` row are **mandatory** context before SQL; `enabled: false` omitted from tool output |
| Authoring capture routing | Disambiguation table: agent/SQL habits → **`ai-annotation`**; catalog narrative → **`descriptive`**; see WI-388 |
| Skymill reference | `skymill.segments` → join `cities` for city-name projection (GAP-1 **locked**) |
| Authoring wire | **`metadata-authoring.propose_facet_assignment`** with `facetTypeKey=ai-annotation` |

### 2. Payload fields (lock in design)

| Field | Schema | Required | Notes |
|-------|--------|----------|-------|
| `title` | STRING | no | Short operator label |
| `instruction` | STRING | **yes** | Free-form agent instruction |
| `kind` | ENUM | no | `sql_generation` (default), `tool_output`, `general` |
| `tags` | ARRAY STRING | no | `stereotype: tags` |
| `enabled` | BOOLEAN | no | Default true; false hides from agent surfaces |

### 3. Tool wire shape (lock in design)

On **`schema.list_schemas`**, **`list_tables`**, **`list_columns`** output items:

```json
"aiAnnotations": [
  {
    "title": "Airport name projection",
    "instruction": "When this table is used in SQL, join skymill.cities …",
    "kind": "sql_generation",
    "tags": [],
    "enabled": true
  }
]
```

Source: enabled `ai-annotation` facet assignments on that entity (`facetsResolved`, MULTIPLE rows).

## Out of scope

- Platform YAML seed (WI-384)
- Adapter / code changes (WI-385)
- Optional [`GAPS.md`](GAPS.md) unless `kind` enum or inheritance (schema → table) is debated during review

## Acceptance criteria

- Design doc linked from [`STORY.md`](STORY.md).
- Tool extension inventory lists exactly three extended tools and explicit exclusions.
- Skymill `segments` example documented with sample payload ([`GAPS.md`](GAPS.md) GAP-1).
- Payload and `aiAnnotations` wire shape match [`facet-type-descriptor-formats.md`](../../../design/metadata/facet-type-descriptor-formats.md).

## Deliverables

- This work item definition.
- [`docs/design/metadata/ai-annotation-facet-type.md`](../../../design/metadata/ai-annotation-facet-type.md) on the story branch.
