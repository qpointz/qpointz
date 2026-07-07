# Chart test and scenario proof strategy (Gap 21)

**Status:** Locked design input
**Pattern:** WI-351 Option D — layered mock-LLM + per-layer unit tests; **no live LLM in CI**
**Reference:** [`artifact-foundation.md`](../artifact-foundation.md) §9, metadata-authoring [`GAPS.md`](../../../workitems/completed/20260629-metadata-authoring-profiles/GAPS.md) §1

## Summary

| Concern | Decision |
|---------|----------|
| **CI proof** | Mapping, wiring, emit, persist, SSE, GET replay, wire — **mock LLM / inject events**, no flaky live model |
| **End-user validation** | **Manual** — operator exercises real chat + UI (out of automated CI scope) |
| **Scenario packs** | **Scripted** `ArtifactEmitScenariosIT` packs + baselines (mirror SQL/facet emit POC) |
| **Extract from live chat** | **`scenario-export`** endpoint + scenario capture — draft YAML, operator adds `verify:` |

---

## Locked proof vehicle: Option D (layered mock)

Same normative pattern as **WI-351**. Chart story proves each layer independently; integration uses
**scripted scenario harness**, not `OPENAI_API_KEY` in CI.

**Out of scope for automated proof:** live-LLM `mill-ai-test` packs for chart flows (optional
opt-in smoke only, like `LiveScenarioPacksIT`).

### L1–L6 layer table

