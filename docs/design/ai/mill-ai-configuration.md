# Mill AI configuration (`mill.ai.*`)

**Status:** Specification aligned with **WI-175**–**WI-177** (providers, embedding harness, vector store),
**WI-174** (DB repository for embeddings and value rows), **WI-179** (sync routine), and **WI-180**
(`ValueMappingService`) under [`docs/workitems/completed/20260416-implement-value-mappings/`](../workitems/completed/20260416-implement-value-mappings/STORY.md).
Property classes for **WI-175**–**WI-177** live in **`mill-ai-v3-autoconfigure`** as Java `@ConfigurationProperties`
with generated **`spring-configuration-metadata.json`**.

## Purpose

**`mill.ai.*`** is the **single umbrella** for Mill **AI functionality** configuration: provider
credentials, chat models, embedding model registries, vector stores, and future surfaces. New subtrees
are added **without** redefining the top-level prefix.

**Module placement:** contracts in **`ai/mill-ai-v3`**; Spring **`@ConfigurationProperties`** and wiring in
**`ai/mill-ai-v3-autoconfigure`**. See also [`../persistence/persistence-overview.md`](../persistence/persistence-overview.md).

## `mill.ai.enabled`

| Property | Default | Effect |
|----------|---------|--------|
| **`enabled`** | **`true`** (property may be omitted) | When **`false`**, **`mill-ai-v3-autoconfigure`** does **not** register AI beans (provider registry, embedding harness, vector store, chat runtime, schema/sql helpers, value-mapping sync wiring, etc.). The root **`AiConfigurationProperties`** bean still loads so the flag and YAML remain bindable. |

Use this on hosts that include the autoconfigure module on the classpath but must run **without** any AI stack (for example minimal services or tests).

The unified AI v3 **HTTP** surface in **`mill-ai-v3-service`** (chat lifecycle, profiles, related `@RestControllerAdvice`) applies the **same** gate via the composed **`ConditionalOnAiEnabled`** annotation (`mill.ai.enabled`), so those endpoints are not registered when AI is off.

## `mill.ai.providers` (WI-175)

**Per-provider credentials and shared HTTP settings**, keyed by **provider id** (e.g. `openai`).

| Concept | Description |
|---------|-------------|
| **Structure** | Map (or equivalent) **`mill.ai.providers.<providerId>.*`** |
| **Typical fields** | `api-key`, `base-url` (OpenAI-compatible API root), provider-specific options as needed |
| **Reuse** | Any feature that calls a remote provider resolves **`providerId`** → this subtree. **Secrets live here only**, not duplicated on every embedding profile. |

Other domains (chat, embeddings) **reference** a provider id; they do not embed API keys per logical profile.

## `mill.ai.embedding-model` (WI-176)

**Registry of named embedding profiles** (not tied to a single feature).

| Concept | Description |
|---------|-------------|
| **Structure** | Map **`mill.ai.embedding-model.<name>.*`** |
| **Each entry** | **`provider`** — id matching **`mill.ai.providers.<id>`**; plus **non-secret** params (remote model name, dimension, client options). |
| **Reuse** | Multiple features can point different **`mill.ai.<feature>.…`** properties at the **same** registry **name**. |

## `mill.ai.value-mapping` (WI-176)

**Facet-driven indexing:** two metadata facet types (`ai-column-value-mapping`, `ai-column-value-mapping-values`), **`ValueSource`** composition, and dedupe rules are summarized in [**Value mapping — indexing facet types**](../metadata/value-mapping-indexing-facet-types.md) ([**WI-181**](../../workitems/planned/value-mapping-facets-vector-lifecycle/WI-181-value-mapping-facet-types.md)). That doc also relates the normative facet model to the older [**tactical YAML**](../metadata/value-mapping-tactical-solution.md) layout so the two do not drift.

Value-mapping–specific settings; at minimum:

