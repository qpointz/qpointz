# Agentic Runtime v3 — Conversation Persistence

**Status:** Design
**Date:** March 2026
**Scope:** `ai/v3` conversation state — in-memory continuity, LLM memory, UX record

---

## 1. Two Distinct Persistence Concerns

Conversation persistence in `v3` covers two fundamentally different concerns that must be
designed and implemented separately.

### 1.1 LLM Chat Memory

What gets fed back into the model's context window so it can reason correctly across turns.

- Optimized for the **model**, not the user
- Token-budget bounded — context window is finite
- Can be lossy — windowed, summarized, or compressed as conversations grow
- May diverge from what was actually said (e.g. a summary replaces 20 turns)
- Contains model-facing artifacts that have no UX meaning: system prompts, raw tool
  call/result interleaving, compression summaries

### 1.2 UX Conversation Record

What gets stored so a user can resume, review, or continue a conversation across sessions.

- Optimized for the **user and UI**, not the model
- Complete and faithful — full fidelity of what happened
- Grows unboundedly as the conversation progresses
- Must support UI rendering: messages, tool outputs, results, timestamps
- Supports audit, sharing, and cross-session resume

---

## 2. Why They Must Be Separated

These two concerns appear combinable via a "projection" pattern: store the full UX record
once and derive LLM memory from it on each request. This holds for simple windowing but
breaks for any serious production runtime.

### 2.1 Summarization forces a second store

Once a conversation grows beyond a context window, LLM memory must compress old turns into
a summary. That summary:

- is produced by an LLM call — it is expensive to recompute
- must itself be stored between requests
- does not belong in the UX record (it is a model-facing artifact, not a user message)

At this point a second store exists whether it was designed in or not.

### 2.2 Different access patterns

|                   | LLM Memory              | UX Record                       |
|-------------------|-------------------------|---------------------------------|
| Read on           | Every request           | Conversation load / UI scroll   |
| Latency budget    | Sub-millisecond         | Hundreds of ms acceptable       |
| Size              | Bounded (context window)| Unbounded                       |
| Storage           | Hot cache / in-process  | Durable DB                      |
| Load strategy     | Full load every turn    | Paginated / lazy                |

For large conversations, loading thousands of UX record rows on every request to derive a
10-turn window is untenable. Separation lets LLM memory live in a fast, bounded store
independently of the full record.

### 2.3 Content diverges in both directions

LLM memory contains things that do not belong in the UX record:

- System prompts (re-injected each turn, not a user-visible message)
- Raw tool call/result interleaving that the model needs for ReAct reasoning
- Summarization artifacts

UX record contains things LLM memory does not need:

- Timestamps, user IDs, session metadata
- Ratings, reactions, UI annotations
- Structured protocol outputs (charts, tables, rendered artifacts)
- Audit trail of model version, capability set, run IDs

### 2.4 Structured artifacts are a third important persistence concern

For agentic query/refinement workflows, chat memory alone is not sufficient.

The runtime also needs durable, machine-readable artifacts representing what was produced in
earlier turns, for example:

- generated SQL
- SQL validation results
- SQL execution result references
- value-mapping results
- later, normalized query models

Artifacts should be preserved even when they are no longer the latest active artifact.

The "last SQL" or "active SQL" pointer is useful for follow-up refinement, but the full artifact
history is also valuable for:

- audit
- replay
- UX navigation
- behavioral analytics
- later offline analysis such as:
  - what users are generating most often
  - which tables/columns are most frequently touched
  - which artifacts are repeatedly refined or abandoned

These artifacts are different from both:

- chat memory
  - what was said
- UX conversation record
  - what happened from the user's perspective

They are the structured representation of what the system produced.

This matters because follow-up requests such as:

- "last query should also include Germany"
- "same query but exclude inactive clients"
- "keep the previous result and group by segment"

should ideally refine an explicit previous query artifact rather than forcing the model to
reconstruct prior state from raw conversation text.

---

