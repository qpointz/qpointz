# WI-284 — `mill.ai` property bindings and Java metadata

**Story:** [`ai-configuration-restructure`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ai` |
| **Depends on** | — (first WI in story) |
| **Enables** | [**WI-285**](WI-285-mill-ai-configuration-autoconfigure-wiring.md) |

## Problem

Legacy **`mill.ai.*`** keys duplicate provider credentials (`mill.ai.model` vs `mill.ai.providers`),
scatter embedding and value-mapping settings, and use a singleton `mill.ai.vector-store`. The new
layered model needs typed **`@ConfigurationProperties`** with generated metadata.

## Goal

Introduce Java property classes (processor metadata) for the target tree; remove bindings for
deleted legacy prefixes in this WI or mark classes `@Deprecated` only if needed until WI-285 lands.

## Deliver

### Extend [`AiConfigurationProperties`](../../../../ai/mill-ai-v3-autoconfigure/src/main/java/io/qpointz/mill/ai/autoconfigure/config/AiConfigurationProperties.java)

- `models.chat` / `models.embedding` maps (`ChatModelProfile`, move/rename `EmbeddingModelProfile`).
- Remove top-level `embeddingModel` map.
- `AiProviderEntry.type` (default `openai`).

### New Java properties

| Class | Prefix |
|-------|--------|
| `VectorStoresConfigurationProperties` | `mill.ai.vector-stores` |
| `DataEmbeddingConfigurationProperties` | `mill.ai.data.embedding` |

**`DataEmbeddingConfigurationProperties` profile:**

- `model`, `vectorStore`, `maxContentLength`, `refresh` (nested, same semantics as old value-mapping refresh).
- `List<EmbeddingSource>` with discriminated `type`; v1 concrete type `MetadataFacetsEmbeddingSource` (**`type` only**, no facet URNs in YAML).

**Shared nested types:** extract `VectorStoreBackend`, `Chroma`, `PgVector` from current
[`VectorStoreConfigurationProperties`](../../../../ai/mill-ai-v3-autoconfigure/src/main/java/io/qpointz/mill/ai/autoconfigure/config/VectorStoreConfigurationProperties.java)
for reuse.

### Kotlin chat properties

Extend [`AiV3ChatProperties`](../../../../ai/mill-ai-v3-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/AiV3ChatProperties.kt):

- `model: String = "default"`.
- `valueMapping.embedding: String = "default"`.
- `schemaSearch.embedding` (optional, reserved).
- Update [`additional-spring-configuration-metadata.json`](../../../../ai/mill-ai-v3-autoconfigure/src/main/resources/META-INF/additional-spring-configuration-metadata.json) if present.

### Delete / replace

- Delete [`AiModelProperties.kt`](../../../../ai/mill-ai-v3-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/AiModelProperties.kt).
- Replace [`ValueMappingConfigurationProperties`](../../../../ai/mill-ai-v3-autoconfigure/src/main/java/io/qpointz/mill/ai/autoconfigure/config/ValueMappingConfigurationProperties.java) (remove in this WI or WI-287 once nothing references it).

## Test plan

- Binding tests: minimal GCP-shaped YAML loads `data.embedding.default` with `sources: [{ type: metadata-facets }]`.
- `providers.openai.type`, `models.chat.default`, `vector-stores.pg` bind when present.
- Verify `spring-configuration-metadata.json` includes new keys after compile.

## Acceptance

- [ ] Java `@ConfigurationProperties` for `mill.ai.data.embedding` and `mill.ai.vector-stores` with generated metadata.
- [ ] `models.*` on `AiConfigurationProperties`; `providers.<id>.type`.
- [ ] `AiV3ChatProperties` exposes `chat.value-mapping.embedding`.
- [ ] Binding unit tests pass in `mill-ai-v3-autoconfigure`.
