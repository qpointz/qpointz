# Agentic Runtime v3 - Chat Service and API

**Status:** Implemented baseline  
**Date:** March 2026  
**Scope:** Unified persisted chat resource, runtime rehydration, SSE presentation stream, and
HTTP service facade in `ai/mill-ai-v3-service`

## 1. Summary

`ai/v3` now exposes a unified chat stack with four distinct layers:

- chat metadata persistence
- transcript and memory persistence
- runtime rehydration from persisted chat metadata
- HTTP/SSE chat service facade

The result is one canonical backend chat resource for both:

- general chats
- context-bound chats

## 2. Module Split

Current ownership is:

```text
ai/
  mill-ai-v3                # ports, runtime contracts, in-memory stores, SSE event model
  mill-ai-v3-persistence    # JPA entities/repositories/adapters for chat metadata
  mill-ai-v3-autoconfigure  # AI v3 runtime and chat runtime bean wiring
  mill-ai-v3-service        # HTTP/SSE controller, ChatService, DTOs, exception advice

persistence/
  mill-persistence          # centralized Flyway migrations
```

Key rule:

- transport lives in `mill-ai-v3-service`
- persistence adapters live in `mill-ai-v3-persistence`
- schema history stays centralized in `mill-persistence`

## 3. Chat Resource Model

`ai/v3` now treats chat metadata as a first-class persisted resource separate from transcript
turns.

Current persisted metadata includes:

- `chatId`
- `userId`
- `profileId`
- `chatName`
- `chatType`
- `isFavorite`
- `contextType?`
- `contextId?`
- `contextLabel?`
- `contextEntityType?`
- `createdAt`
- `updatedAt`

Current persistence rules:

- `chatId == conversationId`
- general chats are listed in the main chat list
- contextual chats are singleton per `(userId, contextType, contextId)`
- contextual lookup is based on persisted metadata, not transcript inference

Current ownership split:

- `ChatRegistry`
  - chat identity
  - ownership
  - favorites and naming
  - context binding
  - runtime reconstruction metadata
- `ConversationStore`
  - durable transcript turns
- `ChatMemoryStore`
  - model-facing bounded memory

## 4. Runtime Rehydration

Persisted chat metadata is now sufficient to reopen an existing chat and reconstruct the
intended runtime identity.

Current rehydration rules:

- persisted `profileId` is authoritative
- context metadata is durable and participates in runtime reconstruction
- transcript is loaded separately from metadata
- runtime selection does not depend on transcript-only inference

The current implementation uses a compile-time `DefaultProfileRegistry`.

Accepted current limitation:

- if new profiles are introduced without updating the registry, rehydration can drift

Deferred follow-up:

- replace the compile-time profile registry with a dynamic or Spring-managed registry once
  profile growth makes manual registration too fragile

## 5. HTTP Service Boundary

The unified chat service is exposed from `ai/mill-ai-v3-service`.

Current service pattern:

- `AiChatController`
  - thin HTTP/SSE transport layer
  - OpenAPI documented directly on the controller
  - depends on `ChatService`
- `ChatService`
  - primary service boundary for the chat API
- `UnifiedChatService`
  - current orchestration implementation

Current API surface:

- `GET /api/v1/ai/chats`
- `POST /api/v1/ai/chats`
- `GET /api/v1/ai/chats/{chatId}`
- `PATCH /api/v1/ai/chats/{chatId}`
- `GET /api/v1/ai/chats/{chatId}/messages`
- `POST /api/v1/ai/chats/{chatId}/messages`
- `DELETE /api/v1/ai/chats/{chatId}`
- `GET /api/v1/ai/chats/context-types/{contextType}/contexts/{contextId}`

Current API semantics:

- `GET /api/v1/ai/chats` returns general chats only
- contextual chat create reuses an existing singleton
- `POST /api/v1/ai/chats` returns:
  - `201` when a new chat is created
  - `200` when an existing contextual chat is reused
- `POST /api/v1/ai/chats/{chatId}/messages` returns:
  - `404` before the stream opens if the chat does not exist
  - `200` once the stream is opened; runtime failures after stream start become in-stream
    failure events

## 6. SSE Presentation Model

The public streaming model is presentation-oriented rather than agent-lifecycle-oriented.

Current event types:

- `item.created`
- `item.part.updated`
- `item.completed`
- `item.failed`

Current envelope fields:

- `eventId`
- `chatId`
- `itemId`
- `sequence`
- `type`
- `timestamp`
- `payload`

Current v1 semantics:

- `itemId` identifies one logical assistant output item
- `presentation = conversation`
- `partType = text`
- `mode = append` for streamed text deltas
- `item.completed.content` is nullable
- `item.failed` carries both stable `code` and human-readable `reason`

This keeps the stream suitable for:

- current `mill-ui`
- future CLI consumers
- later structured item extensions such as `sql`, `data`, or `chart`

## 7. Error Handling Pattern

The chat service adopts the repository-wide REST error-handling direction documented in:

- [platform/rest-exception-handling-pattern.md](../platform/rest-exception-handling-pattern.md)

Current pattern:

- controllers stay thin
- services throw semantic status exceptions
- local HTTP advice maps them to REST responses

Deferred platform follow-up:

- extract one shared Spring web module for reusable REST advice and standard error payloads

## 8. Current Limitations and Deferred Work

Implemented baseline does not yet include:

- `mill-ui` migration to the v3 chat API
- structured assistant item parts beyond plain text
- shared Spring web advice module extraction
- dynamic profile registry

These remain explicit follow-up items rather than accidental gaps.

## 9. Related Documents

| Document | Purpose |
|---|---|
| [v3-conversation-persistence.md](./v3-conversation-persistence.md) | Transcript, memory, and artifact persistence split |
| [../persistence/persistence-overview.md](../persistence/persistence-overview.md) | Cross-domain persistence ownership |
| [../platform/rest-exception-handling-pattern.md](../platform/rest-exception-handling-pattern.md) | Reusable REST exception/status pattern |