## 3. Target Architecture

```
ConversationRecord              UX-facing, durable, append-only, complete
  turns: List<ConversationTurn>   userInput, assistantAnswer, toolCallAudit, timestamp
        │
        │  (shared conversationId foreign key only)
        │
ConversationMemory              LLM-facing, managed separately, hot store
  window: recent N turns          derived from record — cheap, no extra storage
  summary: compressed old turns   stored — computed once per compaction, not recomputed
        │
        ├──────────────┐
        │              │
ConversationArtifacts          machine-readable outputs from prior runs/turns
  generated SQL
  validation results
  execution references
  value mappings
  future query-model artifacts
        │
        │  LlmMemoryStrategy.project(memory)
        ▼
List<ChatMessage>               what the model actually sees this request
```

The two stores share only a `conversationId`. They evolve and are accessed independently.

Artifacts should also be keyed by `conversationId`, but unlike LLM memory they should remain
addressable by type and recency so the planner/runtime can select relevant prior outputs.

Two concepts should be distinguished:

- full artifact history
  - append-only record of all produced artifacts
- active artifact pointers
  - runtime/session-level references such as "last generated SQL" or "current result artifact"

The planner/refinement loop primarily needs active pointers.
Analytics, UX, and offline indexing need the full history.

### 3.1 LlmMemoryStrategy

A pluggable projection from `ConversationMemory` to `List<ConversationMessage>`:

```kotlin
fun interface LlmMemoryStrategy {
    fun project(memory: ConversationMemory): List<ConversationMessage>
}
```

Built-in strategies:

| Strategy | Behaviour |
|----------|-----------|
| `FullHistoryStrategy` | Replay all turns — default, suitable for short conversations |
| `WindowStrategy(n)` | Last N turns only |
| `TokenBudgetStrategy(n)` | Fit within N tokens, drop oldest first |
| `SummaryBufferStrategy` | Summarize old turns, keep recent in full |

The strategy is a runtime concern — it does not affect storage schema.

---

## 4. Current State (WI-072)

WI-072 delivered **in-memory conversation continuity** for the CLI REPL. This is the
starting point, not the production shape.

`ConversationSession` currently serves both concerns simultaneously:

```kotlin
data class ConversationSession(
    val conversationId: String,
    val profileId: String,
    val messages: MutableList<ConversationMessage>,  // USER/ASSISTANT pairs only
)
```

This is appropriate for the CLI testing scope. The session is created once per CLI run,
lives in process, and is never persisted.

The agent (`SchemaExplorationAgent.run(session, input, listener)`) replays the session's
`USER`/`ASSISTANT` pairs into the LangChain4j messages list each turn, then appends the
completed turn back. This is a `FullHistoryStrategy` inlined into the agent.

### 4.1 What this covers

- Multi-turn context in the CLI REPL
- Refine-style follow-ups (`remove email`, `show as pie chart`) have prior context
- `/clear` command resets the session

### 4.2 What this does not cover

- Persistence across process restarts
- Cross-request state in an HTTP service
- Memory management for long conversations
- Full tool-call audit in the record
- UX record vs LLM memory separation
- Structured artifact reuse for deterministic follow-up/refinement

## 4A. Why Structured Artifacts Matter For Refinement

Without structured artifacts, a follow-up like:

- "also include Germany"

must be interpreted from chat text alone.

With structured artifacts, the runtime can instead:

1. load the most recent query artifact
2. identify the previously used table and filters
3. modify the relevant predicate
4. validate and execute again

Example prior SQL artifact:

```json
{
  "artifactType": "generated-sql",
  "statementId": "q-17",
  "sql": "SELECT * FROM `retail`.`customers` WHERE `country` = 'Switzerland'",
  "source": "generated"
}
```

Example prior execution artifact:

```json
{
  "artifactType": "sql-result",
  "statementId": "q-17",
  "resultId": "res-17",
  "rowCount": 42
}
```

This allows refinement to operate on:

