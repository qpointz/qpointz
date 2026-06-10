# WI-176 — Embedding model harness (registry + `embed()`)

**Story:** [`implement-value-mappings`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ai` |
| **Depends on** | [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md) — **`mill.ai.providers`** (credentials resolution). [**WI-174**](WI-174-value-mapping-embedding-repository.md) — **logical only** (vectors align with persisted `ai_embedding_model` in later sync WIs; **no** compile dependency). |
| **Story order** | Second implementation slice after **WI-175**; see [`STORY.md`](STORY.md). |

## Problem Statement

Value mapping needs a **runtime path** that turns **canonical text** into an **embedding vector**.
That path must use a **registry** of named embedding profiles, resolve **secrets** from **`mill.ai.providers`**
(**WI-175**), support **OpenAI via LangChain4j** first, and run tests **without** live APIs via a **stub**.

**Implementation stack:** the harness and its integration tests use **LangChain4j** only (`EmbeddingModel`, in-memory embedding store for IT). **mill-ai-v1** integration tests (e.g. Spring AI `EmbeddingModel`, `application-test-moneta-slim-it.yml`) are **reference only** for **configuration and flow** (model id, `${OPENAI_API_KEY}`, end-to-end embed into a small vector workflow) — **not** for copying Spring AI APIs into v3.

## Goal

Deliver the **embedding harness**:

- **API:** **`embed(content)`** → **`float[]`** (or project vector type) with dimension matching the
  active registry entry (see **dimension** on the profile below).
- **`mill.ai.embedding-model`** — map from **logical name** → configuration: **`provider`** (id
  referencing **`mill.ai.providers.<id>`** from **WI-175**) + **non-secret** params (remote model id,
  **dimension** where required, LangChain4j options). Dimension is **model-specific** and belongs in
  configuration (per profile / preset), not hard-coded in tests except when asserting against the
  **configured** value.
- **`mill.ai.value-mapping.embedding-model`** — string: **key** into the **`mill.ai.embedding-model`**
  map for value-mapping flows (extensible pattern for other features later).
- **Providers:** pluggable; **implement OpenAI** (LangChain4j) + **stub** (deterministic, no network) for
  fast tests. Other backends (Ollama, …) out of scope but port must not assume
  OpenAI-only types in the public contract.
- **Relationship to `spring.ai.*`:** Mill uses **`mill.ai.*`** for this harness; Spring AI vector beans
  **not** required.
- **Vector store alignment:** embedding **dimension** is defined here; operators must configure [**WI-177**](WI-177-vector-store-harness.md) (and any external index) so dimensions **match**. Mill does not silently correct a misconfigured deployment.

**Not in this WI:** persisting vectors (**WI-174** repository), **delta sync**, **vector index**
ingestion, **`AttributeValueSource`**. **Batch** optional; minimum scope is synchronous **`embed(String)`**
unless **`embedAll`** is trivial.

## Module boundaries

| Concern | Module |
|---------|--------|
| **Contracts** — embedding ports / provider SPI names TBD; **no** Spring on public APIs | **`ai/mill-ai-v3`** |
| **Properties**, **`@Configuration`**, LangChain4j **`EmbeddingModel`** beans, stub, facade, `AutoConfiguration.imports` | **`ai/mill-ai-v3-autoconfigure`** |

**Rule:** **`mill-ai-v3-autoconfigure`** depends on **`mill-ai-v3`** and **WI-175** provider config.

**Spring configuration metadata:** **`@ConfigurationProperties`** for **`mill.ai.embedding-model`** /
**`mill.ai.value-mapping`** (and related keys) **must be implemented in Java** so
**`spring-boot-configuration-metadata.json`** is generated — same rule as [**WI-175**](WI-175-mill-ai-v3-ai-configuration-foundation.md).

## References (in-repo patterns; v3 implementation stays LangChain4j)

| Use | Location |
|-----|----------|
| Env + embedding model name (v1 Spring AI stack — **reference for semantics only**) | [`ai/mill-ai-v1-core/src/testIT/resources/application-test-moneta-slim-it.yml`](../../../../ai/mill-ai-v1-core/src/testIT/resources/application-test-moneta-slim-it.yml) |
| End-to-end embedding usage in IT (v1 — **reference for flow only**) | [`DefaultValueRepositoryTestIT.java`](../../../../ai/mill-ai-v1-core/src/testIT/java/io/qpointz/mill/ai/nlsql/components/DefaultValueRepositoryTestIT.java) |
| Skip real API when `OPENAI_API_KEY` unset (v3) | [`LangChain4jAgentHelloWorldTestIT.kt`](../../../../ai/mill-ai-v3-test/src/testIT/kotlin/io/qpointz/mill/ai/test/LangChain4jAgentHelloWorldTestIT.kt) |

## Test Plan

- **Unit:** stub same input → same vector; facade resolves **`mill.ai.value-mapping.embedding-model`** →
  registry → provider credentials; dimension matches **configured** profile.
- **testIT (OpenAI, LangChain4j):** in **`ai/mill-ai-v3-test`** — integration test that uses a **real**
  OpenAI embedding model when **`OPENAI_API_KEY`** is set. Use **`Assumptions.assumeTrue`** (or equivalent)
  so the test is **skipped** when the variable is missing (local / CI without secrets). Exercise the
  harness against LangChain4j’s **in-memory embedding store** (ingest + query) so the scenario matches
  production semantics without Chroma or an external vector DB. Add **`mill-ai-v3-autoconfigure`** (and
  harness) on the test classpath as needed for Spring + beans.
- **Fast / no network:** stub-based tests remain for default **`./gradlew test`** and for contributors
  without API keys; they do **not** replace the OpenAI **testIT** narrative above.

## Acceptance Criteria

- **`mill.ai.embedding-model.<name>`** map with at least two names in docs; **`mill.ai.value-mapping.embedding-model`**
  references one of them.
- Resolution: registry **`provider`** id → **`mill.ai.providers.<id>`** (**WI-175**).
- **OpenAI + LangChain4j** works when configured (document env/properties); **dimension** is driven by
  embedding profile / model configuration, not ad hoc constants in tests.
- **testIT** in **`mill-ai-v3-test`** validates the real OpenAI path when **`OPENAI_API_KEY`** is present;
  same tests **skip** when it is absent.
- **Stub** for unit and no-network paths.
- **Java** `@ConfigurationProperties` for new **`mill.ai.*`** keys (**`spring-configuration-metadata.json`** via processor); **JavaDoc** on those classes per project policy; Kotlin elsewhere as needed with **KDoc**.
- **Logging** on every harness operation (no full vectors or secrets at default levels).

## Risks and Mitigations

- **Dimension mismatch** — validate at startup or first embed against **configured** dimension.
- **Secrets** — only in **`mill.ai.providers`** (**WI-175**).

## Deliverables

- This WI file.
- **`mill-ai-v3`** + **`mill-ai-v3-autoconfigure`** code per **Module boundaries** and Acceptance Criteria.
- OpenAI harness **`testIT`** in **`mill-ai-v3-test`** per **Test Plan**.
- Cross-link sync WIs to [**WI-174**](WI-174-value-mapping-embedding-repository.md) when wiring persist + harness.

**Follow-on in story (typical):** [**WI-177**](WI-177-vector-store-harness.md) — single vector store per instance; [**WI-179**](WI-179-sync-vectors-hydration.md) / [**WI-180**](WI-180-value-mapping-service-orchestrator.md); [**WI-178**](WI-178-value-mappings-stack-documentation.md) before story closure.

## Closure

Update [`STORY.md`](STORY.md); [`MILESTONE.md`](../../MILESTONE.md).
