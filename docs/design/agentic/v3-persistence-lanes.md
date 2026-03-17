# Agentic Runtime v3 - Persistence Lanes

**Status:** Draft  
**Date:** March 17, 2026  
**Scope:** Detailed persistence architecture for `ai/v3`

## 1. Purpose

This note turns the persistence discussion into an implementation-oriented architecture for
`ai/v3`.

The design is organized around three main persistence lanes:

- Lane 3 — chat memory persistence
- Lanes 1,2 — routed event / conversation / artifact persistence
- Lane 4 — artifact observers, relation indexing, and derived analytics

These lanes have different consumers, consistency requirements, and ownership boundaries.

## 2. Core Persistence Model

The runtime should treat the following concerns separately:

- state
  - runtime working state for the active run/session
  - ephemeral, runtime-owned
- chat memory
  - model-facing session continuity
  - durable as needed, LangChain4j-oriented
- conversation persistence
  - user/consumer-facing transcript and run record
  - runtime-owned
- artifacts
  - machine-readable outputs such as generated SQL, validation results, result references,
    authored metadata captures, and value mappings
  - reusable across turns and by downstream consumers

The key principle is:

- not everything durable belongs in chat memory
- not everything persistent belongs in the conversation transcript
- not everything produced should be injected back into model context

## 3. Lanes Overview

### 3.1 Lane 3 — Chat Memory Persistence

Purpose:

- preserve model-facing context continuity across requests/sessions

Primary characteristics:

- model-optimized
- token-budget bounded
- may be lossy or summarized
- should remain Spring-free at the core contract level

Primary consumers:

- LangChain4j / planner / answer synthesis path

### 3.2 Lanes 1,2 — Event / Conversation / Artifact Persistence

Purpose:

- persist what materially happened
- persist what was said
- persist what was produced

This is the main runtime-owned durability lane and should be implemented first.

Primary characteristics:

- runtime-optimized
- append-heavy
- low-latency critical path with eventual-consistency-friendly downstream projections

Primary consumers:

- conversation UX
- run inspection
- follow-up query refinement
- future SSE/event consumers

### 3.3 Lane 4 — Artifact Observers, Relations, Analytics

Purpose:

- derive related-object links
- index artifacts against model/domain objects
- enable downstream analytics and future artifact RAG

Primary characteristics:

- asynchronous
- best-effort
- rebuildable from source-of-truth stores

Primary consumers:

- `mill-ui` related views
- analytics
- artifact-derived RAG or summaries

## 4. Lane 3 - Chat Memory Persistence

### 4.1 Responsibility

Lane 3 stores the model-facing memory required to continue a conversation coherently.

It is not:

- the full UX transcript
- the full event log
- the full artifact store

It is a projection optimized for model reasoning.

### 4.2 Core Contracts

Suggested core interfaces:

```kotlin
interface ChatMemoryStore {
    fun load(conversationId: String): ConversationMemory?
    fun save(memory: ConversationMemory)
    fun clear(conversationId: String)
}

fun interface LlmMemoryStrategy {
    fun project(input: MemoryProjectionInput): List<ConversationMessage>
}
```

Suggested types:

- `ConversationMemory`
- `MemoryProjectionInput`
- `ConversationMessage`

### 4.3 Implementation Guidance

First round:

- in-memory store
- bounded recent-history strategy
- no mandatory summarization yet

Likely first adapters:

- `InMemoryChatMemoryStore`
- optional Caffeine-backed implementation for bounded retention

Later:

- JPA-backed adapter in Spring integration module
- summarization buffer strategy

### 4.4 Interaction with Artifacts

Artifacts should not be loaded wholesale into chat memory.

Instead:

- runtime/planner selects relevant artifacts
- memory strategy projects compact summaries of those artifacts into model context

This is one of the main long-conversation advantages of `v3`.

## 5. Lanes 1,2 - Routed Events, Conversation, and Artifacts

### 5.1 Responsibility

This lane establishes the durable runtime record and the reusable structured outputs of a run.

It should handle:

- routed event propagation
- selected event persistence
- transcript projection
- artifact creation and persistence
- active artifact pointers for follow-up/refinement

### 5.2 Event Routing

Raw `AgentEvent` is a runtime stream, not the canonical persisted conversation record.

Profiles should declare event-routing policy, while runtime applies it.

Suggested model:

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

### 5.3 Routed Event Envelope

The routed event should be consumer-agnostic and structured.

Suggested envelope:

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

This envelope should support:

- consumer rendering
- persistence as JSON
- later SSE publication

### 5.4 Event Propagation Facility

The runtime should expose publisher/listener style propagation:

```kotlin
interface AgentEventPublisher {
    fun publish(event: RoutedAgentEvent)
    fun register(listener: AgentEventListener)
}

fun interface AgentEventListener {
    fun onEvent(event: RoutedAgentEvent)
}
```

Example listeners:

- CLI listener
- persistence listener
- artifact extraction listener
- future SSE listener
- debug logging listener

### 5.5 Conversation Persistence

Conversation persistence should capture canonical user/assistant turns.

Suggested contracts:

```kotlin
interface ConversationStore {
    fun appendTurn(conversationId: String, turn: ConversationTurn)
    fun load(conversationId: String): ConversationRecord?
}
```