- what was produced

rather than only on:

- what was said

That is one of the main long-conversation architectural advantages of `v3`.

---

## 4B. Agent Events Must Be Clarified Before Persistence

If persistence work starts now, one of the first clarifications should be the role of
`AgentEvent`.

The current event model is useful for runtime streaming and debugging, but it should not be
treated as identical to "what was said" in the conversation.

### 4B.1 Current `AgentEvent` types

The runtime currently emits events such as:

- `run.started`
- `thinking.delta`
- `plan.created`
- `message.delta`
- `tool.call`
- `tool.result`
- `observation.made`
- `answer.completed`
- `reasoning.delta`
- `protocol.text.delta`
- `protocol.final`
- `protocol.stream.event`

These events are not all equal from a persistence perspective.

### 4B.2 Three persistence categories

For persistence design, the event/output space should be split into three categories:

#### A. Canonical conversation content — "what was said"

This is the durable conversational record the user would reasonably interpret as the dialogue.

Examples:

- user input
- final assistant answer
- explicit clarification question shown to the user
- possibly protocol text that is part of the assistant-visible answer

This should become the primary conversation-turn record.

#### B. Runtime event audit — "what happened during the run"

This is execution telemetry and debugging/audit information.

Examples:

- `run.started`
- `thinking.delta`
- `plan.created`
- `tool.call`
- `tool.result`
- `observation.made`
- `reasoning.delta`
- intermediate protocol stream events

These events are valuable, but they are not the canonical conversation transcript.

They should be persisted separately as run audit events or event logs.

#### C. Structured artifacts — "what was produced"

This is the durable machine-readable output of the run.

Examples:

- generated SQL
- SQL validation result
- SQL result reference
- value mappings
- capture payloads

These should be stored as artifacts, not as plain messages and not merely as event text.

### 4B.3 Recommended rule: events are not the conversation record

The persistence model should treat `AgentEvent` as:

- an execution/event stream

and not as:

- the authoritative conversation transcript

Otherwise the system will conflate:

- user-visible chat
- internal progress narration
- tool telemetry
- protocol emissions

and later have difficulty answering basic questions such as:

- what did the user ask?
- what did the assistant finally answer?
- what tools were used?
- what artifact was produced?

### 4B.4 Recommended normalization

A good persistence shape is:

- `ConversationTurn`
  - canonical user input and assistant-visible output
- `RunEvent`
  - raw or normalized `AgentEvent` audit stream
- `RunArtifact`
  - machine-readable outputs from the run
- `ArtifactRelation`
  - derived links from artifacts to related domain/UI objects

This lets the system answer different questions cleanly:

- UI transcript → `ConversationTurn`
- debug / replay / audit → `RunEvent`
- follow-up refinement / machine reuse → `RunArtifact`

### 4B.5 Suggested mapping of current events

Recommended treatment of current runtime events:

| Event type | Primary persistence target |
|---|---|
| `run.started` | `RunEvent` |
| `thinking.delta` | `RunEvent` |
| `plan.created` | `RunEvent` |
| `message.delta` | transient stream only, optionally `RunEvent` |
| `tool.call` | `RunEvent` |
| `tool.result` | `RunEvent` and possibly `RunArtifact` if it is canonical |
| `observation.made` | `RunEvent` |
| `answer.completed` | `ConversationTurn` assistant output |
| `reasoning.delta` | `RunEvent` only, not canonical conversation |
| `protocol.text.delta` | transient stream only, optionally `RunEvent` |
| `protocol.final` | `RunArtifact`, possibly summarized into assistant output |
| `protocol.stream.event` | `RunEvent` and/or `RunArtifact` depending on protocol semantics |

### 4B.6 First practical implication

If the immediate goal is persistence of "what has been said", the minimum durable unit should be:

- user turn
- final assistant turn

not:

- every raw runtime event

Persisting every event is still useful, but it should be treated as:

- audit/debug persistence

rather than as the primary conversation-memory model.

