# Agentic Runtime v3 - Runtime, Events, Persistence, And Observers

**Status:** Active  
**Date:** March 19, 2026

---

## 1. Purpose

This chapter explains the lower half of `ai/v3`:

- how runs execute
- how raw events are emitted
- how events are routed
- how transcript, memory, artifacts, and telemetry differ
- how persistence is wired
- how artifact observers fit in

This chapter matters any time you build:

- a new runtime
- a new profile routing policy
- a new persistence adapter
- a Chat API or streaming endpoint
- downstream indexing or artifact consumers

---

## 2. Runtime Shape

The current runtime loops in `LangChain4jAgent` and `SchemaExplorationAgent` both follow the same basic structure:

1. allocate `runId`
2. allocate assistant `turnId`
3. ensure the durable conversation exists
4. append the user turn to `ConversationStore`
5. create a routed listener
6. emit `RunStarted`
7. resolve capabilities
8. build system prompt and tool surface
9. project memory back into model messages
10. execute the LLM/tool loop
11. emit raw `AgentEvent`s
12. route them into `RoutedAgentEvent`s
13. publish routed events to listeners and projectors

The runtime itself should stay focused on execution, not persistence details.

---

## 3. Raw Events

Raw runtime events are represented by `AgentEvent`.

Current event families:

- run lifecycle
  - `run.started`
- chat stream
  - `thinking.delta`
  - `message.delta`
  - `reasoning.delta`
  - `protocol.text.delta`
- tool loop
  - `tool.call`
  - `tool.result`
  - `plan.created`
  - `observation.made`
- final response
  - `answer.completed`
- protocol outputs
  - `protocol.final`
  - `protocol.stream.event`
- telemetry
  - `llm.call.completed`

Raw events answer:

> What happened in the runtime?

They do not answer:

> How should the platform persist or expose this?

That is the router’s job.

---

## 4. Routed Events

`RoutedAgentEvent` is the stable downstream envelope.

Current important fields:

- `runtimeType`
- `kind`
- `category`
- `destinations`
- `content`
- `route`
- `conversationId`
- `runId`
- `profileId`
- `turnId`
- `createdAt`

The most important design rule here is:

> downstream consumers should use `destinations`, not just `category`

Why:

- one routed event may belong to multiple lanes
- a transcript item may also be chat-stream visible
- a tool result may be telemetry and artifact-bearing at the same time

---

## 5. Stateless Routing

`DefaultAgentEventRouter` is stateless. It takes:

- a raw `AgentEvent`
- routing policy
- run and conversation context

and returns zero or more routed events.

That one-to-many behavior matters.

### 5.1 Example: normal answer

`answer.completed` becomes one routed event with destinations:

- `CHAT_STREAM`
- `CHAT_TRANSCRIPT`
- `MODEL_MEMORY`

### 5.2 Example: canonical artifact-bearing tool result

A `tool.result` containing a recognized `artifactType` may now produce:

1. the normal `tool.result` event
2. an extra artifact-routed event such as:
   - `sql.generated`
   - `sql.result`
   - `sql.validation`

This is how the SQL result path reaches artifact persistence and the no-op indexer even though the original raw event is still just `tool.result`.

### 5.3 Why router state is forbidden

Stateful routers make event interpretation harder to reason about and harder to test. Counting,
aggregation, and enrichment belong in listeners or projectors.

---

## 6. Destination Lanes

The current destination lanes are:

- `CHAT_STREAM`
- `CHAT_TRANSCRIPT`
- `MODEL_MEMORY`
- `ARTIFACT`
- `TELEMETRY`

### 6.1 Chat stream

Use for:

- live deep-thoughts/progress
- tool activity
- streamed text
- anything a Chat API might expose live

These events are not automatically durable transcript.

### 6.2 Chat transcript

Use for:

- canonical chat reconstruction
- user and assistant turns

Current first-pass projection:

- user turn is appended directly by the runtime
- assistant turn is created from routed `answer.completed`

### 6.3 Model memory

Use for:

- feeding the model a projected message history

The current runtime still writes memory directly through `saveToMemory(...)`. The routing lane
exists to keep the architecture aligned with future service/API layering.

### 6.4 Artifact

Use for:

- machine-readable outputs
- protocol outputs
- canonical artifact-bearing tool results

Artifacts are persisted in `ArtifactStore`.

### 6.5 Telemetry

Use for:

- run lifecycle
- token usage
- tool call counts
- selected diagnostics

Telemetry is partly durable and partly in-memory.

---

## 7. Default Routing Policy

`DefaultEventRoutingPolicy` provides the baseline mapping.

Current examples:

- `run.started`
  - `TELEMETRY`
  - persisted to `RunEventStore`
- `tool.call`
  - `CHAT_STREAM` and `TELEMETRY`
- `answer.completed`
  - `CHAT_STREAM`, `CHAT_TRANSCRIPT`, `MODEL_MEMORY`
  - persisted as transcript
- `protocol.final`
  - `CHAT_STREAM`, `ARTIFACT`
  - persisted as artifact
- `llm.call.completed`
  - `TELEMETRY`
  - persisted as run event

Profiles can override selected rules by replacing the event-type rule.

---

## 8. Profile-Specific Routing

Profiles should override routing when semantics differ.

Current example:

- `SchemaAuthoringAgentProfile`
  overrides `protocol.final` so persisted capture artifacts also update pointer key:
  - `last-schema-capture`

This pattern is preferred over ad hoc conditionals inside projectors.

Use profile overrides for:

- pointer keys
- custom artifact persistence rules
- profile-specific event destinations

---

## 9. Transcript Persistence

Transcript is persisted through `ConversationStore`.

Current contracts:

