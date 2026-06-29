# WI-365 — Scenario capture mode and DB export to ScenarioPack YAML

Status: `done`  
Type: `✨ feature`, `📝 docs`  
Area: `ai`  
Story: [`STORY.md`](STORY.md)  
**Branch:** `feat/scenario-capture-export`

## Problem Statement

The v3 scenario harness ([WI-300](../../completed/20260616-ai-artifact-emit-contract/WI-300-conversation-scenario-design.md))
runs YAML packs with hand-authored `verify` blocks. Live tuning in mill-ui produces rich
conversation data in `ai_*` tables, but there is no path to **export a chat → draft scenario pack**
for iterative prompt/tool tuning.

Real-time per-message filesystem recording was considered; **DB export on demand** is preferred:
conversations already persist; export is an offline read + YAML writer.

## Goal

When **`mill.ai.chat.scenario-capture.enabled=true`** (dev/tuning only):

1. Persist extended run events (`tool.call`, `tool.result`, …) to `ai_chat_run_event`.
2. Expose **`GET /api/v1/ai/chats/{chatId}/scenario-export`** (YAML download).
3. Reconstruct draft **`ScenarioPack`**: `ask` per user turn, best-effort `script` from run events
   (artifact fallback when capture was off), YAML **comments** suggesting verify checks — operator
   adds `verify:` manually.

When **`enabled=false`** (default): identical runtime to today.

## Configuration

```yaml
mill:
  ai:
    chat:
      scenario-capture:
        enabled: false   # default — production-safe
```

| `enabled` | Behavior |
|-----------|----------|
| `false` | Default routing; no export controller; no extra run-event writes |
| `true` | Extended routing persistence + export REST endpoint |

## In Scope

1. **`ScenarioCaptureProperties`** + metadata in `mill-ai-autoconfigure`
2. **Routing override** in `LangChain4jChatRuntime` when enabled (`EventRoutingPolicy.overriding` for `tool.call`, `tool.result`)
3. **`RunEventStore.findByChatIdOrderByCreatedAtAsc`** (+ JPA repository method)
4. **`ConversationScenarioExporter`** + YAML writer in `mill-ai` (relocate shared `ScenarioPack` types from `mill-ai-test`)
5. **Conditional REST controller** in `mill-ai-service`
6. **Design doc** § in [`ai-v3-conversation-scenarios.md`](../../../design/agentic/ai-v3-conversation-scenarios.md)
7. **Tests:** routing persistence, export IT, round-trip load via `ScenarioPackLoader`

## Out of Scope

- Automatic `verify:` block generation (manual attribution)
- Real-time FS write per message
- CLI export task (defer; REST sufficient for v1)
- Live-LLM CI matrix changes
- Schema migration (reuse existing `ai_chat_run_event` columns)

## Risk assessment

Focus: **overall system stability** and **added complexity** — not export/YAML quality (that is a tuning concern, out of scope here).

### System stability — **Low risk**

| Area | Impact | Notes |
|------|--------|-------|
| **Default path (`enabled=false`)** | **None** | No routing override, no export bean, no extra persistence. Chat runtime, SSE, artifact pipeline unchanged. |
| **Agent loop / LangChain4j** | **None** | No changes to `LangChain4jAgent`, tool execution, or coordinator. |
| **Event routing (when enabled)** | **Low** | Reuses existing `EventRoutingPolicy.overriding` — same persist path as `run.started` / `plan.created`; only toggles `persistEvent` for two kinds. |
| **Persistence schema** | **None** | No DDL; existing `ai_chat_run_event` table and indexes. |
| **Port contracts** | **Low** | Additive `RunEventStore.findByChatId`; in-memory test impl updated. No existing callers change signature. |
| **Regression surface** | **Low** | Existing ITs run with property absent/false; new tests cover enabled path only. |
| **Misconfiguration (prod)** | **Low (stability) / Medium (security)** | Enabling in prod adds DB writes + export endpoint — operational mistake, not a crash/failure mode. Document dev-only. |

**Verdict:** Safe to merge. Production stability unchanged unless the flag is explicitly turned on.

### Increased complexity — **Low–moderate**

| Addition | Ongoing cost |
|----------|--------------|
| `ScenarioCaptureProperties` + one conditional in `LangChain4jChatRuntime` | Small — single boolean gate; must stay thin (no per-event checks when off) |
| `RunEventStore.findByChatId` + JPA query | Small — one query method |
| `ConversationScenarioExporter` + YAML writer in `mill-ai` | Moderate — new package; must track `ScenarioPack` shape as harness evolves |
| `ScenarioPack` type relocation `mill-ai-test` → `mill-ai` | One-time refactor; reduces duplication long-term |
| Conditional REST controller in `mill-ai-service` | Small — standard Spring pattern |
| Design doc § | Small |

**Touch points:** ~4 modules (`mill-ai`, `mill-ai-autoconfigure`, `mill-ai-persistence`, `mill-ai-service`), ~6 new classes, no new Gradle modules.

**What we deliberately avoid** (keeps complexity bounded):

- No agent-loop hooks or per-message interceptors
- No schema migration or new tables
- No changes to profile YAML routing defaults
- No CI/live-LLM matrix changes in v1

**Verdict:** Isolated sidecar feature. Complexity is localized and opt-in; main maintenance cost is the exporter staying aligned with scenario harness types.

## Acceptance Criteria

- [x] `mill.ai.chat.scenario-capture.enabled` defaults to `false`; metadata documented
- [x] When `false`: no export endpoint bean; no measurable extra persistence vs baseline
- [x] When `true`: `tool.call` events persisted with `name` + `arguments` in `content_json`
- [x] `GET .../scenario-export` returns valid YAML with `name`, `profileId`, `run[].ask`
- [x] When capture was on for the chat, exported pack includes reconstructable `script` for SQL/facet POC paths
- [x] YAML includes commented verify hints from artifacts; no auto-generated `verify:` block
- [x] Design doc updated with capture + export workflow
- [x] `:ai:mill-ai:test`, `:ai:mill-ai-service:testIT`, `:ai:mill-ai-test:testIT` green

## Suggested commit

`[feat] WI-365: scenario capture mode and chat DB export to YAML`