### 4B.7 Events need routing policy: transient vs durable

In practice, the runtime emits many events that are useful during live UX streaming but should not
be persisted durably.

This creates two broad classes of event:

#### Transient UX events

These are primarily for immediate feedback during a live run.

Examples:

- "thinking" style updates
- intermediate deltas
- low-level progress narration

They may be shown to the user, but they should usually not be persisted as canonical history.

#### Durable events

These are meaningful after the run finishes and are candidates for persistence.

Examples:

- final assistant answer
- tool call
- tool result
- final structured protocol output
- major run lifecycle markers

So the runtime should not treat `AgentEvent` as "persist everything by default".
It should route events according to policy.

### 4B.8 Recommended event-routing model

Recommended approach:

- profiles declare event-routing policy
- the runtime applies that policy

This is preferable because different profiles may want different UX/audit behavior.

Examples:

- an analysis/query profile may expose SQL validation and execution events to downstream consumers
- an authoring profile may expose capture/proposal events and suppress low-value SQL noise
- an editor-oriented profile may expose query-updated events but not execute by default

So the profile is a good place to define:

- which events are exposed to downstream consumers
- which events are persisted
- which events contribute to conversation transcript
- which events are converted into artifacts

The router should also assign a stable routed event kind for downstream consumers.

This matters because consumers often depend not only on whether an event is visible, but also
on what category of event it is.

For example, a downstream consumer may want to treat differently:

- thinking/progress events
- tool-call events
- SQL validation events
- SQL execution events
- final answer events
- metadata capture events

So the routed event should preserve:

- the original runtime event type
- a stable routed event kind

The routed event kind can be derived from:

- runtime event type
- tool name
- protocol id
- active profile

But the routing logic itself should remain a runtime responsibility.

### 4B.9 Suggested policy shape

One possible shape:

```kotlin
data class EventRoutingPolicy(
    val rules: List<EventRoutingRule>
)

data class EventRoutingRule(
    val eventType: String,
    val exposeToConsumers: Boolean,
    val persistEvent: Boolean,
    val persistAsTranscript: Boolean,
    val persistAsArtifact: Boolean,
)
```

And the profile could declare:

```kotlin
data class AgentProfile(
    val id: String,
    val capabilityIds: Set<String>,
    val eventRoutingPolicy: EventRoutingPolicy,
)
```

Recommended implementation discipline:

- runtime owns event dispatch and persistence
- profile owns policy declaration
- policy should inherit from sensible runtime defaults, with profile-level overrides

### 4B.9A Routed event envelope

The routed/published event should ideally not be just free-form text.

It should carry:

- stable runtime type
- stable routed kind
- structured content object
- routing metadata

For example:

```kotlin
data class RoutedAgentEvent(
    val runtimeType: String,
    val kind: String,
    val content: Map<String, Any?>,
    val route: EventRoute,
    val conversationId: String?,
    val runId: String?,
    val profileId: String,
)
```

Where `content` is a structured payload suitable for:

- consumer rendering
- persistence as JSON
- later SSE publication

Examples:

- `thinking`

```json
{
  "kind": "thinking",
  "content": {
    "message": "checking schema"
  }
}
```

- `tool.call`

```json
{
  "kind": "tool.call",
  "content": {
    "toolName": "list_columns",
    "arguments": {
      "schemaName": "retail",
      "tableName": "customers"
    }
  }
}
```

- `sql.validation.result`

```json
{
  "kind": "sql.validation.result",
  "content": {
    "passed": true,
    "attempt": 1,
    "normalizedSql": "SELECT ..."
  }
}
```

- `assistant.answer`

```json
{
  "kind": "assistant.answer",
  "content": {
    "text": "I updated the query ..."
  }
}
```

This lets consumers switch on `kind` and handle structured data rather than reparsing raw strings.

### 4B.10 Example default routing intent

Examples of likely defaults:

