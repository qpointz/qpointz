# Scenario capture and DB export

Export live AI v3 chats from `ai_*` persistence into draft **ScenarioPack** YAML for
manual `verify` tuning and scripted regression — without real-time filesystem recording.

**Related design:** [`docs/design/agentic/ai-v3-conversation-scenarios.md`](../../../design/agentic/ai-v3-conversation-scenarios.md) (WI-300 harness); extends with capture mode + export workflow.

**Backlog:** **A-97** (`done`)

**Story folder:** [`docs/workitems/completed/20260629-scenario-capture-export/`](.) — closed **2026-06-29**.

## Goal

1. Opt-in **`mill.ai.chat.scenario-capture.enabled`** (default `false`) — when on, persist
   full-fidelity run events (`tool.call`, `tool.result`, …) and expose export REST endpoint.
2. When off — **no** extra DB writes, **no** export endpoint, **no** runtime overhead.
3. **`GET /api/v1/ai/chats/{chatId}/scenario-export`** returns draft YAML: `ask` + best-effort
   `script` + commented verify hints; operator adds `verify` manually.

## Work Items

- [x] WI-365 — Scenario capture mode + DB export to ScenarioPack YAML (`WI-365-scenario-capture-db-export.md`)

## Verify (before story archive)

```bash
./gradlew :ai:mill-ai:test --tests "*ScenarioCapture*"
./gradlew :ai:mill-ai-service:testIT --tests "*ScenarioExport*"
./gradlew :ai:mill-ai-test:testIT --tests "*ArtifactEmit*"
```

## Branch

Delivered on **`feat/meta-capability-prompts`** (alongside WI-363); MR targets **`dev`**.
