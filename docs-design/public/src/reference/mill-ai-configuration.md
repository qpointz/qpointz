# Mill AI configuration (`mill.ai`)

Operator summary for **`mill.ai.*`**. Full specification:
**`docs/design/ai/mill-ai-configuration.md`**.

## Layers

| Prefix | Role |
|--------|------|
| **`mill.ai.providers.<id>`** | API keys, base URL, `type` (v1: `openai`) |
| **`mill.ai.models.chat.<name>`** | Chat model profile → provider + model name |
| **`mill.ai.models.embedding.<name>`** | Embedding profile → provider + model + dimension |
| **`mill.ai.vector-stores.<id>`** | Optional shared Chroma/pgvector connection templates |
| **`mill.ai.data.embedding.<profile>`** | Pipeline: model ref, vector store, refresh, sources |
| **`mill.ai.chat`** | Chat defaults + `value-mapping.embedding` → data profile |

## Minimal example

```yaml
mill:
  ai:
    providers:
      openai:
        type: openai
        api-key: ${OPENAI_API_KEY}
        base-url: https://api.openai.com/v1
    models:
      chat:
        default:
          provider: openai
          model-name: gpt-4o-mini
      embedding:
        default:
          provider: openai
          model-name: text-embedding-3-small
          dimension: 1536
    data:
      embedding:
        default:
          model: default
          vector-store:
            backend: in-memory
          sources:
            - type: metadata-facets
    chat:
      model: default
      value-mapping:
        embedding: default
```

## Mill Service profiles

In **`apps/mill-service/src/main/resources/application.yml`**:

- **`ai`** — enables the AI stack
- **`chromadb`** / **`ai-chromadb`** — Chroma vector store on the active embedding profile
- **`pgvector`** / **`ai-pgvector`** — pgvector on PostgreSQL (requires `vector` extension)

## Removed keys (breaking change)

Do not use: **`mill.ai.model`**, **`mill.ai.embedding-model`**, **`mill.ai.value-mapping`**, top-level **`mill.ai.vector-store`**. See the design doc migration table.

## See also

- [Metadata concepts](../metadata/concepts.md)