| Event type | Expose to consumers | Persist event | Transcript | Artifact |
|---|---|---|---|---|
| `thinking.delta` | yes | no | no | no |
| `message.delta` | yes | usually no | no | no |
| `plan.created` | maybe | maybe | no | no |
| `tool.call` | maybe | yes | no | no |
| `tool.result` | maybe | yes | no | maybe |
| `answer.completed` | yes | yes | yes | no |
| `protocol.final` | maybe | yes | maybe | yes |

The exact policy can vary by profile, but this illustrates the main principle:

- live UX stream and durable persistence are related, but not identical

### 4B.11 Event propagation facility

Because routed events may have several consumers, the runtime should expose a publisher/listener
style facility.

Recommended flow:

1. executor emits raw `AgentEvent`
2. router applies profile policy and produces `RoutedAgentEvent`
3. publisher broadcasts routed event
4. listeners consume it according to concern

Examples of listeners:

- CLI streaming listener
- persistence listener
- artifact extraction listener
- future SSE listener
- debug logging listener

This keeps:

- routing centralized
- propagation reusable
- persistence/SSE/UX concerns decoupled

## 5. Recommended Persistence Work Item Split

When persistence work begins, treat the two concerns as separate tracks:

**Track A — UX Conversation Record persistence**

- Define `ConversationRecord` and `ConversationTurn` in `mill-ai-v3-core`
- Add `ConversationRecordStore` interface (load, save, append)
- Implement an in-process / JDBC store
- HTTP layer: accept `conversationId`, load record, return in response

**Track B — LLM Memory management**

- Extract `LlmMemoryStrategy` interface to `mill-ai-v3-core`
- Implement `WindowStrategy` and `TokenBudgetStrategy`
- Make strategy injectable into the agent run
- Defer `SummaryBufferStrategy` — requires LLM call, plan separately

**Track C — Structured artifact persistence and reuse**

- Define typed run artifacts for query-oriented workflows
- Persist artifacts by `conversationId`, `runId`, and `artifactType`
- Track active / latest relevant artifact references in run state
- Make refinement-capable planners load the most recent relevant artifact as working context
- Prevent synthesis from claiming facts that are absent from the underlying artifact

These tracks can be worked in parallel or sequenced. Track A delivers UX value
(conversation resume). Track B delivers model quality (no context overflow on long
sessions). Neither depends on the other.

---

## 6. Relation to Current Types

| Current type | Future role |
|---|---|
| `ConversationSession` | Transitional — merges into `ConversationRecord` (UX) + in-process `ConversationMemory` (LLM) |
| `ConversationMessage` | Stays in core; used in both tracks with `MessageRole` as-is |
| `MessageRole` | Stays; `TOOL_RESULT` and `SYSTEM` become relevant in Track B |
| `RunState.conversationId` | Foreign key linking a single run to its `ConversationRecord` |

Additional target types for Track C:

| Future type | Role |
|---|---|
| `RunArtifact` | Base interface for persisted machine-readable outputs |
| `GeneratedSqlArtifact` | Canonical SQL statement produced in a turn |
| `SqlValidationArtifact` | Validation result for a generated statement |
| `SqlResultArtifact` | Execution/result reference from SQL execution |
| `ValueMappingArtifact` | Canonical mapped values used during query construction |
| `ArtifactRelation` | Derived relation between artifact/conversation/run and domain objects |

## 7. Recommended Rule For Query Follow-Ups

When a user refers to prior work using phrases such as:

- "last query"
- "same query but"
- "also include"
- "exclude"
- "keep everything else the same"

the planner/runtime should prefer:

- refining the most recent relevant structured artifact

instead of:

- reconstructing prior state only from conversational text

This rule should be reflected in:

- system prompts
- planner policy
- runtime artifact selection logic

## 9. Consistency Model: Prefer Low Latency Over Strong Consistency

For `v3` persistence, the default design bias should be:

- low latency on the user-facing runtime path
- eventual consistency for most durable projections

