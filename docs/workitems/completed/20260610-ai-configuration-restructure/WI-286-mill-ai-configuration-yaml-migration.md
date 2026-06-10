# WI-286 — Operator YAML migration

**Story:** [`ai-configuration-restructure`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `🔧 refactoring` |
| **Area** | `ai` / `deploy` |
| **Depends on** | [**WI-285**](WI-285-mill-ai-configuration-autoconfigure-wiring.md) |
| **Enables** | [**WI-287**](WI-287-mill-ai-configuration-tests.md) |

## Goal

Update all in-repo operator and test YAML to the target `mill.ai` shape so deployments and CI match
the new bindings.

## Files (minimum)

| File | Notes |
|------|--------|
| [`deploy/gcp/resources/ai-config/config/config.tpl.yml`](../../../../deploy/gcp/resources/ai-config/config/config.tpl.yml) | Remove duplicated `model.api-key`; add `data.embedding.default` |
| [`apps/mill-service/src/main/resources/application.yml`](../../../../apps/mill-service/src/main/resources/application.yml) | `ai`, `pgvector`, `chromadb` profile blocks |
| [`apps/mill-service/application.yml`](../../../../apps/mill-service/application.yml) | Root sample if still maintained |
| [`ai/mill-ai-v3-autoconfigure/src/testIT/resources/application.yml`](../../../../ai/mill-ai-v3-autoconfigure/src/testIT/resources/application.yml) | Stub + in-memory |
| [`ai/mill-ai-v3-service/src/testIT/resources/application-testIT.yml`](../../../../ai/mill-ai-v3-service/src/testIT/resources/application-testIT.yml) | |
| [`ai/mill-ai-v3-test/src/testIT/resources/application.yaml`](../../../../ai/mill-ai-v3-test/src/testIT/resources/application.yaml) | |
| [`ai/mill-ai-v3-data/src/testIT/resources/application-chroma-explore-skymill.yml`](../../../../ai/mill-ai-v3-data/src/testIT/resources/application-chroma-explore-skymill.yml) | |

### Target GCP fragment

```yaml
mill:
  ai:
    providers:
      openai:
        type: openai
        api-key: $${OPENAI_API_KEY}
        base-url: $${OPENAI_BASE_URL:https://api.openai.com/v1}
    models:
      chat:
        default:
          provider: openai
          model-name: "${openai.model}"
      embedding:
        default:
          provider: openai
          model-name: "${openai.embedding.model}"
          dimension: ${openai.embedding.dimension}
    data:
      embedding:
        default:
          model: default
          vector-store:
            backend: pgvector
            pgvector:
              table: mill_langchain_embedding_store
              create-table: true
          max-content-length: 2048
          refresh:
            on-startup: { enabled: true }
            schedule: { enabled: true, interval: PT15M }
          sources:
            - type: metadata-facets
    chat:
      model: default
      value-mapping:
        embedding: default
      default-profile: schema-authoring
```

## Acceptance

- [ ] No remaining `mill.ai.model`, `mill.ai.embedding-model`, `mill.ai.value-mapping`, or top-level `mill.ai.vector-store` in listed files.
- [ ] Mill Service `ai` / `pgvector` / `chromadb` profile groups documented inline in YAML comments where helpful.
