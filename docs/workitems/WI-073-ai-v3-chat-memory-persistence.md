# WI-073 - AI v3 Chat Memory Persistence

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Backlog refs: `A-64`

## Problem Statement

`ai/v3` currently has in-memory conversation continuity for CLI-style sessions, but it does not
yet have a durable, explicit chat-memory persistence model suitable for:

- multi-request session continuity
- bounded LLM memory projection
- later summary/window strategies

This work should build on the central persistence foundation introduced in `WI-073a` rather than
creating persistence bootstrap logic ad hoc inside `ai` modules.

## Goal

Implement a first persistence lane for model-facing chat memory in `ai/v3` that:

- remains Spring-free in `v3-core`
- uses injectable storage ports
- supports in-memory adapters first
- is compatible with LangChain4j integration

## Scope

In scope:

- `ChatMemoryStore` port in `ai/v3-core`
- core `ConversationMemory` model
- `LlmMemoryStrategy` extraction and injection
- in-memory memory store implementation
- runtime wiring so agent instances receive the store via dependencies

Out of scope:

- central persistence module bootstrap
- transcript persistence
- routed event persistence
- artifact persistence
- relation indexing
- advanced summarization LLM flows

## Design Requirements

- Chat memory must remain distinct from conversation transcript.
- Chat memory must remain distinct from artifact persistence.
- The core runtime must not depend on Spring or JPA types.
- The first implementation should favor in-memory repositories, with optional Caffeine support for
  bounded retention.
- The runtime should support later replacement with persistence adapters from the shared
  persistence module without changing core contracts.

## Primary Contracts

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

## Implementation Outline

1. Define `ConversationMemory` and `MemoryProjectionInput` in `ai/v3-core`.
2. Extract and standardize `LlmMemoryStrategy`.
3. Update the runtime/agent construction path to accept `ChatMemoryStore` and `LlmMemoryStrategy`
   as injected dependencies.
4. Implement `InMemoryChatMemoryStore`.
5. Add tests proving:
   - memory survives multiple turns within a session
   - strategy projection is applied consistently
   - stores are injected and not hardcoded

## Testing Strategy

- unit tests for `InMemoryChatMemoryStore`
- unit tests for memory strategy behavior
- integration-style tests for multi-turn continuity with injected store

## Acceptance Criteria

- `ai/v3-core` exposes a Spring-free chat-memory persistence port.
- Agent/runtime instances accept injected chat-memory collaborators.
- An in-memory implementation exists and is used in tests/dev flows.
- Multi-turn continuity works through the store abstraction rather than only ad hoc session lists.
- The implementation is wired through the shared persistence-module foundation from `WI-073a`.
