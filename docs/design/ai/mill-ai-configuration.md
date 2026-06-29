# Mill AI configuration (`mill.ai.*`)

**Status:** Restructured (**WI-284**–**WI-288**, story `ai-configuration-restructure`). Property classes live in
**`mill-ai-v3-autoconfigure`** as Java `@ConfigurationProperties` with generated
**`spring-configuration-metadata.json`**.

## Purpose

**`mill.ai.*`** is the umbrella for Mill AI configuration, organized in layers:

1. **`providers`** — credentials and endpoints (`type` discriminant; v1: `openai`)
2. **`models`** — named chat and embedding profiles referencing providers
3. **`vector-stores`** — optional shared vector-store connection registry
4. **`data.embedding`** — embedding pipelines (model ref, vector store, refresh, sources)
5. **`chat`** — chat defaults and capability hooks (`value-mapping.embedding`, …)

**Clean break:** removed `mill.ai.model`, `mill.ai.embedding-model`, `mill.ai.value-mapping`, and singleton
`mill.ai.vector-store`. No backward-compatibility shims.

## `mill.ai.enabled`

| Property | Default | Effect |
|----------|---------|--------|
| **`enabled`** | **`true`** | When **`false`**, **`mill-ai-v3-autoconfigure`** skips AI beans. Root **`AiConfigurationProperties`** still binds. |

## `mill.ai.providers`

| Field | Description |
|-------|-------------|
| **`type`** | Provider implementation id (v1: **`openai`**). |
| **`api-key`** | Secret; referenced only here. |
| **`base-url`** | OpenAI-compatible API root (typically ends with `/v1`). |

## `mill.ai.models`

### `mill.ai.models.chat.<name>`

| Field | Description |
|-------|-------------|
| **`provider`** | Key into **`mill.ai.providers`**. |
| **`model-name`** | Remote model / deployment id. |

Selected by **`mill.ai.chat.model`** (default profile name: `default`).

### `mill.ai.models.embedding.<name>`

| Field | Description |
|-------|-------------|
| **`provider`** | Provider id or **`stub`** for deterministic local embeddings. |
| **`model-name`** | Remote model name when provider is not stub. |
| **`dimension`** | Vector dimension (required for stub and remote). |

Referenced from **`mill.ai.data.embedding.<profile>.model`**.

## `mill.ai.vector-stores` (optional registry)

Shared connection templates keyed by id (e.g. `pg`, `chroma-prod`):

| Field | Description |
|-------|-------------|
| **`backend`** | `in-memory`, `chroma`, or `pgvector`. |
| **`chroma.*`** / **`pgvector.*`** | Backend-specific non-secret settings (same semantics as profile `vector-store`). |

Per-profile **`mill.ai.data.embedding.<profile>.vector-store.backend`** may be:

- A **built-in** backend id (`in-memory`, `chroma`, `pgvector`), or
- A **registry key** — settings merge: registry base + profile overrides (profile `pgvector.table` wins over registry).

## `mill.ai.data.embedding.<profile>`

Embedding **pipeline** profile (data acquisition → embed → vector store).

| Field | Description |
|-------|-------------|
| **`model`** | Key into **`mill.ai.models.embedding`**. |
| **`vector-store`** | Inline backend or registry ref + `chroma` / `pgvector` nested settings. |
| **`max-content-length`** | Max **`String.length()`** for value content before embed (default **2048**). |
| **`refresh`** | Startup and scheduled refresh gates (formerly under `mill.ai.value-mapping.refresh`). |
| **`sources`** | Data acquisition only (v1: **one** entry, **`type: metadata-facets`**). Facet URNs and adapters are code-owned ([**WI-181**](../../workitems/completed/20260417-value-mapping-facets-vector-lifecycle/WI-181-value-mapping-facet-types.md)). |

Active profile for value-mapping: **`mill.ai.chat.value-mapping.embedding`** (default `default`).

### Refresh properties (`data.embedding.<profile>.refresh`)

| Property | Type | Default |
|----------|------|---------|
| **`on-startup.enabled`** | boolean | `true` |
| **`schedule.enabled`** | boolean | `true` |
| **`schedule.interval`** | `Duration` | `PT15M` |

## `mill.ai.chat`

| Property | Description |
|----------|-------------|
| **`model`** | Key into **`mill.ai.models.chat`**. |
| **`default-profile`** | Agent profile id for new chats. |
| **`default-user-id`** | Static user id when no security integration. |
| **`max-title-length`** | Auto-title truncation. |
| **`value-mapping.embedding`** | Key into **`mill.ai.data.embedding`**. |
| **`schema-search.embedding`** | Reserved; optional key into **`mill.ai.data.embedding`**. |
| **`scenario-capture.enabled`** | Dev/tuning only (default `false`). When `true`, persist extended run events (`tool.call`, `tool.result`, …) and expose **`GET /api/v1/ai/chats/{chatId}/scenario-export`** for draft ScenarioPack YAML. |

