# AI v3 conversation scenario harness

**Status:** Implemented (WI-300)  
**Module:** `ai/mill-ai-test`  
**Related:** [`v3-validation-harness.md`](v3-validation-harness.md), [`artifact-emit-contract.md`](artifact-emit-contract.md)

---

## 1. Purpose

YAML-driven **scenario packs** replay multi-turn conversations against the real LangChain4j agent runtime with a **scripted model** (no live LLM). Each turn produces a **regression record** with full outcomes for diffing and offline re-check.

Inspired by retired v1 integration ([`ai-v1-integration/README.md`](../ai/ai-v1-integration/README.md)); greenfield schema (breaking changes from the old `mill-ai-test` skeleton).

---

## 2. ScenarioPack YAML

```yaml
name: pack-slug-or-title
profileId: hello-world
parameters:
  mode: scripted   # scripted | live (live not primary CI for this story)
run:
  - ask: "User message for this turn"
    script:        # required when mode=scripted
      - toolCalls:
          - name: say_hello
            args: { name: Alice }
      - answer: "Done."
    verify:
      pass: ERROR   # ERROR | WARN | INFO
      check:
        - events: { containsInOrder: [...] }
        - artifacts: [...]
        - sse: [...]
        - response: { assert: not-blank }
        - transcript: { turnCount: 2 }
```

### 2.1 `script` semantics

Each `script` entry is **one model invocation**, consumed in order from a shared queue used by:

- the planner loop (`LangChain4jAgent`)
- `LangChain4jProtocolExecutor` (CAPTURE / STRUCTURED_FINAL paths)

| Entry | Meaning |
|-------|---------|
| `toolCalls:` | Model returns tool requests (harness assigns synthetic ids if omitted) |
| `answer:` | Model returns final text (planner answer or protocol JSON) |

**QUERY two-step example (SQL, post WI-304):**

```yaml
script:
  - toolCalls:
      - name: validate_sql
        args: { sql: "SELECT 1", attempt: 1 }
  - answer: ""
```

**CAPTURE two-step example (facet):**

```yaml
script:
  - toolCalls:
      - name: propose_facet_assignment
        args: { facetTypeKey: descriptive, ... }
  - answer: '{"facetTypeKey":"descriptive",...}'
```

Script exhaustion (agent needs another model call but queue is empty) → test failure with profile, turn index, and script position.

### 2.2 Check types

| Key | Asserts |
|-----|---------|
| `events` | `containsInOrder` — list of `{ type, name?, protocolId? }` |
| `artifacts` | `persistKind`, `count`, optional `shape` |
| `sse` | `type`, `presentation`, `partType` |
| `response` | `assert: not-blank` |
| `transcript` | `turnCount` |

Register new types via `TurnCheckRegistry.register(...)`.

---

## 3. ConversationRegressionRecord (`schemaVersion: 1`)

Written to `ai/mill-ai-test/build/reports/scenarios/<pack-slug>.record.json` after each pack run.

```json
{
  "schemaVersion": 1,
  "recordedAt": "2026-06-11T12:00:00Z",
  "runMeta": {
    "mode": "scripted",
    "profileId": "hello-world",
    "gitCommit": "abc123",
    "scenarioSource": "scenarios/harness-smoke-hello.yml"
  },
  "pack": { "name": "...", "parameters": { "mode": "scripted" } },
  "summary": { "overall": "PASS", "turnCount": 1, "checksPassed": 2, "checksFailed": 0, "durationMs": 42 },
  "turns": [
    {
      "index": 0,
      "action": "ask",
      "input": { "ask": "...", "script": [...] },
      "outcome": { "response": "...", "events": [...], "artifacts": [...], "sseEvents": [...], "transcript": {...} },
      "verify": { "passLevel": "ERROR", "checks": [...], "results": [...] }
    }
  ]
}
```

### 3.1 Normalization (baselines)

`*.record.normalized.json` scrubs volatile fields before diff:

| Field | Treatment |
|-------|-----------|
| `runId`, `chatId`, `turnId`, `eventId`, `artifactId` | Placeholder or omit |
| UUID-shaped strings | `<uuid>` |
| `recordedAt`, `createdAt`, timestamps | Omit or sentinel |
| `runMeta.gitCommit` | Omit in baseline |
| Token stats | Omit |

Preserve: event order, types, protocol ids, persist kinds, payloads, SSE partTypes.

Refresh baselines: `UPDATE_BASELINES=1 ./gradlew :ai:mill-ai-test:testIT`

### 3.2 Offline re-check

Load a saved `.record.json` → `TurnCheckRegistry.replay(outcome, checks)` without re-running the agent.

---

## 4. CI (future)

Planned `ai:v3-integration` job: run `:ai:mill-ai-test:testIT`, publish `build/reports/scenarios/**` as artifacts (v1 pattern).

---

## 5. Relationship to v3-validation-harness

[`v3-validation-harness.md`](v3-validation-harness.md) describes the three-layer pyramid (unit / deterministic / live LLM). This document is the **normative YAML spec** for the deterministic layer implemented in `mill-ai-test`.