This is a better fit than making every persistence write part of the synchronous agent critical
path.

### 9.1 Why this bias makes sense

The runtime already has natural layering:

- live event emission
- in-memory run/session state
- durable transcript/event storage
- durable artifact storage
- downstream relation indexing and analytics

Not all of these layers need strong consistency at the same moment.

For most user-facing workflows, the immediate priorities are:

- keep the run responsive
- keep streaming low-latency
- preserve correctness of the active in-memory session

while many durable projections can lag slightly behind.

### 9.2 Recommended synchronous path

Keep only the minimum required state updates on the synchronous path.

Typical examples:

- in-memory session state
- in-memory active artifact pointers
- runtime step/run progression

Depending on failure tolerance, a minimal append of the canonical conversation turn may also
eventually become synchronous, but this should be justified explicitly rather than assumed.

### 9.3 Recommended asynchronous / eventual path

These are good candidates for eventual consistency:

- routed event log persistence
- durable transcript projection when not required immediately for correctness
- durable artifact persistence
- artifact relation indexing
- analytics projections
- RAG/index documents derived from artifacts
- "related objects" graph maintenance

These components should ideally consume emitted runtime outputs asynchronously and update their
stores without blocking the chat flow.

### 9.4 Practical lane interpretation

The current persistence lanes fit this model well:

#### Lane 1 — routed agent events

- live UX emission: immediate
- durable event persistence: usually asynchronous
- transcript projection: asynchronous unless immediate cross-request durability is required

#### Lane 2 — artifacts

- active artifact in session state: immediate
- durable artifact store: preferably asynchronous

#### Lane 3 — chat memory

- active in-memory memory/session: immediate
- durable memory snapshot or backing store: eventual unless immediate resume guarantees are needed

#### Lane 4 — relation indexing / observers / analytics

- always asynchronous
- explicitly best-effort

### 9.5 Important discipline

The runtime should distinguish:

- source-of-truth state required for the current live run
- derived persistent views used by other consumers

The first must stay correct immediately.
The second can be allowed to converge.

### 9.6 Failure semantics

If eventual consistency is the default, then the design should make failure semantics explicit.

In particular, decide which data is acceptable to lose between:

- event emitted
- event/artifact durably persisted

For early iterations, it may be acceptable that:

- audit/event persistence lags
- relation indexes lag
- analytics lag

while the active in-memory session remains the authoritative live state.

Later, if cross-process resume becomes mandatory, the minimal durable subset can be increased
without changing the general architecture.

## 10. Implementation Guidance: Injectable Stores, In-Memory First

The first implementation round should prefer:

- injectable repository/store ports
- in-memory adapters
- no hardcoded persistence dependencies inside `v3-core`

### 10.1 Stores must be injected, not hardcoded

Persistence collaborators should be provided at agent/runtime instance construction time.

This is important because the same runtime should support:

- in-memory implementations for early development and testing
- future Spring/JPA-backed implementations
- profile- or environment-specific store combinations

So `v3-core` should depend on interfaces such as:

- `ConversationStore`
- `RunEventStore`
- `ArtifactStore`
- `ChatMemoryStore`
- `RelationStore`

and receive implementations through constructor-injected dependencies or dependency holders.

`v3-core` should not:

- instantiate repositories directly
- depend on Spring injection
- use global singletons for persistence
- embed JPA or framework annotations in core runtime types

### 10.2 Recommended dependency shape

One possible shape:

```kotlin
data class PersistenceDependencies(
    val conversationStore: ConversationStore,
    val runEventStore: RunEventStore,
    val artifactStore: ArtifactStore,
    val chatMemoryStore: ChatMemoryStore? = null,
    val relationStore: RelationStore? = null,
)
```

The exact shape may vary, but the key rule is:

- stores are selected outside the core runtime
- the runtime only knows the ports

### 10.3 First round: in-memory adapters

The recommended first implementation round is:

- in-memory repositories/stores
- enough semantics to validate lane boundaries and runtime integration
- no premature JPA schema lock-in

