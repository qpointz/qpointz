# WI-175 — Mill AI v3 — AI configuration foundation (`mill.ai.providers`)

**Story:** [`implement-value-mappings`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ai` |
| **Depends on** | — (first WI in this story; see [`STORY.md`](STORY.md) **Execution order**) |
| **Enables** | [**WI-176**](WI-176-embedding-model-harness.md), [**WI-177**](WI-177-vector-store-harness.md) — `mill.ai.providers` resolution for embedding and vector store harnesses |
| **Parallel** | [**WI-174**](WI-174-value-mapping-embedding-repository.md) — repository persistence; **no** code dependency between WI-175 and WI-174 |

## Problem Statement

Mill AI needs a **clear, extensible configuration namespace** under **`mill.ai.*`** for all **AI
functionality** (providers, chat models, embedding registries, vector stores, and future surfaces).
Without a **foundation** slice, later WIs duplicate ad-hoc property trees and tests.

## Goal (this WI only)

Establish the **pattern** for **`mill.ai.*`**: `@ConfigurationProperties`, autoconfigure registration,
and **binding tests**.

**Spring configuration metadata:** any new **`@ConfigurationProperties`** types for **`mill.ai.*`**
**must be implemented in Java** in **`mill-ai-v3-autoconfigure`** so **`spring-boot-configuration-processor`**
generates **`META-INF/spring-configuration-metadata.json`** (IDE support, validation). Kotlin property
classes do **not** receive that metadata unless every key is duplicated in hand-written
**`META-INF/additional-spring-configuration-metadata.json`** per **`CLAUDE.md`** — **avoid** for these
bindings; use **Java** for provider and related maps.

**Deliver in this WI:**

- **`mill.ai.providers`** — map (or equivalent) **by provider id** (e.g. `openai`, future others). Each
  entry holds **credentials and shared HTTP settings** for that provider: `api-key`, `base-url`, and
  other **provider-wide** fields as needed.
- **Optional** small **resolver** port (contract in **`mill-ai-v3`**, implementation in
  **`mill-ai-v3-autoconfigure`**) so features can obtain provider-scoped config by id without duplicating
  property access logic.

**Extensibility (design intent, not all implemented here):** the same **`mill.ai.*`** prefix will grow
with additional subtrees in later WIs — e.g. **`mill.ai.embedding-model`** ([**WI-176**](WI-176-embedding-model-harness.md)), chat-related
keys (existing **`mill.ai.model`** may align over time), **vector store** ([**WI-177**](WI-177-vector-store-harness.md) — `mill.ai.vector-store.*`).
This WI does **not** implement embedding registry, vector store harness, or chat refactors.

## Explicitly out of scope

- **`mill.ai.embedding-model`** registry, **`mill.ai.value-mapping.embedding-model`**, **`embed()`**
  harness — [**WI-176**](WI-176-embedding-model-harness.md).
- **Vector store** harness (single instance, pluggable backends) — [**WI-177**](WI-177-vector-store-harness.md).
- **Migrating** existing **`mill.ai.model`** (chat) onto **`mill.ai.providers`** — optional follow-up;
  not required to close this WI.

## Module boundaries

| Concern | Module |
|---------|--------|
| **Contracts** — provider config access / resolver ports if any; **no** Spring imports on public contracts | **`ai/mill-ai-v3`** |
| **`@ConfigurationProperties`** (Java — see **Spring configuration metadata** above), **`@Configuration`**, beans, `AutoConfiguration.imports` | **`ai/mill-ai-v3-autoconfigure`** |

**Rule:** **`mill-ai-v3-autoconfigure`** depends on **`mill-ai-v3`**; not the reverse.

**Design references:** [`docs/design/persistence/persistence-overview.md`](../../../design/persistence/persistence-overview.md) (platform config principles; schema for DB is separate).

## Test Plan

- **Unit / testIT:** properties bind under `mill.ai.providers.*`; invalid YAML fails predictably if
  applicable.
- **No** LangChain4j embedding beans required for this WI (those land in **WI-176**).

## Acceptance Criteria

- **`mill.ai.providers.<providerId>.*`** documented with example YAML (env placeholders for secrets).
- **`@ConfigurationProperties`** for **`mill.ai.providers`** implemented in **Java**; autoconfigure in **`mill-ai-v3-autoconfigure`**; contracts only in **`mill-ai-v3`**.
- **`META-INF/spring-configuration-metadata.json`** produced via **`spring-boot-configuration-processor`** (Java properties classes).
- Tests prove configuration loads; logging on sensitive paths avoids printing secrets.

## Risks and Mitigations

- **Overlap with `mill.ai.model`** — document relationship; avoid two OpenAI keys long-term (follow-up).

## Deliverables

- This WI file.
- Code + tests per Acceptance Criteria.

## Closure

Update [`STORY.md`](STORY.md); [`MILESTONE.md`](../../MILESTONE.md) when scheduled.

**Next in story (typical):** [**WI-176**](WI-176-embedding-model-harness.md) (embedding registry + `embed()`), then [**WI-177**](WI-177-vector-store-harness.md) (single vector store per instance).