| Property | Description |
|----------|-------------|
| **`embedding-model`** | String: must equal a **key** in **`mill.ai.embedding-model`** — selects which registry entry the value-mapping embedding harness uses. |
| **`max-content-length`** | Positive integer (default **2048**): maximum **`String.length()`** for **`AttributeValueEntry.content`** (full embedding line). Longer lines are truncated before embed/persist; see [**WI-181**](../../workitems/planned/value-mapping-facets-vector-lifecycle/WI-181-value-mapping-facet-types.md) § *NULL and `content` length*. |

**Resolution quality (v1):** per-attribute **`similarityThreshold`** on facet **`ai-column-value-mapping`** ([**WI-181**](../../workitems/planned/value-mapping-facets-vector-lifecycle/WI-181-value-mapping-facet-types.md)) — after **top-1** vector search, accept the mapping only if similarity **≥** threshold. Implementations map the same number to LangChain4j **`EmbeddingSearchRequest.minScore`**. This name matches **`similarityThreshold`** in existing NL/value-mapping scenario YAML (**`mill-ai-v1-core`** test resources).

**Embedding profile change:** Changing **`embedding-model`** selects a different registry entry; [**WI-181**](../../workitems/planned/value-mapping-facets-vector-lifecycle/WI-181-value-mapping-facet-types.md) § **5b** — the synchronizer **re-embeds** when the resolved model id differs from persisted rows. **Dimension mismatch** with the vector store surfaces as a **failed** refresh for that attribute until the operator fixes config or clears vectors.

### `mill.ai.value-mapping.refresh` ([**WI-182**](../../workitems/planned/value-mapping-facets-vector-lifecycle/WI-182-value-mapping-vector-refresh-lifecycle.md))

Orchestrator settings (startup, scheduled tick cadence, gates). Normative detail: [**WI-182** § Configuration](../../workitems/planned/value-mapping-facets-vector-lifecycle/WI-182-value-mapping-vector-refresh-lifecycle.md).

| Property | Type (locked) | Notes |
|----------|----------------|-------|
| **`mill.ai.value-mapping.refresh.schedule-interval`** | **`java.time.Duration`** | Java field **`scheduleInterval`** on nested **`Refresh`** under **`ValueMappingConfigurationProperties`**. YAML / properties: **`mill.ai.value-mapping.refresh.schedule-interval: 15m`**. Spring Boot relaxed binding only — **no** custom converter. Default **`PT15M`**. |

## `mill.ai.vector-store` (WI-177)

**Single runtime vector store** per Spring context (similarity search / LangChain4j `EmbeddingStore`), **not** the golden-source DB tables from [**WI-174**](../workitems/completed/20260416-implement-value-mappings/WI-174-value-mapping-embedding-repository.md).

| Concept | Description |
|---------|-------------|
| **Cardinality** | **One** active backend per process — Spring **`@ConditionalOnProperty`** (or equivalent) so **only one** implementation bean is registered. |
| **Backends** | **`in-memory`** (default); **`chroma`** — LangChain4j `ChromaEmbeddingStore` over HTTP (`mill.ai.vector-store.chroma.*`). **pgvector** may attach later via the same prefix. |
| **Structure** | **`mill.ai.vector-store.*`** — implementation selector plus backend-specific **non-secret** fields; **secrets** via **`mill.ai.providers`** when needed. Chroma token/header auth is **not** exposed by LangChain4j’s builder yet; use network policies or a sidecar until upstream adds client credentials. |
| **Alignment** | Embedding **dimension** comes from **`mill.ai.embedding-model`** (WI-176); operators must configure the store implementation so dimensions **agree** with the chosen embedding profile. |

**Sync** from DB → this store is implemented by **`VectorMappingSynchronizer`** ([**WI-179**](../workitems/completed/20260416-implement-value-mappings/WI-179-sync-vectors-hydration.md)).
**`ValueMappingService`** ([**WI-180**](../workitems/completed/20260416-implement-value-mappings/WI-180-value-mapping-service-orchestrator.md)) deduplicates incoming values and delegates to the synchronizer; it requires
**`EmbeddingHarness`**, **`ValueMappingEmbeddingRepository`**, and an **`EmbeddingStore<TextSegment>`** bean.

### Caveats (vector store)

These constraints follow from [**WI-181**](../../workitems/planned/value-mapping-facets-vector-lifecycle/WI-181-value-mapping-facet-types.md) § *Shared `EmbeddingHarness` + `EmbeddingStore`* and from how LangChain4j similarity search works.

