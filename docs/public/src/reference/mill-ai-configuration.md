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
| **`mill.ai.chat`** | Chat defaults + `value-mapping.embedding` → data profile; optional **`scenario-capture`** (dev/tuning) |
| **`mill.ai.profiles`** | YAML agent profile seed locations (`seed.resources`) |
| **`mill.ai.mcp`** | MCP Streamable HTTP servlet (`enabled`, `profile`, `capabilities`, `http.endpoint`) |

## MCP (capability exposure)

When **`mill.ai.mcp.enabled=true`**, mill-service exposes v3 capabilities as MCP tools at
**`/services/mcp`** (same `/services/**` authentication as Jet/export). Opt-in — not enabled by default.

```yaml
mill:
  ai:
    mcp:
      enabled: true
      profile: schema-exploration
      http:
        endpoint: /services/mcp
```

Profile groups: **`ai-mcp`**, **`skymill-ai-mcp`** (adds **`mcp`** to **`ai`** / **`skymill-ai`**).
Example client: [`misc/examples/ai-mcp-langchain-skymill/`](../../../../misc/examples/ai-mcp-langchain-skymill/README.md).
Design: [`docs/design/agentic/v3-mcp-capability-exposure.md`](../../../../design/agentic/v3-mcp-capability-exposure.md).

Well-known discovery advertises **`ai-mcp`** under **`services`** when MCP is enabled (no `connections` entry).

## Scenario capture and export (dev/tuning)

Opt-in workflow for turning **live AI v3 chats** into draft **ScenarioPack** YAML for prompt/tool tuning and the `mill-ai-test` harness. **Not for production** — default off.

| Property | Default | Effect |
|----------|---------|--------|
| **`mill.ai.chat.scenario-capture.enabled`** | `false` | When `true`: persist `tool.call` / `tool.result` run events; register export REST endpoint |

```yaml
mill:
  ai:
    chat:
      scenario-capture:
        enabled: true   # dev only
```

1. Run conversations in mill-ui (or API) with capture enabled.
2. Download draft pack: **`GET /api/v1/ai/chats/{chatId}/scenario-export`** (`?format=yaml` default).
3. Hand-edit YAML: add **`verify:`** blocks using commented hints; commit under `ai/mill-ai-test/src/testIT/resources/scenarios/`.

Design: [`docs/design/agentic/ai-v3-conversation-scenarios.md`](../../../../design/agentic/ai-v3-conversation-scenarios.md) §6.

## Agent profiles (YAML)

Profiles are **`kind: AgentProfile`** YAML loaded via **`mill.ai.profiles.seed.resources`** (default: `classpath:profiles/platform-agent-profiles.yaml`). Select at chat creation with **`profileId`** or **`mill.ai.chat.default-profile`**.

| Profile | Use when |
|---------|----------|
| **`schema-exploration`** | Browse schema + read metadata — no facet capture |
| **`metadata-authoring`** | Document metadata (any catalog facet type) — no SQL |
| **`data-analysis`** | Mixed turns: generate SQL **and** capture facet proposals |

**Do not use** deprecated profile id **`schema-authoring`**. Operator detail: [`docs/design/agentic/metadata-facet-catalog-v3.md`](../../../../design/agentic/metadata-facet-catalog-v3.md).

```yaml
mill:
  ai:
    chat:
      default-profile: data-analysis
    profiles:
      seed:
        resources:
          - classpath:profiles/platform-agent-profiles.yaml
```

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
- **`mcp`** / **`ai-mcp`** / **`skymill-ai-mcp`** — Streamable HTTP MCP at `/services/mcp` (`mill.ai.mcp.enabled=true`)
- **`chromadb`** / **`ai-chromadb`** — Chroma vector store on the active embedding profile
- **`pgvector`** / **`ai-pgvector`** — pgvector on PostgreSQL (requires `vector` extension)

## Removed keys (breaking change)

Do not use: **`mill.ai.model`**, **`mill.ai.embedding-model`**, **`mill.ai.value-mapping`**, top-level **`mill.ai.vector-store`**. See the design doc migration table.

## See also

- [Metadata concepts](../metadata/concepts.md)