- `ensureExists(conversationId, profileId)`
- `appendTurn(conversationId, turn)`
- `attachArtifacts(conversationId, turnId, artifactIds)`
- `load(conversationId)`

Important current behavior:

- runtimes append the user turn directly before execution
- `StandardPersistenceProjector` appends assistant turns on `answer.completed`
- assistant turns may have zero or more attached artifact ids

### 9.1 Why transcript is separate from memory

Transcript is the chat-facing authority. Memory is the model-facing projection. They are not guaranteed to match.

### 9.2 Why transcript is separate from artifacts

One conversation item may own multiple artifacts. The chart-style case is:

- one assistant chat turn
- one SQL artifact
- one chart-config artifact

If artifacts became transcript items directly, the conversation would fragment.

---

## 10. Artifact Persistence

Artifacts are stored as `ArtifactRecord`.

Current fields:

- `artifactId`
- `conversationId`
- `runId`
- `kind`
- `payload`
- `turnId`
- `pointerKeys`
- `createdAt`

### 10.1 What should become an artifact

Good artifact candidates:

- `protocol.final` capture outputs
- canonical SQL result artifacts
- canonical SQL validation artifacts
- generated SQL statements

Bad artifact candidates:

- token deltas
- tool calls
- generic progress messages
- plain conversational answers

### 10.2 Artifact attachment

Current projection behavior is order-independent:

- if the artifact is persisted before the turn, the turn can include it
- if the turn exists first, `attachArtifacts(...)` updates the turn after artifact save

This was an important requirement for multi-artifact single-response cases.

### 10.3 Artifact pointers

`ActiveArtifactPointerStore` tracks latest relevant artifacts by key.

Use pointer keys for things like:

- `last-schema-capture`
- `last-sql`
- `last-sql-result`

Pointers are profile policy, not global framework truth.

---

## 11. Run Events And Telemetry

There are two related but separate telemetry mechanisms.

### 11.1 Durable run events

`RunEventStore` stores selected routed events when `persistEvent = true`.

Use it for:

- run lifecycle markers
- persisted telemetry
- auditable plan decisions

### 11.2 In-memory telemetry accumulation

`RunTelemetryAccumulator` listens on the telemetry lane and aggregates:

- token counts from `llm.call.completed`
- tool call counts from `tool.call`

Use this for:

- session dashboards
- CLI diagnostics
- future run summaries

Do not confuse the accumulator with the durable event log.

---

## 12. StandardPersistenceProjector

`StandardPersistenceProjector` is the current default projector registered by `AgentPersistenceContext`.

It handles:

- durable run events
- transcript turns
- artifacts
- active pointers
- post-persist artifact observers

### 12.1 Important rule

Observers are called only after artifact persistence succeeds.

That means:

- no persisted artifact
- no observer notification

This is why observer output only appears for actual artifact-bearing flows.

### 12.2 Best-effort observer isolation

Observer failures are swallowed and logged. They must not break artifact persistence.

---

## 13. Artifact Observers

`ArtifactObserver` is a downstream hook:

```kotlin
fun interface ArtifactObserver {
    fun onArtifactCreated(artifact: ArtifactRecord)
}
```

The current first implementation is `NoOpArtifactObserver`.

Its job:

- inspect the persisted artifact
- normalize an `ArtifactIndexingRequest`
- print a no-op indexing request

Its job is not:

- relation persistence
- correctness logic
- mutation of the core runtime flow

### 13.1 Why the no-op observer still matters

It proves the downstream seam:

- artifact persistence happened
- observer received the persisted record
- artifact kind and artifactType can be distinguished

That is the right first step before real indexing logic.

---

## 14. Chat Memory

`ChatMemoryStore` and `LlmMemoryStrategy` are the model-facing memory lane.

Current flow:

1. runtime loads `ConversationMemory`
2. `LlmMemoryStrategy.project(...)` produces `ConversationMessage`s for the current run
3. runtime appends current user input
4. after the run, runtime saves updated memory

### 14.1 Why memory is not transcript

Memory may be:

- bounded
- transformed
- summarized later
- missing empty assistant turns

Transcript is durable chat history. Memory is model context.

---

## 15. Implementing A New Persistence Adapter

When you add JPA or another durable backend, preserve these rules:

1. keep the port contracts in `mill-ai-v3`
2. make durable adapters implement the existing ports
3. do not leak Spring/JPA types into the framework contracts
4. preserve transcript/artifact separation
5. preserve append-only artifact history
6. keep pointer updates separate from artifact history

If the adapter needs a relation table for turn-to-artifact links, that is fine. The domain model already requires that linkage.

---

## 16. Common Runtime Extension Patterns

### 16.1 Add a new artifact-bearing tool result

Pattern:

1. tool returns structured payload with `artifactType`
2. router recognizes canonical `artifactType`
3. router emits extra artifact-routed event
4. projector persists artifact
5. observer receives persisted artifact

This is the current pattern used for SQL result paths.

### 16.2 Add a new pointer key

Pattern:

1. choose which raw event should ultimately create the artifact
2. override the profile rule for that event type
3. set `artifactPointerKeys`

### 16.3 Add a downstream indexer

Pattern:

1. implement `ArtifactObserver`
2. register it in `AgentPersistenceContext`
3. keep it best-effort
4. do not push correctness into it

---

## 17. Anti-Patterns

- using transcript as if it were model memory
- persisting every event into transcript
- having observers inspect raw events instead of persisted artifacts
- storing JSON strings inside artifact payloads
- making projectors depend on a specific profile id with hardcoded logic
- putting stateful counters into the router

---

## 18. Related Documents

- `v3-persistence-lanes.md`
- `v3-conversation-persistence.md`
- `v3-developer-recipes.md`
- `v3-developer-testing-and-debugging.md`
