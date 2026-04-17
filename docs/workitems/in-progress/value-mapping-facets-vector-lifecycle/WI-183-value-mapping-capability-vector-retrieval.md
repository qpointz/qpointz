# WI-183 — Value mapping capability + vector retrieval

| Field | Value |
|--------|--------|
| **Story** | Follow-on to [`value-mapping-facets-vector-lifecycle`](STORY.md) — **not** in that story’s tracked checklist (**181 / 184 / 182 / 185**); same milestone folder until a dedicated follow-on story file exists. |
| **Status** | `planned` |
| **Type** | `feature` / `test` |
| **Area** | `ai` |
| **Depends on** | [**WI-181**](WI-181-value-mapping-facet-types.md) — retrieval facet parameters + **shared bean rule**. [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) — **recommended** so the store is populated before agents query. **Normative:** § *Normative decisions* below (indexing SQL + RAG resolution). Overlap with [**WI-157**](../ai-value-mapping-capability/WI-157-ai-v3-value-mapping-production-and-facet-rag.md). |

## Problem

[`ValueMappingCapability`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/valuemapping/ValueMappingCapability.kt) delegates to **`ValueMappingResolver`**. Production paths still use **mocks** or non-vector logic.

**Product model:** Value mapping solves **mismatch** between what the **user says** (e.g. “Switzerland”, “active”) and what the **database stores** (e.g. ISO code `CH`, status codes). **Resolution** — turning user text into the **substitution value** for generated SQL — is **exclusively** via **RAG**: similarity search on the same **`EmbeddingStore`** used at index time (§ *Normative decisions*). **Raw column values in the DB are not** an authoritative resolver input for that mapping; they only feed **indexing** (DISTINCT / static facets → vectors, [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md)). **Indexing** SQL (DISTINCT, physical names, **`IS NULL`** in emitted SQL) is specified in § Goal 2 — not a parallel “live DB lookup” resolver.

## LLM tools + resolver (planning contract)

**Scope:** [`ValueMappingToolHandlers.kt`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/valuemapping/ValueMappingToolHandlers.kt) / [`get_value_mapping`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/valuemapping/ValueMappingCapability.kt) when the LLM asks to normalize user phrases to **stored** values. **Normative for production `ValueMappingResolver`.**

| Step | Behaviour |
|------|------------|
| **1. Column metadata** | Resolve `tableId` + `attributeName` → attribute URN; load **facet** rows (primary `ai-column-value-mapping` + optional `ai-column-value-mapping-values`). Implementations may **cache** facet payloads per attribute URN (TTL or request-scoped). |
| **2. Value-mapping enabled?** | If the primary facet is **absent** or **`data.enabled`** is false: **do not** call RAG. Return each requested string unchanged (**passthrough**) in [`ValueResolution.mappedValue`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/valuemapping/ValueMappingCapability.kt) (same as input), or a single agreed sentinel — **pick one policy** in implementation and document in KDoc. |
| **3. RAG path** | If enabled: embed the phrase with the **same** **`EmbeddingHarness`** as [`VectorMappingSynchronizer`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/valuemap/DefaultVectorMappingSynchronizer.kt); query the **same** **`EmbeddingStore`**; **top-1**; accept only if similarity **≥** facet **`similarityThreshold`**, else use **`mill.ai.value-mapping.*`** default when the facet omits it. |
| **4. No acceptable match** | Below threshold, empty store, or miss: **passthrough** (same as §2) — user phrase passes through to SQL / planner unless product chooses **null** to signal “unresolved”; align tool YAML + handler docs. |
| **5. Substitution on hit** | **Critical:** the value substituted into generated SQL / tools must come from **`metadata["value"]`** on the matched **indexed** row (see [**WI-181**](WI-181-value-mapping-facet-types.md) § `AttributeValueEntry` / dedup on `content`). **Do not** use the embedding **text** (`content` / `TextSegment` body) as the canonical DB value when `metadata["value"]` is present — that string is the **full embedding line**; substitution is the **cell / static** value carried in metadata. Indexing must **persist** `value` (and e.g. `isNull`) into **vector store segment metadata** alongside sync so retrieval can read it back. |

**References:** indexing side [`DefaultVectorMappingSynchronizer`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/valuemap/DefaultVectorMappingSynchronizer.kt) + [`AttributeValueEntry`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/valuemap/AttributeValueEntry.kt); tool surface [`ValueMappingToolHandlers.resolveValues`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/capabilities/valuemapping/ValueMappingToolHandlers.kt).

## Infrastructure rule (from WI-181)

**Same embedding model:** the resolver must use the **same** **`EmbeddingHarness`** bean (same fingerprint / model config) as **`VectorMappingSynchronizer`** for turning **user search text** into a query vector.

**Same vector store:** the resolver must query the **same** **`EmbeddingStore<TextSegment>`** bean that sync writes to.

Achieve this by **constructor injection of the singleton beans** in **`mill-ai-v3-autoconfigure`** — **not** by routing query/embed through **`ValueMappingService`** (avoid a god object). **`ValueMappingService`** remains the **indexing** façade (`ValueSource` → sync).

## Normative decisions (consolidated from retired gaps doc)

**Closure:** Spec-level decisions for this story are **closed** in work items; implementation is tracked here and in [**WI-181**](WI-181-value-mapping-facet-types.md) / [**WI-182**](WI-182-value-mapping-vector-refresh-lifecycle.md) / [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md).