This is the fastest way to validate:

- routed event persistence
- transcript projection
- artifact creation and selection
- chat-memory integration
- observer/indexer flow

### 10.4 Where plain in-memory structures are better than cache semantics

For canonical append-only records, simple in-memory stores are often better than eviction-driven
cache structures.

Good candidates for simple append-only in-memory implementations:

- conversation turns
- routed event log
- artifact history

Typical implementation forms:

- `ConcurrentHashMap`
- append-only lists keyed by `conversationId` / `runId`

### 10.5 Where Caffeine fits well

Caffeine is a good fit for cache-like or bounded working-state concerns such as:

- active session cache
- active artifact pointers
- recent chat-memory window
- derived relation caches
- latest-artifact lookup indexes

So the first round can reasonably combine:

- canonical in-memory stores for source-of-truth records
- Caffeine-backed caches/indexes for active/derived lookup paths

### 10.6 Future Spring/JPA adapters

Later, Spring/JPA-backed adapters can be introduced in a separate adapter module without changing
the core runtime contract.

That future module should contain:

- JPA entities
- Spring repositories
- transactional adapter implementations of the core store interfaces

while `v3-core` remains:

- Kotlin-only
- Spring-free
- persistence-port oriented

## 8. Artifact Observer / Relation Indexing

Artifacts are useful not only for conversation refinement but also for downstream UX and analysis.

One important example is the `mill-ui` related-objects experience:

- open a model/table view
- show related chats
- show related generated SQL
- show related metadata authoring proposals

This should be handled by a side-effect component that consumes artifacts after they are produced,
without influencing the chat loop itself.

### 8.1 Recommended role

Introduce a downstream component such as:

- `ArtifactObserver`
- or more specifically `ArtifactRelationIndexer`

Its responsibility is:

- observe artifact create/update events
- analyze artifact content
- derive relations to domain/UI objects
- persist those derived relations for lookup by consumers

### 8.2 Why this should stay outside the agent loop

This logic is:

- useful for UX navigation
- useful for analytics
- useful for audit and discovery

but it is not required for:

- prompt construction
- planner correctness
- tool execution
- chat-memory continuity

So it should remain a downstream side effect, not part of model reasoning.

### 8.3 Example derived relations

From a generated SQL artifact:

- conversation -> table
- run -> table
- artifact -> table
- artifact -> column

From an authored metadata capture artifact:

- conversation -> metadata entity
- artifact -> target entity

From a value-mapping artifact:

- artifact -> table
- artifact -> attribute

Example relation form:

```kotlin
data class ArtifactRelation(
    val sourceType: String,
    val sourceId: String,
    val relationType: String,
    val targetType: String,
    val targetId: String,
)
```

Examples:

- `conversation:C1 --REFERENCES_TABLE--> retail.customers`
- `artifact:A9 --USES_COLUMN--> retail.customers.country`
- `artifact:A10 --TARGETS_ENTITY--> retail.orders.customer_id`

### 8.4 Observer trigger model

Recommended flow:

1. runtime persists artifact
2. artifact event is emitted or callback is invoked
3. observer/indexer analyzes artifact
4. derived relations are persisted
5. UI/analytics query relation store

This observer should ideally be:

- asynchronous
- best-effort
- non-blocking for the user-facing chat run

Chat correctness must not depend on relation indexing succeeding.

### 8.5 Relation indexing and artifact history

Relation indexing is another reason full artifact history must be preserved rather than replacing
old artifacts with only the latest active one.

Even if the current active SQL artifact changes after refinement, older SQL artifacts remain useful
for:

- understanding user exploration patterns
- showing previous related queries for a table/object
- identifying objects that attract repeated user interest
- measuring which generated artifacts are executed, refined, abandoned, or promoted

So the recommended model is:

- keep all artifacts durably
- maintain lightweight active pointers for runtime refinement
- build derived relation indexes from the full artifact stream
