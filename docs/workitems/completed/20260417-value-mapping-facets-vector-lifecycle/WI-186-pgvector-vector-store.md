# WI-186 — pgvector LangChain4j `EmbeddingStore` backend

| Field | Value |
|--------|--------|
| **Story** | [`value-mapping-facets-vector-lifecycle`](STORY.md) |
| **Status** | `done` (implementation + docs; manually verified against PostgreSQL + `vector`) |
| **Type** | `feature` |
| **Area** | `ai`, `mill-ai-v3-autoconfigure` |
| **Depends on** | [**WI-177**](../../../design/ai/mill-ai-configuration.md) vector-store prefix; [**WI-176**](../../completed/20260416-implement-value-mappings/WI-176-embedding-model-harness.md) embedding profiles (`EmbeddingHarness.dimension`). |

## Problem

Operators on a **single PostgreSQL** deployment may want LangChain4j **similarity search** (`EmbeddingStore`) **without** a separate Chroma service. [**WI-177**](../../../design/ai/mill-ai-configuration.md) defined `in-memory` and **Chroma**; **pgvector** on the app’s Postgres is a natural third backend.

## Goal

1. Add **`mill.ai.vector-store.backend=pgvector`** wiring **`dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore`** to the primary Spring **`DataSource`** when it is **PostgreSQL** and the **`vector`** extension is installed.
2. **Dual storage:** LangChain4j uses a **dedicated table** (default **`mill_langchain_embedding_store`**), **not** `ai_value_mapping.embedding` (`BYTEA`). Golden rows stay on the JPA path; ANN search uses the duplicate vectors in the LC4j table (acceptable product trade-off).
3. **Dimension** follows the active **`EmbeddingHarness`** (same as **`mill.ai.embedding-model`** for the profile selected by **`mill.ai.value-mapping.embedding-model`**) — no separate `dimension` property on the vector store.
4. **Tiers:** **A** — H2 / local dev: use **`in-memory`** or Chroma; no pgvector. **B** — Postgres **without** `vector` extension: use **`in-memory`** or Chroma; autoconfig **must not** half-enable pgvector. **C** — Postgres **with** pgvector: optional **`backend=pgvector`**.

## Non-goals

- Universal Flyway that runs **`CREATE EXTENSION vector`** on every deployment (regulated DBs may forbid it). Operators enable the extension out of band or via a **Postgres-only** migration profile (future).
- **Metadata facet types** for PII / retention on vectors ([**WI-181**](WI-181-value-mapping-facet-types.md) follow-ups).
- Replacing **`ai_value_mapping`** storage for embeddings.

## Acceptance criteria

- **`VectorStoreConfigurationProperties`**: `Backend.PGVECTOR` and nested **`pgvector`** (`table`, `createTable`, optional index fields if exposed).
- **`VectorStoreAutoConfiguration`**: builds **`PgVectorEmbeddingStore`** via **`datasourceBuilder()`**, validates **PostgreSQL** + **`vector`** extension, uses **`EmbeddingHarness.dimension`**.
- Clear **fail-fast** messages when `backend=pgvector` but **no `DataSource`**, **not PostgreSQL**, or **extension missing**.
- Tests: properties binding; context failure for **H2** `DataSource` with **`pgvector`** (or missing `DataSource` / harness as applicable).
- Design doc [`mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md), [`CONFIGURATION_INVENTORY.md`](../../../design/platform/CONFIGURATION_INVENTORY.md), and public reference updated.

## Verification

- **Manual:** **`mill.ai.vector-store.backend=pgvector`** exercised with a PostgreSQL **`DataSource`**, **`vector`** extension present, and Mill Service profile **`pgvector`** / **`ai-pgvector`** (see **`apps/mill-service/src/main/resources/application.yml`**). Behaviour confirmed good for operator use.

## Deliverables

- `mill-ai-v3-autoconfigure`: dependency **`langchain4j-pgvector`** (aligned with **`langchain4j-chroma`** beta line), properties, autoconfiguration, tests.
- **`apps/mill-service`**: Spring profiles **`pgvector`** and group **`ai-pgvector`** (parallel to **`chromadb`** / **`ai-chromadb`**).
- Design + public docs: **`docs/design/ai/mill-ai-configuration.md`**, **`docs/public/src/reference/mill-ai-configuration.md`**.
- This work item file; **`STORY.md`** tracking row.
