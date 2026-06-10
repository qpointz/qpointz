# ai-configuration-restructure

## Goal

Restructure **`mill.ai.*`** into a layered, extensible configuration model: **LLM providers** (with
`type`), **named models** (`models.chat` / `models.embedding`), optional **`vector-stores`**
registry, **`data.embedding.<profile>`** pipelines (sources → embed → vector store), and **`chat`**
defaults plus capability hooks (`chat.value-mapping.embedding`, `chat.schema-search.embedding`, …).

**Clean break** from legacy keys: `mill.ai.model`, `mill.ai.embedding-model`, `mill.ai.value-mapping`,
singleton `mill.ai.vector-store`. No backward-compatibility shims.

**Design reference:** plan *AI configuration restructuring* (branch `fix/ai-configuration-simplifaction`);
authoritative target sketch in [`docs/design/ai/mill-ai-configuration.md`](../../../design/ai/mill-ai-configuration.md)
(updated by **WI-288**).

### Target shape (summary)

```yaml
mill:
  ai:
    providers:
      openai: { type: openai, api-key: ..., base-url: ... }
    models:
      chat: { default: { provider: openai, model-name: gpt-4o-mini } }
      embedding: { default: { provider: openai, model-name: text-embedding-3-small, dimension: 1536 } }
    vector-stores:                    # optional shared connections
      pg: { backend: pgvector }
    data:
      embedding:
        default:
          model: default
          vector-store: { backend: pg, pgvector: { table: ..., create-table: true } }
          max-content-length: 2048
          refresh: { on-startup: { enabled: true }, schedule: { enabled: true, interval: PT15M } }
          sources:
            - type: metadata-facets    # adapter owns WI-181 facet logic in code
    chat:
      model: default
      default-profile: hello-world
      value-mapping:
        embedding: default
```

## Execution order

1. **WI-284** — property bindings + Java metadata (`providers.type`, `models`, `vector-stores`, `data.embedding`, `chat` capability refs).
2. **WI-285** — resolvers + autoconfigure wiring (chat model, embedding harness, vector store factory, refresh orchestrator, source router).
3. **WI-286** — migrate operator YAML (`mill-service`, deploy GCP template, testIT resources).
4. **WI-287** — unit + integration tests; remove legacy property classes.
5. **WI-288** — design doc, public reference, `CONFIGURATION_INVENTORY.md`.

## Scope

- **In:** `mill-ai-v3-autoconfigure`, `mill-ai-v3`, `mill-ai-v3-data` (refresh bridge), `apps/mill-service`, `deploy/gcp/resources/ai-config`, AI module testIT YAML.
- **Out:** AI v1 (`mill.ai.nl2sql.*`); new `sources[].type` beyond `metadata-facets`; multi-source merge (list accepts one entry in v1); non-`openai` provider wiring (field only).

## Placement

Archived [`docs/workitems/completed/20260610-ai-configuration-restructure/`](.) (closed **2026-06-10**).

## Work Items

- [x] WI-284 — `mill.ai` property bindings and Java metadata (`WI-284-mill-ai-configuration-property-bindings.md`)
- [x] WI-285 — Autoconfigure resolvers and runtime wiring (`WI-285-mill-ai-configuration-autoconfigure-wiring.md`)
- [x] WI-286 — Operator YAML migration (`WI-286-mill-ai-configuration-yaml-migration.md`)
- [x] WI-287 — Tests and legacy removal (`WI-287-mill-ai-configuration-tests.md`)
- [x] WI-288 — Design and public documentation (`WI-288-mill-ai-configuration-documentation.md`)

## Related

- Predecessor: [**WI-175**](../../../completed/20260416-implement-value-mappings/WI-175-mill-ai-v3-ai-configuration-foundation.md) (`mill.ai.providers` foundation).
- Supersedes ad-hoc `mill.ai.model` / `mill.ai.embedding-model` / `mill.ai.value-mapping` layout documented in [**WI-178**](../../../completed/20260416-implement-value-mappings/WI-178-value-mappings-stack-documentation.md).
