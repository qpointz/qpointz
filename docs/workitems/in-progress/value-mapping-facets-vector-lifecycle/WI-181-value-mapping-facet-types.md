# WI-181 — Facet types, `ValueSource`, and indexing API

| Field | Value |
|--------|--------|
| **Story** | [`value-mapping-facets-vector-lifecycle`](STORY.md) |
| **Status** | `planned` |
| **Type** | `feature` / `docs` |
| **Area** | `metadata`, `ai` |
| **Depends on** | [`WI-180`](../../completed/20260416-implement-value-mappings/WI-180-value-mapping-service-orchestrator.md) / [`WI-179`](../../completed/20260416-implement-value-mappings/WI-179-sync-vectors-hydration.md). **`attributeUrn`** from **`mill-data-metadata`** (`ModelEntityUrn` — § *Attribute identity* below). Coordinate with [**WI-172**](../metadata-value-mapping/WI-172-metadata-value-mapping-bridge.md) for facet → catalog bridge. |

**Related work items (normative decisions migrated from the retired gaps doc):** global refresh — [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md); resolver / RAG — [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md); refresh state / **`STALE`** — [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md). Suggested execution order: [**STORY.md**](STORY.md) § *Suggested execution order* (WI order is **flexible** — see § *Work Items* / *Dependency hints*).

## Problem

Which **columns** feed **value-mapping** indexing and how **retrieval** is tuned must be **data-driven** from metadata, not ad hoc like [`ChromaSkymillDistinctVectorIT`](../../../../ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillDistinctVectorIT.kt). The informal WI-179 “value source contract” needs a **named port** (`ValueSource`) with **pluggable** implementations. **`ValueMappingService`** should accept a **`ValueSource`** for indexing (not only a raw entry list).

**Infrastructure (not a fat service):** indexing and **WI-183** query paths must use the **same** **`EmbeddingHarness`** (same embedding model / fingerprint) and the **same** **`EmbeddingStore`** bean — guaranteed by **Spring singleton wiring** in **`mill-ai-v3-autoconfigure`**, not by stuffing embed/search into **`ValueMappingService`**.

## Goal

### 1. Facet types (column-only targets)

