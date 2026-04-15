# WI-174 — Value mapping embedding repository (golden source persistence)

**Story:** [`implement-value-mappings`](STORY.md)

| Field | Value |
|--------|--------|
| **WI status** | **Finalized** — ready for implementation (spec frozen) |
| **Last updated** | 2026-04-16 |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ai`, `persistence` |
| **Milestone** | Assign when the story is scheduled (see [`STORY.md`](STORY.md)) |
| **Story order** | Recommended sequence in [`STORY.md`](STORY.md) is **WI-175 → WI-176 → WI-177 → WI-174**; **this WI has no compile-time dependency** on [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md), [**WI-176**](WI-176-embedding-model-harness.md), or [**WI-177**](WI-177-vector-store-harness.md). |

## Summary

Add **Flyway schema** in **`mill-persistence`** for **`ai_embedding_model`** (shared embedding-spec
catalog) and **`ai_value_mapping`** (value rows with UUID **`stable_id`** as vector-store id). Implement
**pure ports** + domain types in the **`mill-ai-v3`** contract surface and **JPA adapter** in
**`mill-ai-v3-persistence`**, with **H2 `testIT`**, **KDoc**, and **logging on every repository
operation**. **No** sources, sync, embedding APIs, or vector-store code.

---

## Problem Statement

Value-mapping embeddings need a **persistent golden source** (H2 in dev, Postgres in prod) that
other components can read to **hydrate** a vector index. That persistence should model **attribute**
identity, **stable** document ids, **vectors**, and **embedding model parameters** without ad-hoc
duplication per row.

## Goal (this WI only)

Implement the **repository layer** — pure **contracts**, domain types, JPA entities in
**`mill-ai-v3-persistence`**, and **`testIT` on H2 only** — for:

1. **`ai_embedding_model`** — one row per distinct embedding **configuration** (fingerprint /
   canonical spec). **Uniqueness:** the same logical embedding configuration must yield the **same
   row id** so **multiple attributes** (and **future non–value-mapping embedding features**) can share
   one model row. This table is a **reusable embedding-spec catalog**, not only for value mapping.
2. **`ai_value_mapping`** — value rows: **attribute identity**, **`stable_id`** (primary key — see
   below), **`content`**, **`content_hash`**, **`embedding`** (nullable — see semantics),
   **`embedding_model_id`** FK → **`ai_embedding_model`**, timestamps.

Expose port(s) implemented by **`JpaEmbeddingRepository`** (adapter naming illustrative). **No**
sync, sources, or vector-store calls.

**Design references:** contract purity, centralized schema, and adapter placement follow
[`docs/design/persistence/persistence-overview.md`](../../../design/persistence/persistence-overview.md)
and [`docs/design/persistence/persistence-bootstrap.md`](../../../design/persistence/persistence-bootstrap.md).

## Explicitly out of scope (future WIs / story follow-ups)

- **`AttributeValueSource`**, **delta / sync orchestration**, **embedding API calls**, **vector index**
  hydration.
- Semantics for **NULL `embedding`** beyond persistence (treat as “needs recompute / reingest” in a
  **follow-up WI**).
- **Skymill / Chroma IT rewrites**, NL2SQL, facets, metadata REST (**WI-173**), AI capabilities.
- **Postgres-specific `testIT`** in this WI (H2 only).

## Schema ownership and migrations

- **All Flyway SQL** for these tables lives under **`persistence/mill-persistence/`**:
  **`src/main/resources/db/migration/V6__ai_embedding_model_and_value_mapping.sql`**
  (next version after **`V5__typed_entity_urns.sql`** in that module at time of writing).
- Do **not** add parallel migration trees under `ai/mill-ai-v3-persistence/` for this schema.

## Physical model (decisions — frozen)

### `ai_embedding_model`

- **Primary key:** surrogate (e.g. `BIGINT` identity or UUID — choose one; document in migration).
- **One row per unique embedding configuration** (fingerprint / normalized params). If two attributes
  use the **same** configuration, they reference the **same** PK.
- **Reuse:** shared **embedding-spec** catalog for value mapping **and** future embedding-backed
  components; FKs from other feature tables may be added in later WIs.
- **Columns (illustrative):** **unique** configuration fingerprint, provider, model id, dimension,
  params blob/JSON, optional label, audit timestamps — finalize names and types in **`V6__…`**.

### `ai_value_mapping`

- **Primary key:** **`stable_id`** — **`UUID`**, **globally unique**, **application-generated** on insert.
  Same id is used as the **vector-store document id** (and for deletes). Not derived from text alone.
- **Attribute identity:** **`attribute_urn`** (or equivalent column) — **typed attribute URN** (same family as platform metadata / [`typed entity URNs`](../../../design/metadata/metadata-urn-platform.md)); identifies **which column** this value belongs to.
- **`content`:** **string** — canonical text for this value; forms a logical key with the attribute URN (see **Uniqueness** below).
- **`embedding`:** **nullable**. **`NULL`** = no vector stored yet; sync ([**WI-179**](WI-179-sync-vectors-hydration.md)) treats as **re-embed** when fingerprint matches.
- **FK** `embedding_model_id` → `ai_embedding_model(id)` with **`ON DELETE RESTRICT`**. **“Same embedding model”** for sync comparisons means **`embedding_model_id`** equals the **`ai_embedding_model.id`** row selected for the **current** embedding configuration (not a separate hash column unless added in migration).
- **Uniqueness:** **at most one row per (`attribute_urn`, `content`)** — enforce with a **unique constraint** in **`V6__…`**. Optional **metadata** columns (JSON / text) for extra segments from the value source ([**WI-179**](WI-179-sync-vectors-hydration.md) contract).

### Referential integrity

- **`ON DELETE RESTRICT`** from **`ai_value_mapping`** (and future referencing tables) to
  **`ai_embedding_model`**: do not delete a model row while referenced. Model rows are **fingerprint /
  evidence** of the spec used; delete only after dependents are migrated or removed deliberately.

### Indexes

Implement in **`V6__…sql`** (minimum intent):

- **`ai_value_mapping`:** **`stable_id`** is PK; **unique (`attribute_urn`, `content`)**; index(es) for **list/filter by attribute**; index on **`embedding_model_id`** if queries need it.
- **`ai_embedding_model`:** **unique** on configuration **fingerprint**; index for fingerprint lookup.

### Vector column strategy

- Store vectors in a form **H2-compatible** for `testIT` (e.g. binary / JSON / float array — document
  in migration comments). Postgres **`pgvector`** alignment can be a **follow-up** hardening WI; this
  WI proves behavior on **H2** only.

## Domain / ports (contracts — no JPA)

- Port interfaces and **pure domain types** in the **contract module** per dependency rules (typically
  **`mill-ai-v3`** — confirm against Gradle graph). **No** JPA annotations on contracts.
- **`EmbeddingRepository`** (or split ports): CRUD for model rows and value rows; minimal surface;
  **structured logging** on **every** operation.

## Adapter (`mill-ai-v3-persistence`)

- JPA entities, Spring Data repositories, adapter implementing the port(s): **`ai/mill-ai-v3-persistence`**
  only.
- Map entities ↔ domain types in the adapter; return **only** domain types from public port methods.

## Documentation and logging

- **KDoc:** follow repository **standard rules** (production code documented per project policy).
- **Logging:** **SLF4J** (or project-standard logger); log **every** repository operation (e.g. **debug**
  reads, **info** writes/deletes). Include **`stable_id`**, attribute keys where useful; **never** log
  full embedding arrays at default levels or log secrets.

## Implementation order (recommended)

1. Add **`V6__ai_embedding_model_and_value_mapping.sql`** in **`mill-persistence`** (tables, FK,
   indexes, comments).
2. Define **domain types + port(s)** in the contract module.
3. Add **JPA entities**, Spring Data repos, **adapter**, KDoc, logging.
4. Add **`testIT`** in **`mill-ai-v3-persistence`**: round-trip, FK RESTRICT, nullable embedding,
   list-by-attribute (as implemented).

## Test Plan

- **`testIT` on H2 only** (existing `mill-ai-v3-persistence` harness).
- **Unit tests:** entity ↔ domain mapping if non-trivial.

## Acceptance Criteria

- **`V6__ai_embedding_model_and_value_mapping.sql`** exists under
  **`persistence/mill-persistence/src/main/resources/db/migration/`** and applies cleanly after **`V5`**.
- **`ai_embedding_model`:** unique row per configuration; **same config ⇒ same PK** for reuse.
- **`ai_value_mapping`:** **`stable_id` (UUID)** is **PK**; **`attribute_urn`** + **`content`** (string); **unique (`attribute_urn`, `content`)**; **`embedding`**
  nullable; **FK RESTRICT** to model table.
- Port(s) + adapter in **`mill-ai-v3-persistence`**; **H2 `testIT`**; **logging** on all repository
  operations; **KDoc** on public API.
- **No** value sources, sync, vector ports, or non-H2 integration tests.

## Dependencies

- **`mill-ai-v3-persistence`**, **`mill-persistence`** (Flyway).
- Design: [`persistence-overview.md`](../../../design/persistence/persistence-overview.md),
  [`persistence-bootstrap.md`](../../../design/persistence/persistence-bootstrap.md).

**Same story:** [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md) (providers), [**WI-176**](WI-176-embedding-model-harness.md) (embedding harness), and [**WI-177**](WI-177-vector-store-harness.md) (vector store) may be implemented before or in parallel; **sync** ([**WI-179**](WI-179-sync-vectors-hydration.md)) and **ValueMappingService** ([**WI-180**](WI-180-value-mapping-service-orchestrator.md)) compose **WI-174** + **WI-176** + **WI-177** (see [`STORY.md`](STORY.md)). **Docs sweep:** [**WI-178**](WI-178-value-mappings-stack-documentation.md).

**Content key for sync:** **`ai_value_mapping.content`** (string) plus **`attribute_urn`** — compared **as supplied** from the value source for **`content`** (exact string equality) — see [**WI-179**](WI-179-sync-vectors-hydration.md) **Content identity**.

**Follow-on:** value sources, sync, NULL-embedding behavior, vector hydration — **separate WIs** in
[`STORY.md`](STORY.md).

## Risks and Mitigations

- **Fingerprint collision** — define fingerprint computation or column semantics clearly in migration
  comments; review uniqueness with platform owners if multiple domains share the table.

## Deliverables

- This WI (`docs/workitems/in-progress/implement-value-mappings/WI-174-value-mapping-embedding-repository.md`).
- **`V6__…sql`**, ports, adapter, KDoc, logging, **H2** tests — **Acceptance Criteria** satisfied.

## Closure (when implementation is done)

1. Mark **`[x]`** for **WI-174** in [`STORY.md`](STORY.md) (and move story folder **`planned/` →
   `in-progress/`** on first completed WI per [`RULES.md`](../../RULES.md), if not already moved).
2. Update [`MILESTONE.md`](../../MILESTONE.md) if this WI is tied to a named milestone.
3. **Commit** per project commit rules (one logical commit for the completed WI when appropriate).
