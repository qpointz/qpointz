# Mill AI configuration (`mill.ai`)

Mill exposes AI-related settings under the **`mill.ai.*`** prefix. This page summarizes the **intended**
shape for operator-facing configuration. Exact property names and defaults are defined in code
(**`mill-ai-v3-autoconfigure`**) and may evolve. For the full design specification, see
**`docs/design/ai/mill-ai-configuration.md`** in the Mill source tree.

## Provider credentials (`mill.ai.providers`)

Configure **one block per provider id** (for example **`openai`**). Put **API keys and base URLs** here
so they are **not** repeated on every embedding or chat profile.

Example (illustrative):

```yaml
mill:
  ai:
    providers:
      openai:
        api-key: ${OPENAI_API_KEY}
        base-url: https://api.openai.com/v1
```

Use environment variables or a secrets manager; do not commit real keys.

## Embedding model registry (`mill.ai.embedding-model`)

Register **named** embedding profiles. Each profile names a **provider** (see above) and **non-secret**
options such as the embedding model id and dimensions.

## Value mapping (`mill.ai.value-mapping`)

For value-mapping flows, set **`embedding-model`** to the **name** of a profile in
**`mill.ai.embedding-model`**.

Example (illustrative):

```yaml
mill:
  ai:
    value-mapping:
      embedding-model: default-embed
```

## Vector store (`mill.ai.vector-store`)

Mill uses **one** active vector store implementation per running application (similarity search over
embeddings). Configuration lives under **`mill.ai.vector-store.*`**; see **`docs/design/ai/mill-ai-configuration.md`**
in the Mill repo for the full property table and defaults.

- **`mill.ai.vector-store.backend`** — `in-memory` (default) or **`chroma`** (LangChain4j HTTP client to ChromaDB).
- When **`backend` is `chroma`**, set **`mill.ai.vector-store.chroma.base-url`** (required), e.g. `http://localhost:8000`.
  Optional keys include **`api-version`** (`V1` / `V2`), **`tenant-name`**, **`database-name`**, **`collection-name`**, and **`timeout`** (duration).

**Mill Service profiles (illustrative):** Spring profile **`chromadb`** (or profile group **`ai-chromadb`**, which combines **`ai`** and **`chromadb`**) can point the vector store at a local Chroma instance on port 8000 when those profiles are defined in application YAML — see the service’s `application.yml` for the exact blocks.

**Golden-source** vectors for value mappings live in **`ai_embedding_model` / `ai_value_mapping`** (JPA + Flyway in **`mill-ai-v3-persistence`**); the **`mill.ai.vector-store`** bean is the **search** store only. **`ValueMappingService`** reconciles SQL-derived values into both (see the design doc).

## Chat models (`mill.ai.model`)

Chat / LLM settings (provider, model name, API key) use **`mill.ai.model`** today. A future release may
unify OpenAI credentials with **`mill.ai.providers`**; until then, follow existing application
documentation for chat.

## See also

- Metadata value mappings: [Metadata concepts](../metadata/concepts.md)