**Normative design doc:** dedicated **[`value-mapping-indexing-facet-types.md`](../../../design/metadata/value-mapping-indexing-facet-types.md)** under **`docs/design/metadata/`** — link from [`mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md); **avoid drift** vs the tactical file-layout doc [`value-mapping-tactical-solution.md`](../../../design/metadata/value-mapping-tactical-solution.md) (relationship table and rules live in the facet doc § *Relationship to tactical YAML*).

**Two facet types (column-only)**

Canonical YAML: **§ Appendix — Canonical facet YAML** below (and **`platform-bootstrap.yaml`**). Column-only (`attribute`).

**`urn:mill/metadata/facet-type:ai-column-value-mapping`** (cardinality **SINGLE**) — **Primary.** Enables column for value mapping: **`context`**, **`similarityThreshold`**, **`nullValues`** (`indexNull`, `nullContent` — see *NULL and `content` length*), **`data`** (`enabled`, `refreshAtStartUp`, `refreshInterval`). When **`data.enabled`**: DISTINCT backend values like **`ChromaSkymillDistinctVectorIT`**; **`metadata["value"]`** = column value when not NULL; **`metadata["isNull"]`** when the bucket is SQL NULL; **`content`** = full embedding line (§4 **`AttributeValueEntry`**).

**Refresh flags (interpreted by [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) orchestrator):** **`data.refreshAtStartUp`** — if **true**, the attribute may be refreshed on **application startup** runs (subject to global **`mill.ai.value-mapping.refresh.on-startup.enabled`**). **`data.refreshInterval`** — duration between refreshes for **scheduled** runs; compared against **`ai_value_mapping_state.last_refresh_at`** (**WI-184**); **ignored** for **manual** / **on-demand** reindex (run kind **`ON_DEMAND`**). If **`refreshInterval`** is **absent**, **scheduled** passes **do not** refresh that attribute (only **startup** / **`ON_DEMAND`** apply), unless a later change introduces a default.

**`urn:mill/metadata/facet-type:ai-column-value-mapping-values`** (cardinality **MULTIPLE**) — **Optional** static rows: **`values[]`** of **`content`** / **`value`** pairs for **`StaticListValueSource`**. **Only processed if** the primary facet is present on the same attribute; otherwise **ignore** (no merge or index).

**Composition:** **`CompositeValueSource`** concatenates column-backed + static lists; **`ValueMappingService`** dedupes on **`content`** (§ *Dedup*).

**DISTINCT semantics:** **NULL** included when **`nullValues.indexNull`**; embedding line uses **`nullContent`** for the NULL bucket so NL2SQL can later resolve to **`IS NULL`**. Large cell text **not** special-cased (no streaming-only path). **`AttributeValueEntry.content`** length capped by **`mill.ai.value-mapping.max-content-length`** (default **2048**; truncate; see *NULL and content length* below).

**Retrieval:** Value mapping resolves **one** user phrase to **one** substitution value — vector lookup is **top-1** (single best match). **`similarityThreshold`** (optional, 0.0–1.0) is the **quality gate** on that **one** candidate: **accept** the mapping only if the best match’s **similarity score** is **≥** the threshold; otherwise **no match**. It does **not** mean “return every hit above a floor” (there is only one candidate). Same product name in facet payload and resolver; implementation maps to LangChain4j **`EmbeddingSearchRequest.minScore`** (backend parameter name may differ). Aligns with **`similarityThreshold`** used in existing NL/value-mapping test scenarios (**`mill-ai-v1-core`**). No facet **`topK`** (store **`limit = 1`** is an implementation detail).

### 2. `ValueSource` port + implementations (`mill-ai-v3`)

- Formal **`ValueSource`** interface: given an **indexing context** (attribute URN, column binding, parsed facet payload(s)), returns **`List<AttributeValueEntry>`**. Sources are **providers only** — they **do not** apply product dedupe (§ *Dedup*).
- **`DistinctColumnValueSource`**, **`StaticListValueSource`**, **`CompositeValueSource`** (multiple facets on one column: **concatenate** child `ValueSource` results in order; **no** dedupe inside the composite — optional pass-through duplicates are allowed). When **`nullValues.indexNull`** (*NULL and `content` length*), add a **separate** child **`ValueSource`** that yields **one** NULL-bucket row; **production** assembly from metadata is normative in [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) § *Production metadata retrieval* / **`mill-data-metadata`**.

### 3. `ValueMappingService` indexing API

- Add **`syncFromSource(attributeUrn, source: ValueSource, embeddingModelId, progress: ValueMappingSyncProgressCallback? = null)`** (signatures TBD): **`source` → entries → dedupe (consumer responsibility, last-wins on `content` per §4)** → delegate to **`VectorMappingSynchronizer`** (same WI-179 routine), emitting **per-element** progress via the optional callback. Callback contract and persisted state: [**WI-184**](../value-mapping-facets-vector-lifecycle/WI-184-value-mapping-refresh-state-persistence.md) § *Integration*; [**WI-182**](../value-mapping-facets-vector-lifecycle/WI-182-value-mapping-vector-refresh-lifecycle.md) orchestrator supplies the callback and **reduces/debounces** service events before writing **`ai_value_mapping_state`** (e.g. **10%** steps).
- **Keep** **`syncAttribute(attributeUrn, entries, embeddingModelId)`** for thin callers and existing tests unless the project explicitly drops it.

### 4. `AttributeValueEntry` + synchronizer semantics

**`AttributeValueEntry` identity:** **`content`** is always the **full string** passed to **`EmbeddingHarness.embed`**: when a facet **`context`** is set, that string is **`context` + column/static value** (per facet spec). **`metadata["value"]`** is the **substitution value** for SQL/query/tool use (canonical value from the column or static pair). **WI-179 / repository** uniqueness remains **`(attribute_urn, content)`** — the DB **`content`** column stores the **embedding line**, not a separate “substitution-only” key; substitution lives in **metadata** (persisted as **`metadata_json`** on `ai_value_mapping`).

- KDoc: document the above; always set **`metadata["value"]`** where substitution must be distinguished from **`content`** (static list; distinct path when context prefix is used — and **may** match **`content`** when no prefix).
- **`DefaultVectorMappingSynchronizer`:** segment / store metadata should expose **substitution** by merging **`AttributeValueEntry.metadata`** into the LangChain4j **`TextSegment`** (embedding-store payload), not only into DB **`metadata_json`**, so retrieval ([**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md)) can read **`metadata["value"]`** / **`isNull`** from the segment. **Specification** ([**STORY.md**](STORY.md) § *Gaps*, closed); **implementation** when coding starts.

### 5. Shared `EmbeddingHarness` + `EmbeddingStore` (with WI-183)

- **Do not** grow **`ValueMappingService`** into a god object for search.
- **Do** document and implement in autoconfigure: **one** `EmbeddingHarness` bean + **one** `EmbeddingStore` bean injected into **`VectorMappingSynchronizer`** and into the **production `ValueMappingResolver`** (WI-183). Same instances for “same model / same store.” **Global `EmbeddingStore` (v1):** one Spring bean and one **`mill.ai.vector-store`** config shared by indexing and retrieval; optional multi-collection / multi-store later — keep one value-mapping embedding profile so vector dimensions stay consistent.

### 5b. Embedding profile change — detection and reindex (**WI-181** owns)

When the operator changes **`mill.ai.value-mapping.embedding-model`** (registry key) or the resolved **embedding model id** / dimensions change:

| Concern | Rule |
|---------|------|
| **Detection** | **`DefaultVectorMappingSynchronizer`** / **`ValueMappingEmbeddingRepository`** already compare **`embeddingModelId`** per value row (**WI-179**). A new config resolves to a **new** model id → next **`syncFromSource`** for that attribute **re-embeds** rows where **`needReembed`** (id mismatch or missing vector). **No** separate “version table” required for v1. |
| **Full reindex** | Treat as **full reindex** of that attribute’s vectors when the model id changes — same as running a fresh sync for all distinct entries (existing WI-179 loop). |
| **Dimension mismatch** | If the new model’s vector **dimension** does not match **`EmbeddingStore`** / backend expectations, sync **fails** for that attribute (**FAILED** / **`status_detail`** via [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) / [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md)); operator must fix config or clear the store — document in [`mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md). |
| **Orchestrator** | [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) global passes **invoke** sync with the **current** resolved model id from config; they do **not** need separate “embedding change detection” beyond what the synchronizer already does per row. |

