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

**Profile definitions (not persisted):** There is **no** JPA entity or repository for **`AgentProfile`**. Persistence stores only **`profileId: String`** on chat metadata. The **`ProfileRegistry`** contract (**`resolve`**, **`registeredProfiles`**) is the **source of truth** for which ids are valid and how **`AgentProfile`** (capability ids, routing) is assembled.

The current implementation uses a compile-time **`DefaultProfileRegistry`** (Kotlin **`HelloWorldAgentProfile`**, **`SchemaExplorationAgentProfile`**, **`SchemaAuthoringAgentProfile`**, etc.) registered in one place. **Maintenance:** add or change profiles by updating those definitions and the registry list (or supply a custom **`ProfileRegistry`** `@Bean`, e.g. **`MapProfileRegistry`**). **`GET /api/v1/ai/profiles`** returns whatever the active **`ProfileRegistry`** bean exposes.

Accepted current limitation:

- if a chat row references a **`profileId`** that is later removed from the registry, **rehydration** returns **`null`** for the profile and behavior is degraded — avoid removing ids without a migration story
- if new profiles are introduced without updating the registry, discovery and create-time validation can drift

Deferred follow-up:

- optional **DB- or config-backed** profile catalog and a **`ProfileRegistry`** implementation that reads it (admin API, versioning) — separate story; chat rows would still store **`profileId`** as today
- replace or supplement the compile-time **`DefaultProfileRegistry`** with a dynamic or Spring-managed registry once profile growth makes manual registration too fragile

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
- `GET /api/v1/ai/profiles` — list registered agent profiles (from **`ProfileRegistry`**)
- `GET /api/v1/ai/profiles/{profileId}` — inspect one profile

For **`GET /api/v1/ai/profiles/{profileId}`**, an unknown **`profileId`** returns **`404`** with a **`MillStatusDetails`** JSON body (same error style as unknown chat **`404`**, via **`MillStatuses`** / the service exception handler).

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

Current event types (see `ChatSseEvent` in `ai/mill-ai-v3`):

- `item.created`
- `item.diagnostic` — UX status before the reply completes (`code`, `message`, optional `detail`; e.g. run start, planning, reasoning hints)
- `item.part.updated`
- `item.tool.call` / `item.tool.result` — optional structured tool progress (advanced clients / debug)
- `item.completed`
- `item.failed`

Common envelope fields (per subtype; serialized as one JSON object per SSE `data:` line):

- `eventId`
- `chatId`
- `itemId`
- `sequence`
- `type`
- `timestamp`
- plus type-specific fields (e.g. `content` on `item.part.updated`, `code`/`message`/`detail` on `item.diagnostic`)

Current v1 semantics:

- `itemId` identifies one logical assistant output item
- `presentation = conversation`
- `partType = text` for streamed answer text
- `mode = append` for streamed text deltas
- `item.completed.content` is nullable
- `item.failed` carries both stable `code` and human-readable `reason`

This keeps the stream suitable for:

- current `mill-ui`
- CLI / HTTP test bench consumers (`mill-ai-v3-cli`)
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
