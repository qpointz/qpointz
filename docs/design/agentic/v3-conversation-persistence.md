# Agentic Runtime v3 - Conversation Persistence

**Status:** Implemented baseline  
**Date:** March 2026  
**Scope:** Current conversation, memory, and artifact persistence split in `ai/mill-ai-v3`

## 1. Summary

`ai/v3` now treats conversation persistence as three separate concerns:

- chat transcript
- model memory
- machine-readable artifacts

This separation is deliberate and is the core persistence decision behind `WI-073` and `WI-074`.

## 2. Current Durable and Live Surfaces

### ConversationSession

`ConversationSession` is the live runtime/session object.

It is used for:

- current `conversationId`
- live in-process message list used by the active runtime/CLI path

It is not the canonical durable transcript.

### ConversationStore

`ConversationStore` is the canonical durable chat transcript.

Current shape:

```kotlin
data class ConversationTurn(
    val turnId: String,
    val role: String,
    val text: String? = null,
    val artifactIds: List<String> = emptyList(),
    val createdAt: Instant,
)
```

This is the authority for conversation reconstruction.

### ChatMemoryStore

`ChatMemoryStore` is model-facing memory.

It is used for:

- bounded prior context
- strategy-driven memory projection
- future compaction/summarization oriented behavior

It is not the chat transcript.

### ArtifactStore

`ArtifactStore` persists structured outputs such as:

- generated SQL artifacts
- SQL result-reference artifacts
- schema-authoring capture artifacts
- future chart or analysis artifacts

Artifacts are durable side effects attached to a conversation and optionally to a transcript turn.

## 3. Current Ownership Rules

- `ConversationSession`
  - live runtime/session state only
- `ConversationStore`
  - canonical chat transcript
- `ChatMemoryStore`
  - model-facing continuity
- `ArtifactStore`
  - source of truth for machine-readable outputs
- `ActiveArtifactPointerStore`
  - latest relevant artifact lookup for refinement workflows

## 4. Why Transcript and Memory Are Separate

The current implementation follows these rules:

- transcript is chat-facing and complete enough to reconstruct the conversation
- memory is model-facing and may be bounded or lossy
- transcript entries do not need to mirror all memory content
- memory does not need to carry all transcript metadata

This keeps:

- chat reconstruction
- model prompting
- artifact reuse

as distinct concerns.

## 5. Current Transcript Projection Rules

The transcript is not built from the raw event stream directly.

Instead:

1. raw `AgentEvent` is routed to `RoutedAgentEvent`
2. routing policy decides whether an event contributes to transcript
3. `StandardPersistenceProjector` appends canonical turns to `ConversationStore`

Current defaults:

- user input is appended explicitly by the runtime at the start of each turn
- `answer.completed` creates assistant transcript turns
- intermediate deltas and telemetry do not become transcript turns

Examples of events that do not become transcript history by default:

- `message.delta`
- `reasoning.delta`
- `tool.call`
- `tool.result`
- `llm.call.completed`

## 6. Current Artifact Linking Model

Artifacts and transcript turns are linked through `turnId`.

First-pass behavior:

- one transcript turn may reference multiple artifacts
- one artifact belongs to at most one owning transcript turn

The current in-memory path supports both orders:

- artifact persisted before transcript turn
- transcript turn persisted before artifact

This is done by:

- storing `turnId` on `ArtifactRecord`
- storing `artifactIds` on `ConversationTurn`
- allowing `ConversationStore.attachArtifacts(...)` after transcript creation

This supports the required multi-artifact single-response pattern, for example:

- one assistant turn
- one `generated-sql` artifact
- one `chart-config` artifact

## 7. Current Runtime Behavior

Both `LangChain4jAgent` and `SchemaExplorationAgent` now:

- generate a `runId` per `run()` call
- ensure the conversation exists in `ConversationStore`
- append a user transcript turn at run start
- route runtime events through the shared router/publisher
- persist assistant transcript turns and artifacts through the projector

On capture/protocol flows:

- artifacts may be produced without a normal assistant text answer
- the runtime still emits an empty `answer.completed`
- the transcript turn may therefore have `text = null` and attached artifacts

That is the current artifact-oriented chat item shape.

## 8. Current Memory Behavior

The memory path remains intentionally simpler than the transcript path.

Current shape:

- `ConversationMemory`
- `ConversationMessage`
- `ChatMemoryStore`
- `BoundedWindowMemoryStrategy`

Current runtime behavior:

- user and assistant messages are saved into `ChatMemoryStore`
- empty assistant capture-path turns are not saved into memory
- memory projection remains independent from transcript persistence

This means transcript and memory can diverge intentionally.

## 9. Current Event Categories

From the conversation-persistence perspective, the important routed destinations are:

- `CHAT_STREAM`
- `CHAT_TRANSCRIPT`
- `MODEL_MEMORY`
- `ARTIFACT`
- `TELEMETRY`

Examples:

- `answer.completed`
  - `CHAT_STREAM`, `CHAT_TRANSCRIPT`, `MODEL_MEMORY`
- `protocol.final`
  - `CHAT_STREAM`, `ARTIFACT`
- `llm.call.completed`
  - `TELEMETRY`

This keeps the transcript free from low-level runtime noise.

## 10. Current Limitations

The current baseline does not yet provide:

- durable JPA-backed transcript storage
- paginated transcript queries
- relation indexing over transcript/artifact data
- transcript-side metadata beyond `role`, `text`, and `artifactIds`
- transcript update semantics beyond artifact attachment

Those are deliberate follow-on steps rather than missing pieces of the basic split.
