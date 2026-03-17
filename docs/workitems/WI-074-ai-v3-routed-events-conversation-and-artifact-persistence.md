# WI-074 - AI v3 Routed Events, Conversation, and Artifact Persistence

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `A-65`

## Problem Statement

`ai/v3` emits runtime events and produces structured outputs, but it lacks a durable persistence
lane for:

- selected routed events
- canonical conversation turns
- machine-readable artifacts
- active artifact pointers for follow-up/refinement

The runtime also needs an event propagation facility that can later support:

- persistence
- CLI consumers
- future SSE publishing

without hardcoding those concerns into the executor.

This WI should build on the central persistence foundation introduced in `WI-073a`.

## Goal

Implement the main durable runtime persistence lane for `ai/v3`:

- routed event policy and propagation
- conversation transcript persistence
- artifact persistence
- active artifact pointer tracking

## Scope

In scope:

- `EventRoutingPolicy` and related core routing model
- `RoutedAgentEvent` envelope with:
  - `runtimeType`
  - `kind`
  - `content`
- publisher/listener propagation facility
- `RunEventStore`
- `ConversationStore`
- `ArtifactStore`
- `ActiveArtifactPointerStore`
- in-memory first implementations

Out of scope:

- central persistence module bootstrap
- LangChain4j chat memory persistence
- relation indexing and analytics
- full HTTP/SSE transport implementation

## Design Requirements

- The router must distinguish transient events from durable events.
- Profiles should declare routing policy; runtime should apply it.
- Routed events must be layer-agnostic and suitable for multiple consumers.
- The propagated event envelope must expose:
  - stable `runtimeType`
  - stable routed `kind`
  - structured `content`
- Repositories/stores must be injected, not hardcoded.
- The first round should support in-memory repositories and later shared persistence adapters.
- The same routed event pipeline should be usable later for SSE publication.

## Primary Contracts

Suggested types/interfaces:

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

data class RoutedAgentEvent(
    val runtimeType: String,
    val kind: String,
    val content: Map<String, Any?>,
    val route: EventRoute,
    val conversationId: String?,
    val runId: String?,
    val profileId: String,
)

interface AgentEventPublisher {
    fun publish(event: RoutedAgentEvent)
    fun register(listener: AgentEventListener)
}

fun interface AgentEventListener {
    fun onEvent(event: RoutedAgentEvent)
}
```

## Implementation Outline

1. Introduce event-routing policy on `AgentProfile`.
2. Implement router from raw `AgentEvent` to `RoutedAgentEvent`.
3. Introduce publisher/listener facility in `ai/v3-core`.
4. Add persistence ports:
   - `RunEventStore`
   - `ConversationStore`
   - `ArtifactStore`
   - `ActiveArtifactPointerStore`
5. Implement in-memory versions.
6. Wire listeners for:
   - persisted run events
   - transcript projection
   - artifact creation/persistence
7. Ensure artifacts are kept both as:
   - full durable history
   - active pointers such as `last-sql`

## Important Behavior

- Persist the full artifact history, not only the latest artifact.
- Maintain active pointers separately for refinement workflows.
- Do not treat the raw `AgentEvent` stream as the canonical transcript.
- Use routed events as the common propagation surface for later SSE support.

## Testing Strategy

- unit tests for router behavior by profile
- unit tests for routed event envelope mapping
- unit tests for in-memory stores
- integration-style tests showing:
  - routed events are published
  - durable transcript entries are projected
  - artifacts are persisted and latest pointers updated

## Acceptance Criteria

- Routed event policy exists and is profile-driven.
- The runtime publishes a structured routed event envelope.
- In-memory stores exist for events, transcript, artifacts, and active artifact pointers.
- Canonical conversation turns are persisted separately from event logs.
- Full artifact history is retained, while latest relevant pointers remain addressable.
- The event propagation design is suitable for later SSE consumers without redesign.
- The implementation is wired through the shared persistence-module foundation from `WI-073a`.
