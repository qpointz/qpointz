# Agentic Runtime v3 - Persistence Lanes

**Status:** Implemented baseline  
**Date:** March 2026  
**Scope:** Current persisted-lane architecture in `ai/mill-ai-v3` after `WI-073`, `WI-074`, and `WI-075` phase 1

## 1. Purpose

This note records the implemented persistence-lane split in `ai/v3` and the current ownership
boundaries between:

- model-facing chat memory
- routed run events
- chat transcript persistence
- artifact persistence and active pointers
- downstream artifact observers / indexing hooks

The important design rule is that these are related but distinct lanes. They are not different
views over one shared message log.

## 2. Implemented Lane Model

### Lane 3 - Model Memory

Purpose:

- preserve model-facing continuity across turns

Current implementation:

- `ChatMemoryStore`
- `ConversationMemory`
- `LlmMemoryStrategy`
- `InMemoryChatMemoryStore`
- `BoundedWindowMemoryStrategy`

Properties:

- model-facing, not chat-facing
- bounded / lossy by strategy
- separate from canonical transcript persistence

### Routed Runtime Lane

Purpose:

- normalize raw `AgentEvent` into a stable routed envelope
- classify events for downstream consumers
- drive transcript, artifact, and telemetry projections

Current implementation:

- `EventRoutingPolicy`
- `EventRoutingRule`
- `AgentEventRoutingInput`
- `RoutedAgentEvent`
- `DefaultAgentEventRouter`
- `AgentEventPublisher`

Properties:

- router is stateless
- publisher is synchronous and in-process in the first pass
- profiles can override routing policy

### Chat Transcript Lane

Purpose:

- persist canonical chat items for conversation reconstruction

Current implementation:

- `ConversationStore`
- `ConversationRecord`
- `ConversationTurn`
- `InMemoryConversationStore`

Properties:

- authority for chat reconstruction
- separate from raw event log
- separate from model memory
- one transcript turn may reference multiple artifacts

### Artifact Lane

Purpose:

- persist machine-readable outputs from runs
- support later refinement and downstream indexing

Current implementation:

- `ArtifactStore`
- `ArtifactRecord`
- `InMemoryArtifactStore`
- `ActiveArtifactPointerStore`
- `InMemoryActiveArtifactPointerStore`

Properties:

- full artifact history is append-only
- active pointers are separate from artifact history
- artifacts are durable side effects, not transcript items by default

### Telemetry Lane

Purpose:

- accumulate per-run statistics without polluting transcript persistence

Current implementation:

- `RunEventStore`
- `RunEventRecord`
- `InMemoryRunEventStore`
- `RunTelemetry`
- `RunTelemetryAccumulator`

Properties:

- routed separately from transcript
- token totals and tool-call counts accumulate in memory
- selected telemetry events are also persisted as run events

### Lane 4 - Artifact Observer Hook

Purpose:

- provide a downstream, best-effort hook after artifact persistence

Current implementation:

- `ArtifactObserver`
- `ArtifactIndexingRequest`
- `NoOpArtifactObserver`

Properties:

- invoked only after artifact persistence succeeds
- best-effort and failure-isolated
- currently logs normalized indexing requests only
- does not yet derive relations or persist indexes

## 3. Routed Event Destinations

The current router classifies events into explicit destinations:

- `CHAT_STREAM`
- `CHAT_TRANSCRIPT`
- `MODEL_MEMORY`
- `ARTIFACT`
- `TELEMETRY`

`RoutedAgentEvent` carries both:

- `category`
- `destinations`

Downstream projections should use `destinations` as the authoritative routing surface.

## 4. Current Envelope Shape

Implemented routed envelope:

```kotlin
data class RoutedAgentEvent(
    val eventId: String,
    val runtimeType: String,
    val kind: String,
    val category: RoutedEventCategory,
    val destinations: Set<RoutedEventDestination>,
    val content: Map<String, Any?>,
    val route: EventRoute,
    val conversationId: String?,
    val runId: String?,
    val profileId: String,
    val turnId: String? = null,
    val createdAt: Instant,
)
```

Important constraints:

- `runtimeType` preserves the original raw event type
- `kind` is the stable routed label
- `content` is structured and consumer-safe
- `turnId` allows transcript and artifact linking

## 5. Current Runtime Wiring

The current wiring path is:

1. agent emits raw `AgentEvent`
2. runtime builds `AgentEventRoutingInput`
3. `DefaultAgentEventRouter` produces `RoutedAgentEvent`
4. `AgentEventPublisher` publishes synchronously
5. listeners consume routed events:
   - `StandardPersistenceProjector`
   - `RunTelemetryAccumulator`

The artifact observer hook is invoked from `StandardPersistenceProjector` after
`ArtifactStore.save(...)` succeeds.

## 6. Transcript vs Memory vs Artifact

These three concerns are intentionally separate.

### Chat transcript

- canonical chat-facing history
- user turns and assistant turns
- artifact references attached to turns

### Model memory

- model-facing projection
- bounded and strategy-driven
- not authoritative for chat reconstruction

### Artifacts

- machine-readable structured outputs
- reusable by refinement and downstream consumers
- not equivalent to transcript text

## 7. Current Store Ownership Rules

- `ConversationSession`
  - live runtime/session aggregate
  - carries `conversationId`
  - not the durable transcript authority
- `ConversationStore`
  - durable chat transcript authority
- `ChatMemoryStore`
  - model-facing memory authority
- `ArtifactStore`
  - source of truth for produced machine-readable outputs
- `ActiveArtifactPointerStore`
  - latest relevant artifact lookup
- `RunEventStore`
  - selected durable run/audit events

## 8. Profile-Specific Routing

Profiles may override routing policy without changing router implementation.

Current example:

- `SchemaAuthoringAgentProfile`
  - overrides `protocol.final`
  - updates pointer key `last-schema-capture`

This is the current pattern for profile-specific persistence semantics.

## 9. Consistency Model

The first-pass implementation is intentionally simple:

- routed publishing is synchronous
- persistence projection is synchronous
- telemetry accumulation is synchronous
- artifact observers are best-effort and failure-isolated

This is sufficient for the in-memory baseline and can later evolve toward more asynchronous
delivery for downstream observers and JPA-backed adapters.

## 10. What Is Not Implemented Yet

Not part of the current baseline:

- durable relation indexing
- asynchronous observer scheduling
- SSE publication
- artifact-derived analytics
- transcript pagination or query APIs

The durable JPA-backed store adapters and Spring autoconfiguration are now in
place through `mill-ai-v3-persistence` and `mill-ai-v3-autoconfigure`.

Remaining follow-on concerns are mainly the split `PS-4a` to `PS-4f` relation
indexing work after `WI-075` phase 1.
