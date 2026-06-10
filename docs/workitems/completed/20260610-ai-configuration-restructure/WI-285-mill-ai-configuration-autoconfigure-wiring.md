# WI-285 — Autoconfigure resolvers and runtime wiring

**Story:** [`ai-configuration-restructure`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` |
| **Area** | `ai` |
| **Depends on** | [**WI-284**](WI-284-mill-ai-configuration-property-bindings.md) |
| **Enables** | [**WI-286**](WI-286-mill-ai-configuration-yaml-migration.md), [**WI-287**](WI-287-mill-ai-configuration-tests.md) |

## Goal

Wire runtime beans from the new configuration tree: no duplicate API keys on chat model; embedding
harness and vector store resolved per **`data.embedding`** profile; refresh reads profile ops;
sources dispatch by `type`.

## Deliver

### Resolvers / factories (`mill-ai-v3-autoconfigure`)

| Component | Role |
|-----------|------|
| `PropertiesBackedModelResolver` | `models.chat` / `models.embedding` + `providers` |
| `VectorStoreConfigMerger` | `vector-store.backend` ref vs inline enum + `vector-stores` merge |
| `VectorStoreFactory` | effective config + dimension → `EmbeddingStore` |
| `DataEmbeddingProfileResolver` | profile id → model + store + refresh + sources |
| `EmbeddingSourceTypeRouter` | `metadata-facets` → existing `ValueMappingFacetAssembly` path |

### Autoconfigure updates

- [`AiV3AutoConfiguration`](../../../../ai/mill-ai-v3-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/AiV3AutoConfiguration.kt): `StreamingChatModel` from `chat.model` + `models.chat` + `providers` (remove `AiModelProperties`).
- [`EmbeddingAutoConfiguration`](../../../../ai/mill-ai-v3-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/embedding/EmbeddingAutoConfiguration.kt): harness from active profile's `model` ref (via `chat.value-mapping.embedding` or default profile `default`).
- [`VectorStoreAutoConfiguration`](../../../../ai/mill-ai-v3-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/vectorstore/VectorStoreAutoConfiguration.kt): factory; `@Primary EmbeddingStore` from active profile for existing `@ConditionalOnBean` inject sites.
- Value-mapping refresh: [`ValueMappingRefreshConfigurationBridge`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/valuemap/refresh/ValueMappingRefreshConfigurationBridge.kt) / scheduler read **`data.embedding.<profile>.refresh`** and **`max-content-length`**; update `@Scheduled` property placeholder off `mill.ai.value-mapping.refresh.*`.
- [`ValueMappingRefreshOrchestrator`](../../../../ai/mill-ai-v3-data/src/main/kotlin/io/qpointz/mill/ai/data/valuemap/refresh/ValueMappingRefreshOrchestrator.kt): resolve profile from `chat.value-mapping.embedding`.

### v1 constraints

- Exactly **one** entry in `sources` per profile (fail-fast if >1 until multi-source merge exists).
- Only `type: metadata-facets` implemented.

## Test plan

- Context smoke: minimal YAML starts `EmbeddingHarness`, `EmbeddingStore`, `StreamingChatModel` without legacy keys.
- Chat model uses `providers.openai` only (no `mill.ai.model.api-key`).

## Acceptance

- [ ] No runtime dependency on `mill.ai.model`, `mill.ai.embedding-model`, `mill.ai.value-mapping`, singleton `mill.ai.vector-store`.
- [ ] Value-mapping refresh scheduler uses new refresh prefix.
- [ ] `mill-ai-v3-autoconfigure` unit tests updated and passing.
