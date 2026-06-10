# Value mapping — indexing facet types

**Status:** Aligned with [**WI-181**](../../workitems/completed/20260417-value-mapping-facets-vector-lifecycle/WI-181-value-mapping-facet-types.md) (facet registry, `ValueSource`, indexing API) and [**WI-183**](../../workitems/completed/20260417-value-mapping-facets-vector-lifecycle/WI-183-value-mapping-capability-vector-retrieval.md) (RAG resolution). Canonical YAML blocks live in WI-181 § *Appendix — Canonical facet YAML*.

## Relationship to [`value-mapping-tactical-solution.md`](value-mapping-tactical-solution.md) (avoid drift)

| | **Tactical doc** | **This doc (normative facets, WI-181)** |
|---|------------------|----------------------------------------|
| **Role** | FileRepository-era **attribute** block **`value-mappings`** (manual rows + **`sources`** SQL) beside schema YAML — fast path while the faceted metadata stack matures. | **Metadata facet types** on attributes (**URNs** in **`platform-bootstrap.yaml`** + facet payloads on entities) driving **`ValueSource`**, dedupe, and **vector** indexing. |
| **Config shape** | `mappings[]` (`user-term`, `database-value`, …) and **`sources[]`** (`sql`, `reference-table`, …). | Primary facet **`ai-column-value-mapping`** + optional **`ai-column-value-mapping-values`** (`values[]` with **`content`** / **`value`**). |
| **Product alignment** | Same goals: RAG-ready values, DISTINCT / SQL-backed discovery, substitution for NL2SQL. | Same pipeline at runtime: **`content`** = embedding line; substitution = **`metadata["value"]`**; retrieval gated by **`similarityThreshold`** ([**mill-ai-configuration**](../ai/mill-ai-configuration.md)). |

**Rules:** Do **not** invent a third parallel vocabulary in either doc. **Normative** field names, thresholds, and **`AttributeValueEntry`** rules live in **WI-181** + this file + [**WI-172**](../../workitems/planned/metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md) (bridge from catalog **into** facets). The tactical doc remains the **historical / file-layout** reference; where behavior overlaps, treat **facets + WI-181** as authoritative for **mill-ai-v3** indexing and [**WI-183**](../../workitems/completed/20260417-value-mapping-facets-vector-lifecycle/WI-183-value-mapping-capability-vector-retrieval.md) resolution.

## Scope

Metadata **facet types** drive which **attributes** participate in value-mapping **indexing** and how **retrieval** is tuned. Targets are **column attributes only** (`urn:mill/metadata/entity-type:attribute`).

## Facet types

| URN | Cardinality | Role |
|-----|-------------|------|
| **`urn:mill/metadata/facet-type:ai-column-value-mapping`** | **SINGLE** (primary) | Opts the column into value mapping: **`context`**, optional **`similarityThreshold`**, **`nullValues`**, **`data`** (`enabled`, refresh flags). When **`data.enabled`**, backend DISTINCT values feed indexing like the Skymill Chroma IT prototype. |
| **`urn:mill/metadata/facet-type:ai-column-value-mapping-values`** | **MULTIPLE** (optional) | Static **`content` / `value`** pairs for a static list source. **Ignored** unless the primary facet exists on the **same** attribute. |

For the **seed `contentSchema`** shape of **`ai-column-value-mapping-values`** (including **`values`** as **ARRAY** whose **`items`** is **OBJECT** with **`content`** / **`value`**), see [`facet-payload-schema-reference.md`](facet-payload-schema-reference.md).

**Combination rule:** the primary facet **must** be present to enable value mapping for that column. Optional static rows are merged only when that holds.

## Payload highlights (primary facet)

- **`context`** — Semantic description prepended or combined with cell/static text when building **`AttributeValueEntry.content`** (full embedding line).
- **`similarityThreshold`** — Optional **0.0–1.0**. Resolution uses **top-1** search, then **accepts** only if similarity **≥** threshold; otherwise **no match** (see [**mill-ai-configuration**](../ai/mill-ai-configuration.md) § `mill.ai.value-mapping`). Product name stays **`similarityThreshold`**; LangChain4j maps it to **`EmbeddingSearchRequest.minScore`**.
- **`nullValues`** — **`indexNull`** / **`nullContent`**: control whether SQL NULL is indexed as a distinct bucket and how the NULL embedding line is phrased; **`metadata["isNull"]`** drives **`IS NULL`** in generated SQL (WI-181 § *NULL and `content` length*).
- **`data`** — **`enabled`** gates column-backed indexing; **`refreshAtStartUp`** / **`refreshInterval`** are interpreted by the refresh orchestrator ([**WI-182**](../../workitems/completed/20260417-value-mapping-facets-vector-lifecycle/WI-182-value-mapping-vector-refresh-lifecycle.md)).

## `ValueSource` and `CompositeValueSource`

- **`ValueSource`** — Given indexing context (attribute URN, bindings, parsed facet payloads), returns **`List<AttributeValueEntry>`**. Sources **do not** dedupe; they only **provide** rows.
- **`DistinctColumnValueSource`** — Column-backed DISTINCT path (physical identifiers via **`CatalogPath`** + **`SchemaProvider`** — WI-181 / WI-182).
- **`StaticListValueSource`** — Rows from facet B **`values[]`**.
- **`CompositeValueSource`** — **Concatenates** child lists in order; duplicates may pass through; **`ValueMappingService`** applies **dedupe on `content`** (last-wins) before sync.

## Indexing vs retrieval

- **Indexing** — `ValueSource` → **`ValueMappingService`** (dedupe) → **`VectorMappingSynchronizer`** → shared **`EmbeddingStore`**. **`AttributeValueEntry.metadata["value"]`** holds the **substitution** value for SQL/tools; **`content`** is the full embedding line (may differ when **`context`** prefixes the text).
- **Retrieval** — [**WI-183**](../../workitems/completed/20260417-value-mapping-facets-vector-lifecycle/WI-183-value-mapping-capability-vector-retrieval.md): production **`ValueMappingResolver`** uses the **same** **`EmbeddingHarness`** and **`EmbeddingStore`**; on a hit, substitute from **`metadata["value"]`**, not from raw **`content`**, when substitution is carried in metadata.

## Registry

Facet type definitions are registered in **`metadata/mill-metadata-core`** (`platform-bootstrap.yaml`). **Normative** schema copies: [**WI-181** appendix](../../workitems/completed/20260417-value-mapping-facets-vector-lifecycle/WI-181-value-mapping-facet-types.md#appendix--canonical-facet-yaml-normative).