**Acceptance (WI-181) — mandatory:** Automated test (**unit** or **integration**) that **asserts** **re-embed** when persisted **`embedding_model_id`** (or equivalent) **differs** from the **currently resolved** model id from **`mill.ai.value-mapping.embedding-model`**: either extend existing [**WI-179**](../../completed/20260416-implement-value-mappings/WI-179-sync-vectors-hydration.md) / synchronizer tests or add a focused case on **`needReembed`** / **`VectorMappingSynchronizer`**. **No** manual-only checklist for this row.

### Attribute identity (`attributeUrn`)

**`attributeUrn`** for value mapping (**`syncFromSource`**, **`ai_value_mapping.attribute_urn`**, DISTINCT, NL2SQL alignment) uses **`mill-data-metadata`** — **`io.qpointz.mill.data.metadata.ModelEntityUrn`** (`urn:mill/model/attribute:<schema>.<table>.<column>`). The **`metadata/`** stack stores opaque **`entity_res`**; **catalog binding** for the data plane goes through **`mill-data-metadata`** / **`CatalogPath`**, not a parallel ad hoc string. See [`metadata-urn-platform.md`](../../../design/metadata/metadata-urn-platform.md) § relational model URNs.

### NULL and `content` length (indexing)

SQL **NULL** may be business-meaningful (e.g. unknown status). Primary facet **`nullValues`**: **`indexNull: true`** — include one NULL bucket row when NULL appears; **`indexNull: false`** — omit. NL2SQL uses **`metadata["isNull"]` = true** so generated SQL uses **`IS NULL`**, not **`'null'`**. Omitted **`nullValues`** → treat **`indexNull`** as **false**. **`content`** max length: **`mill.ai.value-mapping.max-content-length`** (default **2048**); truncate; log at debug/trace. Collation: backend DISTINCT order unless product adds a locale rule.