Suggested types:

- `ConversationRecord`
- `ConversationTurn`

At minimum, persisted turn history should contain:

- user message
- final assistant answer
- timestamps
- run reference

### 5.6 Run Event Persistence

Run events should be persisted separately from transcript.

Suggested contracts:

```kotlin
interface RunEventStore {
    fun append(event: PersistedRunEvent)
    fun listByConversation(conversationId: String): List<PersistedRunEvent>
}
```

This gives audit/debug visibility without polluting canonical conversation turns.

### 5.7 Artifact Persistence

Artifacts should be durable, append-only, and reusable.

Suggested contracts:

```kotlin
interface ArtifactStore {
    fun save(artifact: RunArtifact)
    fun listByConversation(conversationId: String): List<RunArtifact>
    fun listByConversationAndType(conversationId: String, artifactType: String): List<RunArtifact>
}
```

Suggested artifact examples:

- `GeneratedSqlArtifact`
- `SqlValidationArtifact`
- `SqlResultArtifact`
- `ValueMappingArtifact`
- `MetadataCaptureArtifact`

### 5.8 Active Artifact Pointers

Runtime refinement often needs the latest relevant artifact rather than the whole history.

So distinguish:

- full artifact history
- active artifact pointers such as:
  - last generated SQL
  - last SQL result
  - last capture artifact

Suggested contract:

```kotlin
interface ActiveArtifactPointerStore {
    fun put(pointer: ActiveArtifactPointer)
    fun find(conversationId: String, role: String): ActiveArtifactPointer?
}
```

### 5.9 Planner Interaction

Planner should decide when a prior artifact is relevant for the current user input.

Typical refinement flow:

1. runtime retrieves bounded candidate artifacts
2. planner selects desired artifact role/reference
3. artifact projector injects compact summary into model context
4. refined artifact is generated and persisted

### 5.10 Consistency and Latency

This lane should favor:

- immediate runtime state updates
- asynchronous durable persistence where possible

Synchronous path should stay minimal.

Asynchronous side effects are acceptable for:

- event log persistence
- artifact durability if immediate cross-process resume is not required
- transcript projections when low latency is more important than immediate durability

## 6. Lane 4 - Artifact Observers, Relations, and Analytics

### 6.1 Responsibility

Lane 4 consumes artifacts after they are produced and derives additional value from them.

This lane should not influence:

- planner correctness
- prompt construction
- core chat execution

### 6.2 Artifact Observer

Suggested contract:

```kotlin
fun interface ArtifactObserver {
    fun onArtifactCreated(artifact: RunArtifact, context: ArtifactContext)
}
```

This observer can be implemented asynchronously and best-effort.

### 6.3 Relation Indexing

Suggested derived relation model:

```kotlin
data class ArtifactRelation(
    val sourceType: String,
    val sourceId: String,
    val relationType: String,
    val targetType: String,
    val targetId: String,
)
```

Example relations:

- conversation -> table
- artifact -> table
- artifact -> column
- artifact -> metadata entity

This supports `mill-ui` related-object views such as:

- table -> related chats
- object -> related generated SQL
- object -> related authoring proposals

### 6.4 Artifact Analytics and RAG

Because artifacts are structured and domain-linked, they are a strong basis for:

- offline analytics
- "most interesting objects" summaries
- future artifact-derived RAG documents

The correct pattern is:

- keep artifacts as source of truth
- derive RAG/index documents from artifacts
- keep derived indexes rebuildable

## 7. Injection and Implementation Rules

### 7.1 Repositories/stores must be injected

Stores should be injected at agent/runtime instance level.

`v3-core` should depend only on interfaces, never on hardcoded implementations.

This allows:

- in-memory stores now
- Spring/JPA stores later
- test doubles easily

### 7.2 In-memory first

The first implementation round should use:

- in-memory canonical stores
- optional Caffeine-backed caches/indexes

Good first candidates:

- `InMemoryConversationStore`
- `InMemoryRunEventStore`
- `InMemoryArtifactStore`
- `InMemoryChatMemoryStore`

Caffeine is best reserved for:

- active session cache
- active artifact pointers
- memory windows
- derived recent indexes

### 7.3 Future Spring/JPA adapters

Spring/JPA should live in adapter modules only.

Suggested structure:

- `ai/v3-core`
  - ports, domain types, routing logic
- future adapter module
  - JPA entities
  - Spring repositories
  - transactional store adapters

## 8. Recommended Work Item Split

### WI A - Lane 3

- durable chat memory store
- LangChain4j-oriented memory projection integration
- in-memory first implementation

### WI B - Lanes 1,2

- event routing policy
- routed event publisher/listener facility
- conversation persistence
- run event persistence
- artifact persistence
- active artifact pointers
- groundwork for future SSE consumers

### WI C - Lane 4

- artifact observer
- relation indexing
- related-object lookups
- first artifact-driven analytics groundwork

## 9. Recommended Delivery Order

Recommended implementation order:

1. Lanes 1,2
2. Lane 3
3. Lane 4

Reason:

- lanes 1,2 establish canonical durable record and artifact base
- lane 3 can then project from a stable persisted model
- lane 4 depends on artifact existence and is downstream