Agent profiles remain in the **profile registry**, not under `mill.ai.chat`.

## `mill.ai.profiles`

YAML **`kind: AgentProfile`** documents loaded at startup (default: `classpath:profiles/platform-agent-profiles.yaml`).

| Property | Description |
|----------|-------------|
| **`seed.resources`** | Spring `Resource` locations (classpath, file, cloud) for multi-document profile YAML |

| Profile id | Typical use |
|------------|-------------|
| **`hello-world`** | Smoke / harness |
| **`schema-exploration`** | Schema + metadata QUERY — no capture |
| **`metadata-authoring`** | Catalog-generic facet capture only |
| **`data-analysis`** | SQL + value-mapping + **metadata-authoring** (mixed documentary + query turns) |

**Deprecated:** profile id **`schema-authoring`** — use **`metadata-authoring`** (facets) or **`data-analysis`** (SQL + facets). Normative tool matrix and authoring loop: [`metadata-facet-catalog-v3.md`](../agentic/metadata-facet-catalog-v3.md).

```yaml
mill:
  ai:
    chat:
      default-profile: data-analysis
    profiles:
      seed:
        resources:
          - classpath:profiles/platform-agent-profiles.yaml
          # - file:/etc/mill/profiles/custom-profiles.yaml
```

### Chat model

1. **`mill.ai.chat.model`** → **`mill.ai.models.chat.<name>`**
2. **`provider`** → **`mill.ai.providers.<id>`** (credentials)
3. LangChain4j **`StreamingChatModel`** bean

### Embedding harness (value-mapping)

1. **`mill.ai.chat.value-mapping.embedding`** → **`mill.ai.data.embedding.<profile>`**
2. **`model`** → **`mill.ai.models.embedding.<name>`** → **`mill.ai.providers`**
3. **`EmbeddingHarness`** bean

### Vector store

1. Active **`data.embedding`** profile → **`vector-store`**
2. Merge with **`mill.ai.vector-stores.<ref>`** when `backend` is a registry key
3. LangChain4j **`EmbeddingStore`** bean (dimension from harness)

## Migration from legacy keys

| Legacy | Replacement |
|--------|-------------|
| `mill.ai.model.*` | `mill.ai.models.chat.<name>` + `mill.ai.providers.<id>`; select via `mill.ai.chat.model` |
| `mill.ai.embedding-model.<name>` | `mill.ai.models.embedding.<name>` |
| `mill.ai.value-mapping.embedding-model` | `mill.ai.chat.value-mapping.embedding` |
| `mill.ai.value-mapping.max-content-length` | `mill.ai.data.embedding.<profile>.max-content-length` |
| `mill.ai.value-mapping.refresh.*` | `mill.ai.data.embedding.<profile>.refresh.*` |
| `mill.ai.vector-store.*` | `mill.ai.data.embedding.<profile>.vector-store.*` (optional `mill.ai.vector-stores.*` registry) |

## Mill Service profile shortcuts

**[`apps/mill-service/src/main/resources/application.yml`](../../../apps/mill-service/src/main/resources/application.yml)**:

| Profile / group | Effect |
|-----------------|--------|
| **`ai`** | Full AI stack: providers, models, `data.embedding.default`, chat defaults. |
| **`chromadb`** / **`ai-chromadb`** | Sets `data.embedding.default.vector-store.backend=chroma` + `chroma.base-url`. |
| **`pgvector`** / **`ai-pgvector`** | Sets `data.embedding.default.vector-store.backend=pgvector` + table defaults. |

**`pgvector`** still requires a PostgreSQL **`DataSource`** with the **`vector`** extension.

## Example (operator)

```yaml
mill:
  ai:
    enabled: true
    providers:
      openai:
        type: openai
        api-key: ${OPENAI_API_KEY}
        base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
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
          max-content-length: 2048
          refresh:
            on-startup: { enabled: true }
            schedule: { enabled: true, interval: PT15M }
          sources:
            - type: metadata-facets
    chat:
      model: default
      default-profile: hello-world
      value-mapping:
        embedding: default
```

## Related documents

- [`../agentic/metadata-facet-catalog-v3.md`](../agentic/metadata-facet-catalog-v3.md) — catalog-generic facet authoring, YAML profiles
- [`../metadata/value-mapping-indexing-facet-types.md`](../metadata/value-mapping-indexing-facet-types.md)
- [`rag-value-mapping-integration.md`](rag-value-mapping-integration.md)
- [`../platform/CONFIGURATION_INVENTORY.md`](../platform/CONFIGURATION_INVENTORY.md)