| Layer | Module / test | Mock / inject | Assert |
|-------|---------------|---------------|--------|
| **L1 Validator + catalog** | `mill-ai` unit — WI-368 | Direct handler calls; mock schema port | `list_supported_charts` shape; `validate_chart_spec` pass/fail codes; `normalizedChart` / no emit on fail |
| **L2 Agent emit** | `mill-ai` — extend [`LangChain4jAgentEmitTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgentEmitTest.kt) | Mock `StreamingChatModel` returns `validate_chart_spec` tool request(s); mock chart validator + sql deps | `ProtocolFinal` with `chart-mapping.generated-chart`; multi-tool iteration → N finals for N parallel validators (Gap 14 / 19) |
| **L3 Persist + pointers** | `mill-ai` — [`StandardPersistenceProjectorTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjectorTest.kt) | Injected `ProtocolFinal` / routed events (no agent) | `chart.generated` rows; `last-chart` pointer upsert |
| **L4 SSE** | `mill-ai` — [`ChatSseEventTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/sse/ChatSseEventTest.kt) | `AgentEvent.ProtocolFinal` chart payload | `item.part.updated` with `partType: chart`, `presentation: structured` |
| **L5 GET wire** | `mill-ai-service` — [`ArtifactWireMapperTest`](../../../../ai/mill-ai-service/src/test/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapperTest.kt) | Persisted `chart.generated` record | Wire `kind: chart`; semantic fields preserved; no embedded row snapshot |
| **L6 UI compiler** | `ui/mill-ui` Vitest — **WI-370** | Fixture `generated-chart` payload + mock rows | `compileChartSpecToECharts` vectors; Run / Run all (no live SSE) |

**L1–L5:** WI-369 (+ WI-368 unit scope). **L6:** WI-370.

---

## Scripted scenario packs (`ArtifactEmitScenariosIT`)

Add packs under `ai/mill-ai-test/src/testIT/resources/scenarios/artifact-emit/`:

| Pack (draft name) | Mode | Proves |
|-------------------|------|--------|
| `data-analysis-chart-emit.yml` | scripted | SQL turn → chart turn; `chart.generated` + SSE `partType: chart` |
| `data-analysis-chart-invalid-field.yml` | scripted | `validate_chart_spec` fail → **no** chart artifact (Gap 12) |
| `data-analysis-chart-multi-query.yml` | scripted | 2× `validate_chart_spec` → 2× `chart.generated` (Gap 14) |
| `data-analysis-sql-then-chart-same-session.yml` | scripted | Two `run[]` items: ask₁ + validate_sql; ask₂ + validate_chart_spec |

Register in [`ArtifactEmitScenariosIT`](../../../../ai/mill-ai-test/src/testIT/kotlin/io/qpointz/mill/ai/test/ArtifactEmitScenariosIT.kt). Baselines under `scenarios/baselines/*.record.normalized.json`.

Example `verify` fragment (operator/template):

```yaml
verify:
  pass: ERROR
  check:
    - events:
        containsInOrder:
          - { type: tool.call, name: validate_chart_spec }
          - { type: tool.result, name: validate_chart_spec }
          - { type: protocol.final, protocolId: chart-mapping.generated-chart }
          - { type: answer.completed }
    - artifacts:
        - persistKind: chart.generated
          count: 1
    - artifacts.shape:
        persistKind: chart.generated
        match:
          payload.artifactType: generated-chart
    - sse:
        - type: item.part.updated
          presentation: structured
          partType: chart
```

Harness runner: [`ScriptedAgentRunner`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/runner/ScriptedAgentRunner.kt) — real tool handlers, **scripted model** (no LLM).

Refresh baselines: `UPDATE_BASELINES=1 ./gradlew :ai:mill-ai-test:testIT --tests ArtifactEmitScenariosIT`

---

## Extract scenarios from live conversations

Stable workflow to grow scripted packs without hand-authoring tool args:

```text
1. Enable mill.ai.chat.scenario-capture.enabled=true (dev/tuning)
2. Run manual chat (data-analysis) — SQL + chart flows
3. GET /api/v1/ai/chats/{chatId}/scenario-export  → draft YAML
4. Operator reviews script steps + verifyHints; adds verify: block manually
5. Commit pack under scenarios/artifact-emit/; run ArtifactEmitScenariosIT
```

### Export implementation today

| Component | Chart support |
|-----------|---------------|
| [`ScenarioCaptureRouting`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/scenario/ScenarioCaptureRouting.kt) | Persists `tool.call`, `tool.result`, `protocol.final` — **chart tools included** when capture on |
| [`ConversationScenarioExporter.buildScriptFromRunEvents`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/scenario/ConversationScenarioExporter.kt) | Replays captured **`validate_chart_spec`** (and parallel tools) from run events — **works** when capture enabled |
| [`verifyHintsForArtifacts`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/scenario/ConversationScenarioExporter.kt) | Generic by `persistKind` — **`chart.generated`** appears in hints — **works** |
| [`buildScriptFromArtifacts`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/scenario/ConversationScenarioExporter.kt) fallback | SQL + facet heuristics only — **chart gap** |

### WI-369 exporter extension (required)

Extend **`buildScriptFromArtifacts`** fallback when run events are missing:

- Detect `kind == "chart.generated"` (or payload `artifactType == generated-chart`)
- Emit scripted `validate_chart_spec` args reconstructed from persisted payload (`sql`, `charts[]` / encodings)
- Prefer **`normalizedChart` from validation tool result** in run events when both exist

Add tests:

- [`ConversationScenarioExporterTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/scenario/ConversationScenarioExporterTest.kt) — chart artifact fallback + verify hint
- [`AiScenarioExportControllerIT`](../../../../ai/mill-ai-service/src/testIT/kotlin/io/qpointz/mill/ai/service/AiScenarioExportControllerIT.kt) — HTTP export includes `validate_chart_spec` when run events seeded

Endpoint: [`AiScenarioExportController`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/AiScenarioExportController.kt) — `GET .../scenario-export` (YAML default, `?format=json` optional).

---

## What is not in CI

| Activity | Owner |
|----------|-------|
| Real LLM chart quality / UX | **Manual** — operator |
| Live `parameters.mode: live` chart packs | Optional opt-in only (`LiveScenarioPacksIT` pattern) |
| Skymill row value assertions in chart Run UI | WI-370 manual + structural Vitest |

---

## WI ownership

| WI | Proof scope |
|----|-------------|
| **WI-368** | L1 validator/catalog unit tests |
| **WI-369** | L2–L5 + scripted packs + scenario exporter chart support |
| **WI-370** | L6 UI compiler Vitest |

Cross-reference: [`chart-emission-path.md`](./chart-emission-path.md) (Gap 19), [`chart-mcp-exposure.md`](./chart-mcp-exposure.md) (MCP catalog tests separate from scenario harness).
