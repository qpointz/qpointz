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
embeddings). Configuration lives under **`mill.ai.vector-store.*`** (details in the design doc). The
**MVP** uses an **in-memory** LangChain4j store; additional backends (pgvector, Chroma) attach under the same prefix.
**Golden-source** vectors for value mappings live in **`ai_embedding_model` / `ai_value_mapping`** (JPA + Flyway in **`mill-ai-v3-persistence`**); the **`mill.ai.vector-store`** bean is the **search** store only. **`ValueMappingService`** reconciles SQL-derived values into both (see the design doc).

## Chat models (`mill.ai.model`)

Chat / LLM settings (provider, model name, API key) use **`mill.ai.model`** today. A future release may
unify OpenAI credentials with **`mill.ai.providers`**; until then, follow existing application
documentation for chat.

## See also

- Metadata value mappings: [Metadata concepts](../metadata/concepts.md)
