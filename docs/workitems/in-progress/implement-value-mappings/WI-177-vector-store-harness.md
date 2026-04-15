# WI-177 — Vector store harness (single instance + pluggable backends)

**Story:** [`implement-value-mappings`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ai` |
| **Depends on** | [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md) — **`mill.ai.providers`**. [**WI-176**](WI-176-embedding-model-harness.md) — **logical** only for docs/dimension alignment; optional E2E tests. [**WI-174**](WI-174-value-mapping-embedding-repository.md) — **not** a dependency of this WI; the DB is **golden source** persistence; **this** harness is the **search** store (see **Relationship** below). |
| **Story order** | After [**WI-176**](WI-176-embedding-model-harness.md); see [`STORY.md`](STORY.md). |

## Relationship: persistence vs search

**One concern, two roles:** [**WI-174**](WI-174-value-mapping-embedding-repository.md) **persists and manages** vectors as the **golden source** in the application database. The **vector store harness** (this WI) exposes LangChain4j-backed **similarity search** (add / remove / query) against a **runtime** store (**in-memory**, and later **pgvector**, **Chroma**, …). Keeping search in sync with the DB is **not** this WI — it belongs to [**WI-179**](WI-179-sync-vectors-hydration.md) (sync / hydration). [**WI-180**](WI-180-value-mapping-service-orchestrator.md) implements **ValueMappingService** across repository, embedding model, vector store, and [**WI-179**](WI-179-sync-vectors-hydration.md) sync.

## Problem Statement

Beyond [**WI-176**](WI-176-embedding-model-harness.md), Mill needs a **single, Spring-managed vector store** for similarity search — **one active backend per running instance** (contrast: [**WI-176**](WI-176-embedding-model-harness.md) allows **multiple named embedding profiles**).

**Implementation stack:** **LangChain4j** `EmbeddingStore` (or the project’s stable wrapper around it) as the **public contract** in **`mill-ai-v3`** — **backend-agnostic** at the port boundary. **Spring Boot:** exactly **one** backend bean active at a time via **`@ConditionalOnProperty`** / mutually exclusive autoconfigure (same pattern as other single-backend Mill features). **No** Spring AI `VectorStore` requirement.

## Goal (MVP)

- **Semantics:** **one** `EmbeddingStore` (or agreed port) bean **per Spring context** — singleton scope via **`mill-ai-v3-autoconfigure`**.
- **Configuration:** **`mill.ai.vector-store.*`** — selector for implementation (**`in-memory`** for MVP) and **hooks for future** backends. Design for **extensibility**: new implementations register via SPI / additional autoconfigure without forking the core property model.
- **Secrets:** [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md) **`mill.ai.providers`** when a backend needs them; **no** secrets in non-provider leaves.
- **Deferred in MVP:** concrete **pgvector** and **Chroma** implementations — **out of scope for initial delivery**; document how they will attach to the same property tree when added. **Pgvector** is expected to use the **same datasource** as Mill when implemented (align with platform DB story).
- **Dimension / embedding alignment:** **Embedding dimension** is defined by [**WI-176**](WI-176-embedding-model-harness.md) **`mill.ai.embedding-model`** configuration. Operators must configure the **vector store** implementation so dimensions **match**; **Mill does not silently fix** misconfigured deployments.

**Not in this WI:** sync from DB → store ([**WI-179**](WI-179-sync-vectors-hydration.md)), orchestration ([**WI-180**](WI-180-value-mapping-service-orchestrator.md)), **value sources**, metadata REST.

## Module boundaries

| Concern | Module |
|---------|--------|
| **Contracts** — `EmbeddingStore` (LangChain4j) or thin facade; **no** Spring on public APIs | **`ai/mill-ai-v3`** |
| **Properties**, **`@Configuration`**, conditional beans, `AutoConfiguration.imports` | **`ai/mill-ai-v3-autoconfigure`** |

**Rule:** **`mill-ai-v3-autoconfigure`** depends on **`mill-ai-v3`** and [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md).

**Spring configuration metadata:** **`@ConfigurationProperties`** for **`mill.ai.vector-store.*`**
**must be implemented in Java** so **`spring-boot-configuration-metadata.json`** is generated — same rule as [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md).

## Test Plan

- **Default CI / local:** **`in-memory`** backend only; **no Testcontainers**, no mandatory network.
- **Integration tests:** live in **`ai/mill-ai-v3-data`** (same module as other AI–data integration surfaces; aligns with [`v3-mill-ai-v3-data-boundary.md`](../../../design/agentic/v3-mill-ai-v3-data-boundary.md)).
- **Optional E2E** with [**WI-176**](WI-176-embedding-model-harness.md): embed + add + search — **skip** when prerequisites missing (`assumeTrue` pattern).

## Acceptance Criteria

- **`mill.ai.vector-store.*`** documented; **exactly one** backend active (**Spring conditional**).
- **In-memory** implementation ships; **pgvector** / **Chroma** listed as **future** extensions with config extension points described.
- LangChain4j **`EmbeddingStore`** (or documented facade) as the stable surface from **`mill-ai-v3`**.
- **Java** `@ConfigurationProperties` for **`mill.ai.vector-store`** (**`spring-configuration-metadata.json`**); **JavaDoc** on those classes per project policy.
- **Logging** on store operations (no full vectors or secrets at default levels).

## Risks and Mitigations

- **Misaligned dimensions** — document operator responsibility; optional **startup validation** when both embedding and store configs are present (future tightening).

## Deliverables

- This WI file.
- Code per **Module boundaries** and **Acceptance Criteria**.

## Closure

Update [`STORY.md`](STORY.md); [`MILESTONE.md`](../../MILESTONE.md); [`BACKLOG.md`](../../BACKLOG.md) row.