### Dedup and retrieval acceptance (`similarityThreshold`) (summary)

- **`ValueSource`:** providers only; **`ValueMappingService`** dedupes (last-wins on **`content`**). **`CompositeValueSource`** concatenates; no dedupe inside composite.
- **`similarityThreshold`:** optional on the primary facet — after **top-1** search, **accept** iff similarity **≥** threshold (**WI-183**). Higher value = stricter (more likely **no match**). Implementation passes the same numeric bound into **`EmbeddingSearchRequest.minScore`** (or equivalent) when calling the store.

## Non-goals

- **Startup** / **scheduler** — [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md).
- Full **capability** wiring — [**WI-183**](WI-183-value-mapping-capability-vector-retrieval.md) (but §5 is a hard constraint for WI-183).

## Acceptance criteria

- Register facet types in **`metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml`**: append two **`kind: FacetTypeDefinition`** entries (`urn:mill/metadata/facet-type:ai-column-value-mapping`, `urn:mill/metadata/facet-type:ai-column-value-mapping-values`) using the canonical **`contentSchema`** from **§ Appendix — Canonical facet YAML** below (same shape as existing facet blocks — `---` separators, `applicableTo`, `schemaVersion`). Update the file header comment if the “authoritative set” line should list these URNs.
- **Normative design doc (WI-181):** dedicated [`value-mapping-indexing-facet-types.md`](../../../design/metadata/value-mapping-indexing-facet-types.md) + link from [`mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md); **avoid drift** vs [`value-mapping-tactical-solution.md`](../../../design/metadata/value-mapping-tactical-solution.md) (facet doc § *Relationship to tactical YAML*). Lists facet type URNs, **`ValueSource`** + **composite concatenation** rules (dedupe as **`ValueMappingService`** only); canonical YAML copies remain in **§ Appendix** below.
- **`ValueSource`** implementations + **unit tests**; separate tests for **`ValueMappingService`** dedupe on **`content`**.
- **`ValueMappingService`** exposes **source-based** sync; list-based sync still works or is explicitly deprecated in the WI notes.
- **`ValueMappingConfigurationProperties`** (`mill.ai.value-mapping`): add **`max-content-length`** (default **2048**); wire into **`ValueMappingService`** / **`ValueSource`** entry builders so truncation is centralized. **`mill.ai.value-mapping.refresh.*`** (startup / schedule / interval) lives in **[WI-182](WI-182-value-mapping-vector-refresh-lifecycle.md)** — extend the same properties class or a nested **`Refresh`** record per Spring style. **`CONFIGURATION_INVENTORY.md`** + **`mill-ai-configuration.md`** + **`additional-spring-configuration-metadata.json`** (if properties stay Kotlin) updated per repo rules.
- Cross-link [**WI-157**](../ai-value-mapping-capability/WI-157-ai-v3-value-mapping-production-and-facet-rag.md) for facet-driven RAG scope.
- **§ 5b — embedding profile change:** automated test per § **5b** *Acceptance (WI-181) — mandatory* (re-embed when model id changes); [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) does **not** add separate detection — it passes **current** model id into **`syncFromSource`** only.

## Appendix — Canonical facet YAML (normative)

**Targets:** column only — `applicableTo: [ urn:mill/metadata/entity-type:attribute ]` for both types.

**Combination rule:** (1) **`ai-column-value-mapping`** (SINGLE) — required to opt the column in; declares context, retrieval, NULL handling, data/refresh. (2) **`ai-column-value-mapping-values`** (MULTIPLE) — optional static pairs; **ignored** unless (1) is on the same attribute.

**Registry:** seed both **`FacetTypeDefinition`** documents in **`metadata/mill-metadata-core/src/main/resources/metadata/platform-bootstrap.yaml`** (same stream as other platform facet types). **Do not** duplicate definitions in Flyway SQL.

#### Facet A — `ai-column-value-mapping`

```yaml
facetTypeUrn: urn:mill/metadata/facet-type:ai-column-value-mapping
title: Value Mapping
description: When applied, indicates the column should be used for value mapping.
enabled: true
mandatory: false
targetCardinality: SINGLE
contentSchema:
  type: OBJECT
  title: Value mapping payload
  description: Semantic context, retrieval acceptance (similarity threshold), and column-backed indexing controls.
  fields:
    - name: context
      schema:
        type: STRING
        title: Context
        description: General description of the value type; semantic meaning of values
          in this column (used when building the embedding line together with
          the cell value; see §4 AttributeValueEntry).
      required: true
    - name: similarityThreshold
      schema:
        type: NUMBER
        title: Similarity threshold
        description: >-
          Optional. After top-1 vector search, accept the mapping only if the best match’s
          similarity score (0.0–1.0, higher = better match) is greater than or equal to this
          value; otherwise treat as no match. Does not filter a list of hits — retrieval is
          always a single best candidate first, then this gate. Maps to EmbeddingSearchRequest.minScore
          in LangChain4j.
      required: false
    - name: nullValues
      schema:
        type: OBJECT
        title: NULL Values
        description: How SQL NULL in the column participates in indexing and NL2SQL.
          NULL may mean "unknown" (e.g. status active/disabled/NULL) and must be
          retrievable so NL2SQL can emit WHERE col IS NULL when the user asks
          for unknown status.
        fields:
          - name: indexNull
            schema:
              type: BOOLEAN
              title: Index NULL
              description: When true, include NULL as a distinct bucket in DISTINCT/sync (one
                AttributeValueEntry). When false, omit NULL from indexing
                (legacy opt-out).
            required: true
          - name: nullContent
            schema:
              type: STRING
              title: NULL semantic text
              description: Text used in the embedding line for NULL rows so natural phrases
                (e.g. "unknown status") align semantically; combined with
                context per §4. Ignored when indexNull is false.
            required: false
        required: []
      required: false
    - name: data
      schema:
        type: OBJECT
        title: Column data
        description: Controls value mapping sourced from this column's values.
        fields:
          - name: enabled
            schema:
              type: BOOLEAN
              title: Index column values
              description: When true, distinct values from the column are indexed for value
                mapping.
            required: true
          - name: refreshAtStartUp
            schema:
              type: BOOLEAN
              title: Refresh at startup
              description: When true, refresh indexed values from the column when the service
                starts.
            required: true
          - name: refreshInterval
            schema:
              type: STRING
              title: Refresh interval
              description: Frequency for refreshing column values when enabled (e.g. 1mi = 1
                minute, 5d = 5 days).
            required: false
        required: []
      required: true
  required: []
category: ai
applicableTo:
  - urn:mill/metadata/entity-type:attribute
schemaVersion: "1.0"
```

#### Facet B — `ai-column-value-mapping-values`

```yaml
facetTypeUrn: urn:mill/metadata/facet-type:ai-column-value-mapping-values
title: Value Mapping Values
description: Static content/value pairs for value mapping (only applies when Value Mapping is present).
enabled: true
mandatory: false
targetCardinality: MULTIPLE
contentSchema:
  type: OBJECT
  title: Static value list payload
  description: >-
    Manual or supplemental list of embedding text (content) and substitution value (value) pairs.
    Only processed when facet ai-column-value-mapping is attached to the same attribute.
  fields:
    - name: values
      schema:
        type: ARRAY
        title: Values
        description: Static rows for indexing and resolution (see §4 for content vs value).
        items:
          type: OBJECT
          title: Static value row
          description: One pair — semantic text to embed and canonical value for substitution.
          fields:
            - name: content
              schema:
                type: STRING
                title: Content
                description: Text to embed for semantic match (full embedding line or fragment per facet rules; see §4 AttributeValueEntry).
              required: true
            - name: value
              schema:
                type: STRING
                title: Value
                description: Canonical value to use when this row matches (substitution for queries/tools).
              required: true
      required: true
  required: []
category: ai
applicableTo:
  - urn:mill/metadata/entity-type:attribute
schemaVersion: "1.0"
```

## Deliverables

- **`platform-bootstrap.yaml`** updated with both value-mapping **`FacetTypeDefinition`** entries (see Acceptance).
- Design note + registry examples.
- `mill-ai-v3` ports + implementations + tests.
- Autoconfigure note or early bean wiring sketch for **shared** harness/store (final resolver in WI-183).
