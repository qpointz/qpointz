# WI-385 — Schema capability tool extension

| Field | Value |
|--------|--------|
| **Story** | [`ai-annotations-facet`](STORY.md) |
| **Status** | `done` |
| **Type** | `feature` |
| **Area** | `ai`, `metadata` |
| **Depends on** | [**WI-384**](WI-384-ai-annotation-platform-seed.md) |
| **Enables** | [**WI-386**](WI-386-ai-annotation-sql-profile.md) |

## Problem

[`SchemaFacetCatalogAdapter`](../../../ai/mill-ai-data/src/main/kotlin/io/qpointz/mill/ai/data/schema/SchemaFacetCatalogAdapter.kt) maps only **`descriptive`**
(`displayName`, `description`) onto schema tool results. **`ai-annotation`** assignments are
invisible to agents even after WI-384 seeds the facet type.

Per [`schema-facet-ai-tool-field-mapping.md`](../../../design/metadata/schema-facet-ai-tool-field-mapping.md), capability tools today expose entity facets only on **`schema`** — and only
**`list_schemas`**, **`list_tables`**, **`list_columns`** carry descriptive fields.

## Goal

Extend exactly those **three** tools with **`aiAnnotations`** — no new tools, no changes to
**`list_relations`** or **`resolve_metadata_entity`**.

## Deliver

### 1. Port types (`mill-ai`)

**File:** [`SchemaCatalogPort.kt`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/capabilities/schema/SchemaCatalogPort.kt)

- Add `AiAnnotationItem` (or equivalent) data class: `title`, `instruction`, `kind`, `tags`, `enabled`.
- Add `aiAnnotations: List<AiAnnotationItem> = emptyList()` to:
  - `ListSchemasItem`
  - `ListTablesItem`
  - `ListColumnsItem`

### 2. Mapper (`mill-ai-data`)

**File:** [`SchemaFacetCatalogAdapter.kt`](../../../ai/mill-ai-data/src/main/kotlin/io/qpointz/mill/ai/data/schema/SchemaFacetCatalogAdapter.kt)

- For each `list_*` row, read **`SchemaFacets.facetsResolved`** (not typed `SchemaFacets` converters).
- Filter rows where `facetTypeKey` resolves to `ai-annotation`.
- Filter rows with `enabled == false`; omit `enabled` from wire items (GAP-2 **locked**).
- Preserve existing `descriptive` mapping unchanged.

**Note:** Do **not** add `ai-annotation` to [`SchemaFacets.fromResolved`](../../../data/mill-data-schema-core/src/main/kotlin/io/qpointz/mill/data/schema/SchemaFacets.kt) typed merge (same exclusion pattern as value-mapping facets).

### 3. Capability manifest

**File:** [`capabilities/schema.yaml`](../../../ai/mill-ai/src/main/resources/capabilities/schema.yaml)

- Add `aiAnnotations` output property on `list_schemas`, `list_tables`, `list_columns` item schemas.
- Extend `schema.system` prompt: when `aiAnnotations` is non-empty on a resolved entity, treat as hard requirements before SQL (especially `kind: sql_generation`).

### 4. Design cross-link

**File:** [`schema-facet-ai-tool-field-mapping.md`](../../../design/metadata/schema-facet-ai-tool-field-mapping.md)

Add section:

| Seed field | Tool field | Tools |
|------------|------------|-------|
| `title`, `instruction`, `kind`, `tags`, `enabled` | `aiAnnotations[]` | `list_schemas`, `list_tables`, `list_columns` |

### 5. Tests

| Test | Assert |
|------|--------|
| `SchemaFacetCatalogAdapter` unit test | Mock `facetsResolved` → `aiAnnotations` populated; `enabled: false` excluded |
| `SchemaFacetServiceSkyMillIT` (or adapter IT) | `segments` table row includes Skymill fixture instruction from WI-384 |
| Regression | `displayName` / `description` unchanged when only descriptive present |

## Out of scope

- `sql-query` prompts (WI-386)
- `list_relations` / `metadata` / `concept` tools
- MCP-specific wiring (inherits from schema tool schemas)

## Acceptance criteria

- `schema.list_tables("skymill")` returns `aiAnnotations` on `segments` with WI-384 instruction text.
- `list_schemas` and `list_columns` expose `aiAnnotations` when assignments exist on those entities.
- `list_relations` and `resolve_metadata_entity` outputs unchanged.
- `schema.yaml` documents `aiAnnotations` on all three `list_*` tools.
- Unit/IT tests green for adapter and Skymill fixture path.

## Deliverables

- This work item definition.
- Port, adapter, manifest, mapping doc, tests on the story branch.