| Topic | Caveat |
|--------|--------|
| **One bean per runtime** | **Indexing** (sync into the store) and **retrieval** (value-mapping resolver / RAG-style search) must use the **same** `EmbeddingStore` Spring bean in a given context. A second store for the same feature set would split the corpus and break “sync then query” expectations. |
| **One embedding space** | Vectors stored together must come from **compatible** embeddings: **same dimension** and **same model family** as configured under **`mill.ai.embedding-model`**. For value mapping, point **`mill.ai.value-mapping.embedding-model`** at **one** registry profile; do not mix unrelated models into the same store without a deliberate design (separate stores, collections, or segment metadata). |
| **Not the DB** | This store is for **approximate nearest-neighbour search**. Authoritative rows and bytes remain in **`ai_value_mapping`** / **`ValueMappingEmbeddingRepository`** ([**WI-174**](../workitems/completed/20260416-implement-value-mappings/WI-174-value-mapping-embedding-repository.md)); the operator may run sync + DB without a vector backend, or run both. |
| **Collections / isolation (later)** | Backends such as **Chroma** can use **different collection names** for different product areas (e.g. isolating corpora). Mill’s **v1** story assumes a **global** configured store for value mapping; splitting collections or processes for separate workloads is an **optional follow-up**, not required for the first delivery. |

### `mill.ai.vector-store` properties

| Property | Type | Default | Notes |
|----------|------|---------|-------|
| **`mill.ai.vector-store.backend`** | enum | `in-memory` | `in-memory` or `chroma`. |
| **`mill.ai.vector-store.chroma.base-url`** | String | — | Required when `backend` is `chroma` (Chroma HTTP root, no trailing slash). |
| **`mill.ai.vector-store.chroma.api-version`** | `V1` / `V2` | `V2` | Chroma REST API variant. |
| **`mill.ai.vector-store.chroma.tenant-name`** | String | `default_tenant` | Chroma v2 tenant. |
| **`mill.ai.vector-store.chroma.database-name`** | String | `default_database` | Logical database within tenant. |
| **`mill.ai.vector-store.chroma.collection-name`** | String | `mill-value-mapping` | Target collection for embeddings. |
| **`mill.ai.vector-store.chroma.timeout`** | `Duration` | `60s` | HTTP client timeout. |

## Persistence vs search (WI-174)

The **`ValueMappingEmbeddingRepository`** port (**`mill-ai-v3`**) and JPA adapter (**`mill-ai-v3-persistence`**, Flyway **`ai_embedding_model`** / **`ai_value_mapping`**) hold the **authoritative** embedding bytes and model linkage.
**`mill.ai.vector-store`** configures only the **similarity-search** `EmbeddingStore`; operators run both when they need durable vectors and live search.

## `mill.ai.model` (existing)

Chat / LLM configuration (**`AiModelProperties`**) under **`mill.ai.model`** remains as implemented
today. **Optional follow-up:** align chat credentials with **`mill.ai.providers.openai`** so one OpenAI key
is configured once (not part of WI-175/176 closure).

## Resolution flow (embedding harness)

1. Read **`mill.ai.value-mapping.embedding-model`** → registry **name**.
2. Look up **`mill.ai.embedding-model.<name>`** → **`provider`** id + model params.
3. Resolve **`mill.ai.providers.<providerId>`** → credentials.
4. Invoke provider implementation (e.g. LangChain4j OpenAI embedding client) or **stub** in tests.

## Related documents

- [`rag-value-mapping-integration.md`](rag-value-mapping-integration.md) — RAG/value mapping behaviour (may evolve with new persistence).
- [`value-mapping-observability-actions.md`](value-mapping-observability-actions.md) — **action points** for metrics / observability gaps (sync, embed, vector store).
- [`../platform/mill-configuration.md`](../platform/mill-configuration.md) — broader Mill configuration map.
- [`../platform/CONFIGURATION_INVENTORY.md`](../platform/CONFIGURATION_INVENTORY.md) — Spring prefix inventory.
