# WI-179 - Sync vectors (value source -> repository -> vector store)

**Story:** [`implement-value-mappings`](STORY.md) (follow-on)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `feature` |
| **Area** | `ai` |
| **Depends on** | [**WI-174**](WI-174-value-mapping-embedding-repository.md), [**WI-176**](WI-176-embedding-model-harness.md), [**WI-177**](WI-177-vector-store-harness.md). **Input:** [**Value source contract**](#value-source-contract) below. |

## Clarification (vs WI-180)

| Concern | **WI-179 (this)** | [**WI-180**](WI-180-value-mapping-service-orchestrator.md) |
|--------|-------------------|-----------------------------------------------------------|
| **Role** | **Reconciliation algorithm** - keep **three layers** aligned for one **attribute**: **backend values**, **vector repository** (golden source), **runtime vector store**. | **`ValueMappingService`** invokes **this** routine; **query resolution** (similarity, thresholds) is **out of scope** for [`implement-value-mappings`](STORY.md) - **separate** work. |
| **Analogy** | ETL / indexer: deterministic steps below. | Facade for **sync** + future resolve APIs. |

## Three layers (mental model)

| Layer | Role |
|-------|------|
| **Values** | **Authoritative list** for the attribute, supplied by the **value source** (backend / query / facet - **not** the repository). |
| **Vector repository** | [**WI-174**](WI-174-value-mapping-embedding-repository.md) - **`attribute_urn`**, **`content`** (string), **`embedding`**, **`embedding_model_id`** -> **`ai_embedding_model.id`**, **`stable_id`**, optional **metadata** columns. |
| **Vector store** | [**WI-177**](WI-177-vector-store-harness.md) - LangChain4j **`EmbeddingStore`**; **add-or-replace** by **`stable_id`**; **delete** by id is supported at **`EmbeddingStore`** level. |

## Value source contract

Expose a **small port** (name TBD) that yields **entries** for one **attribute URN** per sync invocation, analogous to LangChain4j **`TextSegment`**: **text** (required) plus **metadata** (optional map) that must be **persisted** on the repository row when present ([**WI-174**](WI-174-value-mapping-embedding-repository.md) schema). The sync routine consumes **the full list** of entries for that attribute. This story defines the **contract only**. Concrete value-source implementations (SQL distinct, JDBC, REST, etc.) are **not** part of this WI and are expected to be **provided externally** or added in later WIs.

**Duplicate `content`** in one batch: **dedupe** by **`content`** using **last wins** semantics before repository reconciliation, so only the final occurrence for a given **`content`** is applied.

## Problem Statement

[**WI-174**](WI-174-value-mapping-embedding-repository.md) and [**WI-177**](WI-177-vector-store-harness.md) do not stay aligned with **current column values** by themselves. This WI specifies **sync / hydration**: given **all values** for an **attribute URN**, bring **repository** and **vector store** in line with that list and the **active embedding model**.

## Goal

Implement the **orchestration routine** (port + implementation) that:

1. Accepts **attribute URN** and the **complete entry list** from the [**value source contract**](#value-source-contract).
2. Loads **existing rows** for that **`attribute_urn`** from the **vector repository**.
3. Reconciles **repository** + **vector store** with the value list and **current `ai_embedding_model` row** ([**WI-176**](WI-176-embedding-model-harness.md) selection -> **`ai_embedding_model.id`**).

[**ValueMappingService**](WI-180-value-mapping-service-orchestrator.md) ([**WI-180**](WI-180-value-mapping-service-orchestrator.md)) **calls** this routine; sync logic lives **here**, not duplicated in WI-180.

**Dependency order:** finalize [**WI-179**](WI-179-sync-vectors-hydration.md) **ports** before or with [**WI-180**](WI-180-value-mapping-service-orchestrator.md); **stub** sync behind the service only if interfaces slip.

---

## Orchestration routine (normative)

**Inputs:**

- **`attributeUrn`** - same encoding as [**WI-174**](WI-174-value-mapping-embedding-repository.md) **`attribute_urn`**.
- **`entries`** - full list from value source: each has **`content`** string **as supplied** + optional **metadata** for repository persistence.
- **Current model** - the **`ai_embedding_model`** row in use for this run; **`embedding_model_id`** on repository rows must **equal** this row's **`id`** when the fingerprint check passes.

**Content identity (normative):**

- **One repository row** per **(`attribute_urn`, `content`)** - [**WI-174**](WI-174-value-mapping-embedding-repository.md) unique constraint.
- Match **value source -> repository** on **same `attribute_urn`** + **`String.equals`** on **`content`** (no Mill-side normalization in v1). **Callers** keep value source and DB strings consistent.

**Loaded state:**

- **`repoRows`** - all rows for this **`attribute_urn`**.

### Phase A - Orphans (values removed from backend)

For each **repository** row whose **`content`** is **not** in the **set of `entries[].content`**:

1. **Delete** from **`EmbeddingStore`** by **`stable_id`** ([**WI-177**](WI-177-vector-store-harness.md) - delete-by-id exists on **`EmbeddingStore`**).
2. **Delete** the row from the **vector repository**.

### Phase B - Each entry

For each **entry**, find the row with **same `attribute_urn`** and **`content`** equal to **entry content as supplied**. Evaluate:

| Condition | Meaning |
|-----------|---------|
| **Present in repository** | Row exists. |
| **`embedding_model_id`** | Equals **`current` `ai_embedding_model.id`** for this run. |
| **Vector non-null** | **`embedding`** not **NULL**. |

**If all three pass:**

- **Always re-ingest** into **`EmbeddingStore`** (**add-or-replace** / upsert by **`stable_id`**) so runtime state cannot drift from repository - **safer than skip** if another writer touched the store.

**If `embedding_model_id` matches but vector is NULL:**

- **Re-embed** (treat as failing the "vector non-null" path).

**If any of the three fails** (missing row, wrong model id, or NULL vector):

1. **Embed** via [**WI-176**](WI-176-embedding-model-harness.md) for **`entry.content`** (and metadata if the embed API uses it).
2. **Insert or update** repository: **`embedding`**, **`embedding_model_id`** = current model **`id`**, **`content`**, **`attribute_urn`**, **`stable_id`**, optional metadata fields.
3. **Ingest** into **`EmbeddingStore`** (**add-or-replace** by **`stable_id`**).

**Partial failure (e.g. embed API error on one entry):** **log** and **continue** with remaining entries; **do not** abort the whole batch unless a follow-up policy is added later.

### Consistency aim

**Values** (source), **repository**, and **vector store** stay aligned: no **`stable_id`** left for **`content`** not in the current **`entries`** list; every entry has vectors under the **current** model in **repository** and **store**.

### Concurrency

Concurrent sync for the **same** **`attribute_urn`** is **unlikely** in expected deployments; **no** mutex / advisory lock required for v1. Revisit if multi-writer patterns appear.

---

## Non-goals (this WI)

- **User query resolution** (similarity thresholds, **top-k**, NL -> value) - **not** part of this story; **separate** WI or product track.
- Heavy **AttributeValueSource** implementations - plug into [**value source contract**](#value-source-contract) in later work.

## Acceptance Criteria

- Port(s) + implementation for **Phase A**, **Phase B**, **log-and-continue** on per-entry failure.
- **Integration tests** - in-memory **`EmbeddingStore`** + H2 repository - **no** mandatory OpenAI in default CI.

## Risks and Mitigations

- **String form drift** - value source vs **`content`** mismatch; fix **upstream** ([**Content identity**](#orchestration-routine-normative)).

## Deliverables

- This WI file.
- Code + tests per **Acceptance Criteria**.

## Closure

Update [`STORY.md`](STORY.md); [`MILESTONE.md`](../../MILESTONE.md).
