# WI-180 - Value mapping service (implementation)

**Story:** [`implement-value-mappings`](STORY.md) (follow-on)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `feature` / `test` |
| **Area** | `ai` |
| **Depends on** | [**WI-174**](WI-174-value-mapping-embedding-repository.md) - **`EmbeddingRepository`**. [**WI-176**](WI-176-embedding-model-harness.md) - **`embed()`**. [**WI-177**](WI-177-vector-store-harness.md) - **`EmbeddingStore`**. [**WI-179**](WI-179-sync-vectors-hydration.md) - **column sync routine** (value list + repository + store - **normative spec** in that WI; **do not reimplement**). [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md) - implicit via harnesses. |
| **Story order** | After [**WI-179**](WI-179-sync-vectors-hydration.md) is at least **specified** (interfaces/contracts); implementation may proceed in parallel once sync API is agreed. |

## Clarification (vs WI-179)

| Concern | [**WI-179**](WI-179-sync-vectors-hydration.md) | **WI-180 (this)** |
|--------|-----------------------------------------------|-------------------|
| **Role** | **Reconciliation** - [normative routine](WI-179-sync-vectors-hydration.md#orchestration-routine-normative). | **`ValueMappingService`** - orchestrates **sync** via [**WI-179**](WI-179-sync-vectors-hydration.md). **Resolve / similarity / thresholds** - **not** in [`implement-value-mappings`](STORY.md); **separate** backlog/WI. |
| **Calls** | Invoked by **WI-180**, jobs, pipelines. | Invokes [**WI-179**](WI-179-sync-vectors-hydration.md) only for **hydration**; **no** duplicate sync logic. |

## Problem Statement

Callers need a **single service** that **syncs** an attribute: given a [**value source contract**](WI-179-sync-vectors-hydration.md#value-source-contract), runs [**WI-179**](WI-179-sync-vectors-hydration.md) so **value entries**, **vector repository** (**`attribute_urn`** + **`content`**), and **`EmbeddingStore`** stay aligned.

**Out of scope for this story:** **resolve** / semantic search (**top-k**, thresholds, empty-store fallback) - **tracked separately**; this WI focuses on **vector store hydration** via sync.

**Sync logic** is **not** reimplemented here - it lives in [**WI-179**](WI-179-sync-vectors-hydration.md).

## Goal

Implement **`ValueMappingService`** (interface in **`mill-ai-v3`**, **no** Spring imports on the public port) and a **Spring adapter** in **`mill-ai-v3-autoconfigure`**. **REST / HTTP exposure is out of scope for this WI.**

**Minimum surface (this story):**

- **`syncAttribute(...)`** (name TBD) - obtains **entries** from the [**value source**](WI-179-sync-vectors-hydration.md#value-source-contract), calls [**WI-179**](WI-179-sync-vectors-hydration.md) for that **`attributeUrn`** + current **`ai_embedding_model`**.
- **`resolve...`** - **deferred** (separate WI); not required to close **[`implement-value-mappings`](STORY.md)**.
- **Logging** on service operations (no full vectors or secrets at default levels); **KDoc** / **JavaDoc** per project policy.

**Relationship to exploratory work:** [**WI-171**](../metadata-value-mapping/WI-171-chroma-skymill-vector-exploration.md) informs scenarios; **this WI** is the **supported** production path.

**Spring configuration metadata:** new **`mill.ai.*`** `@ConfigurationProperties` for this feature (if any) **must be Java** - see [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md).

## Module boundaries

| Concern | Module |
|---------|--------|
| **Port** - `ValueMappingService` (or split ports), pure types | **`ai/mill-ai-v3`** |
| **Implementation** - orchestration beans, optional `@Configuration` | **`ai/mill-ai-v3-autoconfigure`** |
| **REST / HTTP** | **Out of scope** for this WI |

## Test Plan

- **Unit:** mocked **repository**, **embedding harness**, **vector store**, **sync** port.
- **testIT (default CI):** **`mill-ai-v3-data`** or equivalent internal test slice - in-memory **`EmbeddingStore`** + stub/deterministic embed; **no** mandatory **OpenAI** or Testcontainers.
- **testIT (Chroma, opt-in):** when an external implementation of the [**value source contract**](WI-179-sync-vectors-hydration.md#value-source-contract) is available, reimplement **[`ChromaSkymillDistinctVectorIT`](../../../../ai/mill-ai-v3-data/src/testIT/kotlin/io/qpointz/mill/ai/data/chroma/it/ChromaSkymillDistinctVectorIT.kt)** so **distinct values** come from that contract and **`ValueMappingService`** runs **WI-179 sync** into a **Chroma** `EmbeddingStore` (not ad-hoc `store.add` in the test). Gate with **`MILL_CHROMA_IT_ENABLED`** (and optional **`MILL_CHROMA_BASE_URL`**); use a **deterministic** embedding model in-test so CI does not require network keys. This is the **supported** successor to exploratory [**WI-171**](../metadata-value-mapping/WI-171-chroma-skymill-vector-exploration.md).

## Acceptance Criteria

- **`ValueMappingService`** (or agreed port name) is **consumable** from at least one **integration path** (e.g. capability dependency, or internal call from a test slice).
- Uses [**WI-174**](WI-174-value-mapping-embedding-repository.md), [**WI-176**](WI-176-embedding-model-harness.md), [**WI-177**](WI-177-vector-store-harness.md), and [**WI-179**](WI-179-sync-vectors-hydration.md) **without duplicating** sync algorithms.
- **Observability:** structured logging; no secrets or full vectors at default levels.
- Any opt-in Chroma proof follows the value-source contract path rather than a manual parallel ingest path.

## Risks and Mitigations

- **Circular dependency** service <-> sync - define **ports** and dependency direction in [**WI-179**](WI-179-sync-vectors-hydration.md) before merge.

## Deliverables

- This WI file.
- Code + tests per **Acceptance Criteria**.

## Closure

Update [`STORY.md`](STORY.md); [`MILESTONE.md`](../../MILESTONE.md); [`BACKLOG.md`](../../BACKLOG.md) row **A-88** when done.
