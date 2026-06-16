# WI-310 — Live YAML packs + injected agent runner

Status: `done`  
Type: `✨ feature` / `🧪 test`  
Area: `ai`  
Story: [`STORY.md`](STORY.md)

## Depends on

- **WI-301** (`ScenarioPack`, `TurnCheckRegistry`, regression record).
- **WI-302** (`ScriptedAgentRunner` pattern; shared `TurnOutcome` collection).

## Goal

Run the **same YAML scenario packs** against a **real LLM** by injecting a caller-built [`LangChain4jAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt). Complements deterministic `mode: scripted` packs (WI-302); **not** primary CI for this story.

## Non-goals

- Harness reading `OPENAI_API_KEY` / model env vars (agent construction stays in testIT).
- HTTP scenario runner over `mill-ai-service` SSE (separate follow-up).
- Committed regression baselines for live runs (non-deterministic).
- Default CI gate — live packs are **opt-in** (`OPENAI_API_KEY` + explicit test class).

## Deliverables

### Runner abstraction

- [x] `AgentTurnRunner` — common interface for turn execution
- [x] **`ProvidedAgentRunner`** — reuses injected `(LangChain4jAgent, AgentPersistenceContext)` across pack turns
- [x] [`ScenarioPackRunner`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/runner/ScenarioPackRunner.kt) accepts injected `AgentTurnRunner`; `ScenarioPackRunner.scripted()` factory for deterministic default
- [x] `ScenarioPackTestBase.createPackRunner()` hook for testIT injection

### YAML contract (`mode: live`)

```yaml
name: live-hello-smoke
profileId: hello-world
parameters:
  mode: live
run:
  - ask: "Use say_hello to greet Bob."
    verify:
      pass: WARN
      check:
        - events:
            containsInOrder:
              - { type: tool.call, name: say_hello }
              - { type: answer.completed }
        - response:
            assert: not-blank
```

Rules:

- `ask.script` **ignored** when using `ProvidedAgentRunner`.
- Prefer **structural checks** over exact payload matchers.
- `verify.pass: WARN` default for live — failed checks do not fail the pack unless `ERROR`.

### Checks and records

- [x] `response.contains` soft matcher for live packs
- [x] Live runs write `build/reports/scenarios/<pack>.record.json` (audit / debug)
- [x] **No** baseline comparison for `mode: live`
- [x] Optional `runMeta.modelName` via `runMetaExtras` from testIT

### Test entry points

- [x] `LiveScenarioPackTestBase` + `LiveScenarioPacksIT`
- [x] `src/testIT/resources/scenarios/live/live-hello-smoke.yml`
- [x] Manual run documented in design doc

### Design doc

- [x] [`ai-v3-conversation-scenarios.md`](../../../design/agentic/ai-v3-conversation-scenarios.md) § live mode + activity logging

### Activity logging

- [x] [`ScenarioActivityLogger`](../../../../ai/mill-ai-test/src/main/kotlin/io/qpointz/mill/ai/test/runner/ScenarioActivityLogger.kt) — pack/turn boundaries, scripted model steps, runtime events, final response (scripted + live)
- [x] Logger `io.qpointz.mill.ai.test.scenario`; testIT `logback-test.xml` at INFO

## Acceptance criteria

- [x] `parameters.mode: live` pack runs end-to-end when testIT supplies agent + `OPENAI_API_KEY`
- [x] Same `ScenarioPackLoader` and `TurnCheckRegistry` as scripted path; runner injected by test
- [x] Live IT skipped (not failed) when API key absent
- [x] Regression record written; no baseline comparison for live packs
- [x] `./gradlew :ai:mill-ai-test:test` passes without API key

## Execution note

**Optional story WI** — does not block WI-303–308 artefact emit acceptance.

## Follow-ups (out of scope)

- HTTP `LiveHttpScenarioRunner` against `mill-ai-service`
- `ai:v3-integration` CI job with live pack matrix
- Live packs for every scripted baseline in `artifact-emit/`