**RAG-only resolution:** **`ValueMappingResolver.resolveValues`** uses **only** similarity search over **`EmbeddingStore`**. There is **no** resolver path that maps user language by reading live JDBC / “authoritative” DISTINCT literals from the database.

**Indexing vs resolver:** DISTINCT + **`CatalogPath`** + **`SchemaProvider`** + backtick SQL (or future query plan) apply to **building vectors** (**`ValueSource`**, global refresh) and to **emitting** SQL such as **`IS NULL`** after a match — see § Goal 2. **`metadata["isNull"]` → `IS NULL`:** per [**WI-181**](WI-181-value-mapping-facet-types.md) § *NULL and `content` length*.

**Stale physical catalog:** if **`CatalogPath`** parses but **`SchemaProvider`** has no matching object, **WI-182** skips refresh and persists **`STALE`** via [**WI-184**](WI-184-value-mapping-refresh-state-persistence.md).

## Goal

1. **Production `ValueMappingResolver`** in **`mill-ai-v3`** / **`mill-ai-v3-autoconfigure`** that:
   - Injects the **same** **`EmbeddingStore`** and **`EmbeddingHarness`** beans as **`VectorMappingSynchronizer`**.
   - Reads **WI-181** facet metadata for **retrieval**: optional **`similarityThreshold`** on the primary facet — after **top-1** search, **accept** iff the best match’s similarity **≥** threshold (implementation: **`EmbeddingSearchRequest.minScore`**). Single value → single value; no **`topK`** on the facet.
   - **`resolveValues`:** **only** **similarity search** on **`EmbeddingStore`** (RAG). **No** alternate path that resolves mapping by reading “authoritative” live DB values.
2. **Indexing / SQL emission (`IS NULL`, DISTINCT for vectors)** — When **building** **`ValueSource`** / executing DISTINCT for **indexing**, or when **emitting** SQL fragments with correct physical names / **`IS NULL`** (not when **resolving** user text — that is § Goal 1):
   - **Prototype:** [`ChromaSkymillDistinctVectorIT.kt`](../../../../ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillDistinctVectorIT.kt) — **`QueryRequest`** / **`SQLStatement`**, **`DataOperationDispatcher.execute`**, **`RecordReaders`**. **Note:** you may **replace** raw SQL with a **query plan** or relational IR later for **interoperability** across backends; treat the test as behavioural reference, not a permanent SQL-string requirement.
   - **Identifier quoting:** Generated SQL uses **backticks** around **schema**, **table**, and **column** names (consistent with the prototype, e.g. `` SELECT DISTINCT `col` FROM `schema`.`table` ``).
   - **Physical schema, table, column names:** Obtain **exact** identifiers only via **[`mill-data-metadata`](../../../../data/mill-data-metadata)** — **`ModelEntityUrn.parseCatalogPath(attributeUrn)`** → **`CatalogPath`** (see **`CatalogPath.kt`**, **`RelationalMetadataEntityUrns`**, **`ModelEntityUrn`**) and cross-check with **`io.qpointz.mill.data.backend.SchemaProvider`** (same contract as **[`LogicalLayoutMetadataSource`](../../../../data/mill-data-metadata/src/main/kotlin/io/qpointz/mill/data/metadata/source/LogicalLayoutMetadataSource.kt)**). SQL or plan text must reflect **SchemaProvider** naming, not ad hoc strings.
   - **`metadata["isNull"]` → `IS NULL`:** per [**WI-181**](WI-181-value-mapping-facet-types.md) — generated SQL (or plan) must use **`IS NULL`**, not a literal **`'null'`** string.
   - **Tests (reuse for plan):** Structure **`test` / `testIT`** so **expected distinct values**, **NULL** handling, and **`IS NULL`** propagation are asserted on **outcomes**, not only on SQL string equality. Shared **fixtures** or a thin **“execute DISTINCT for attribute”** abstraction allows the **same** cases to run when execution falls back from **SQL** to **query-plan** generation.
3. Wire **`ValueMappingCapabilityDependency`** in **`SchemaFacingCapabilityDependencyFactory`** (and CLI / test slices) when **`mill.ai.enabled`** and beans exist.
4. **Tests** — unit tests for branches; optional **`testIT`** with in-memory store seeded via **`syncFromSource`** or **`syncAttribute`**.

## Non-goals

- New **HTTP** APIs ([**WI-173**](../metadata-value-mapping/WI-173-metadata-value-mapping-api-and-ui.md)).
- UI.

## Acceptance criteria

- **`MockValueMappingResolver`** is not the default in **mill-service** / primary CLI when vector infra is configured.
- At least one test proves **vector** resolution using **WI-181-style** facets + **shared** store/harness.
- **Indexing DISTINCT / SQL:** **`CatalogPath`** + **`SchemaProvider`** alignment, **backtick** SQL quoting, tests **reusable** for a future query-plan path; **`IS NULL`** emission covered or explicitly deferred with a linked follow-up.
- **`resolveValues`:** **RAG-only** (no live-DB authoritative mapping path).
- Design note links **`ValueMappingResolver`** ↔ singleton **`EmbeddingStore`** / **`EmbeddingHarness`** ↔ WI-181 retrieval facet.

## Deliverables

- Code + tests per [`RULES.md`](../../RULES.md).
- Coordinate scope with [**WI-157**](../ai-value-mapping-capability/WI-157-ai-v3-value-mapping-production-and-facet-rag.md) in MR description.
