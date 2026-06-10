# WI-287 — Tests and legacy configuration removal

**Story:** [`ai-configuration-restructure`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `🧪 test` |
| **Area** | `ai` |
| **Depends on** | [**WI-285**](WI-285-mill-ai-configuration-autoconfigure-wiring.md), [**WI-286**](WI-286-mill-ai-configuration-yaml-migration.md) |

## Goal

Prove the new configuration tree end-to-end; remove dead legacy property classes and update test
property paths.

## Deliver

### Unit tests (`mill-ai-v3-autoconfigure`)

- Update [`AiProvidersConfigurationPropertiesTest`](../../../../ai/mill-ai-v3-autoconfigure/src/test/kotlin/io/qpointz/mill/ai/autoconfigure/providers/AiProvidersConfigurationPropertiesTest.kt) for `models` + `data.embedding` paths.
- Update [`VectorStoreAutoConfigurationTest`](../../../../ai/mill-ai-v3-autoconfigure/src/test/kotlin/io/qpointz/mill/ai/autoconfigure/vectorstore/VectorStoreAutoConfigurationTest.kt) for profile-scoped vector store + `vector-stores` ref merge.
- Add: chat model resolves credentials only from `providers` (regression for duplication bug).
- Add: `vector-stores.pg` ref + profile `pgvector.table` override merges correctly.
- v1: reject `sources` list length > 1 with clear error.

### Integration tests

- `./gradlew :ai:mill-ai-v3-autoconfigure:testIT`
- `./gradlew :ai:mill-ai-v3-service:testIT` (if AI profile used)
- `./gradlew :ai:mill-ai-v3-data:testIT` for Chroma/Skymill profile YAML (when enabled)

### Cleanup

- Remove [`ValueMappingConfigurationProperties`](../../../../ai/mill-ai-v3-autoconfigure/src/main/java/io/qpointz/mill/ai/autoconfigure/config/ValueMappingConfigurationProperties.java) and singleton [`VectorStoreConfigurationProperties`](../../../../ai/mill-ai-v3-autoconfigure/src/main/java/io/qpointz/mill/ai/autoconfigure/config/VectorStoreConfigurationProperties.java) if fully replaced.
- Grep repo for `mill.ai.value-mapping`, `mill.ai.embedding-model`, `mill.ai.model` in production/tests; zero hits outside changelog/docs.

## Acceptance

- [ ] `mill-ai-v3-autoconfigure:test` and `testIT` green.
- [ ] Legacy property classes deleted; no stale `@EnableConfigurationProperties` for removed types.
- [ ] Document breaking change note in **WI-288** (operator migration table).
