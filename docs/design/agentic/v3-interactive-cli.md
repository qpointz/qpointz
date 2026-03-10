# Agentic Runtime v3 — Interactive CLI

**Status:** Implemented
**Date:** March 2026
**Scope:** `ai/mill-ai-v3-cli` — manual interactive testing tool for `v3` agents

---

## 1. Purpose

The interactive CLI is a **manual testing and exploration tool** for the `v3` agentic runtime.

It is not a production UI.  Its purpose is to:

- run `v3` agents interactively against a real LLM
- observe the full event stream during a live conversation
- support testing of different agent types and capability sets as they are added
- complement `testIT` automated coverage with a human-in-the-loop path

---

## 2. Design Decisions

### 2.1 All events are displayed

The CLI displays every `AgentEvent` emitted by the agent, not just the final answer.

This is intentional.  Manual testing is most useful when the full event trace is visible,
including run lifecycle, thinking progress, tool calls, tool results, and model reasoning.

### 2.2 Generic event display via JSON serialization

All events except streaming token events are displayed through a single generic
`printEvent()` function:

- the event is serialized to JSON using Jackson
- the `type` field is extracted and displayed as a colored label
- the remaining fields are displayed as a compact inline JSON payload

```
  [run.started]      {"profileId":"hello-world"}
  [thinking.delta]   {"message":"Planning response..."}
  [tool.call]        {"name":"say_hello","arguments":"{\"name\":\"Alice\"}","iteration":0}
  [tool.result]      {"name":"say_hello","result":"{\"greeting\":\"Hello, Alice!\"}"}
  [answer.completed] {"text":"Done."}
```

This approach is **polymorphic by construction**.  When a new `AgentEvent` subtype is
added to the core module, the CLI displays it automatically without any code change.
No `when`-branch is needed.

### 2.3 Streaming token events are handled explicitly

Two event types are intentionally excluded from the generic path:

- **`MessageDelta`** — model answer tokens, streamed inline after `agent >` prefix
- **`ReasoningDelta`** — model extended-thinking tokens, streamed inside a bordered block

These require incremental print behavior that cannot be expressed as a complete JSON
object, because each event represents a partial token rather than a discrete fact.

```
  ┌─ reasoning
  │  <streaming reasoning text...>
  └─ end of reasoning
agent > <streaming answer text...>
```

All other event types — including any future additions — go through `printEvent()`.

### 2.4 Event type colour coding

The label colour of each event reflects its category:

| Colour  | Event category               |
|---------|------------------------------|
| Magenta | `tool.*` events              |
| Blue    | `reasoning.*` events         |
| Green   | `answer.*` events            |
| Cyan    | all other events (default)   |

Payload JSON is always displayed in dim white to reduce visual noise.

### 2.5 Extended thinking support

The CLI supports LangChain4j's `onPartialThinking` callback, which carries
reasoning/extended-thinking tokens from models that support them (for example
OpenAI o1/o3 or Claude with extended thinking enabled).

When a model emits reasoning tokens, they are displayed in a bordered `reasoning` block
before the answer.  When the model does not emit reasoning tokens, no block appears.

The corresponding `AgentEvent.ReasoningDelta` event type is defined in
`mill-ai-v3-core` so that any future agent or adapter layer can emit it independently
of the LangChain4j adapter.

---

## 3. Module

```
ai/mill-ai-v3-cli/
└── src/main/kotlin/io/qpointz/mill/ai/cli/
    └── CliApp.kt        entry point and all rendering logic
```

**Gradle coordinates:** `:ai:mill-ai-v3-cli`
**Main class:** `io.qpointz.mill.ai.cli.CliAppKt`

**Runtime dependencies:**

| Dependency              | Role                                  |
|-------------------------|---------------------------------------|
| `mill-ai-v3-core`       | `AgentEvent` types                    |
| `mill-ai-v3-capabilities` | capability discovery via ServiceLoader |
| `mill-ai-v3-langchain4j` | `OpenAiHelloWorldAgent`              |
| `jackson-module-kotlin` | generic event JSON serialization      |
| `bundles.logging`       | runtime-only SLF4j/Logback            |

---

## 4. Running the CLI

```bash
OPENAI_API_KEY=sk-...  ./gradlew :ai:mill-ai-v3-cli:run --console=plain
```

Optional environment variables:

| Variable         | Default      | Purpose                        |
|------------------|--------------|--------------------------------|
| `OPENAI_API_KEY` | *(required)* | OpenAI API key                 |
| `OPENAI_MODEL`   | `gpt-4o-mini`| Model name                     |
| `OPENAI_BASE_URL`| *(not set)*  | Custom base URL for compatible endpoints |

---

## 5. CLI Commands

| Input          | Behaviour                  |
|----------------|----------------------------|
| `<text>`       | Send to agent, display event stream |
| `/help`        | Show help and hint inputs  |
| `/exit`, `exit`, `quit`, `/quit` | Exit the CLI |

---

## 6. Relation to testIT

The CLI and `testIT` serve complementary roles:

| Concern                          | CLI | testIT |
|----------------------------------|-----|--------|
| Free-form conversation           | ✓   |        |
| Observing live event stream      | ✓   |        |
| Deterministic assertion          |     | ✓      |
| Regression guard                 |     | ✓      |
| CI execution                     |     | ✓      |
| Exploring new agent behaviour    | ✓   |        |

The CLI is preferred for exploratory work and manual validation of new capabilities or
agent profiles.  `testIT` is preferred for regression coverage of specific scenarios.

---

## 7. Extending for New Agents

When a new agent type is added:

1. Add any new `AgentEvent` subtypes to `mill-ai-v3-core`.  The CLI will display them
   automatically through `printEvent()` — no CLI changes needed.

2. If the new agent requires a different entry point or factory, update `CliApp.kt`
   to select between agents (for example via an `AGENT` environment variable or a
   `/agent` CLI command).

3. If the new agent emits new streaming token events (incremental partial tokens),
   add an explicit branch for them alongside `MessageDelta` and `ReasoningDelta`.
   All other event types should remain on the generic JSON path.
