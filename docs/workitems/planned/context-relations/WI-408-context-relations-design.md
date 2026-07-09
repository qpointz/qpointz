# WI-408 - Context relations design

> **Story context:** [`STORY.md`](STORY.md). Open decisions: [`GAPS.md`](GAPS.md).

## Goal

Lock the **generic related-objects platform** contract: canonical object identity, relation types,
persistence/read API shape, and mill-ui integration — including how **Analysis inline chat**
regains durable host presence after reload.

## Problem

Mill hosts need to answer “what is related to the object I’m viewing?” from one place. Today:

- **Inline chat UI state** (`InlineChatContext.sessions`, `isDrawerOpen`) is **ephemeral** — lost on
  refresh; red dots and drawer binding do not survive reload.
- **Backend contextual chat** exists in `ai_chat` (`context_type`, `context_id`) but is only created
  on **first message**; there is no relation row and no auto-restore on host mount.
- **Saved queries** (`saved_query`) and contextual chats are linked only by **string convention**
  (`context_id` = query `id`), not a managed relation.
- **Related content** and **chat references** are separate mock services with duplicate
  `(contextType, contextId)` caching in React.

## Scope

### In scope

- **`ObjectRef`** — stable identity for hosts and targets (analysis query, model entity, knowledge
  concept, general/contextual chat, future types).
- **Relation types** — initial taxonomy, e.g.:
  - `has-contextual-chat` (host → contextual chat; 0..1 per user+host)
  - `related-conversation` (host → general chat)
  - `related-to` (cross-domain navigation; concept ↔ model ↔ analysis)
- **Read API** — `GET` relations from a source `ObjectRef`, filter by relation type(s).
- **Write model** — which relations are system-created vs user-created; idempotency rules.
- **Bridge to `ai_chat`** — contextual chat as relation target or mirrored index; no duplicate
  transcript store.
- **UI integration plan** — replace `ChatReferencesContext` + `RelatedContentContext` with one
  provider; **inline host restore** flow on Analysis/Model/Knowledge mount.
- **Analysis inline chat persistence gap** — document explicit requirements:
  - Restore session indicators after reload when `getChatByContext` / relation lookup finds a chat
  - Optional: persist copilot `automation.mode` (session settings) — decision in GAPS
- **Alignment** with [`concept-object-relations`](../concept-object-relations/STORY.md) and **PS-4**
  artifact relation indexers (shared read model vs separate tables).

### Out of scope (this WI)

- Implementation code
- User-facing relation editor
- Migration of production data (design only)

## Design deliverables

1. **`docs/design/platform/context-relations.md`** — normative contract (object ref, relation types,
   API, persistence, UI hooks).
2. **`GAPS.md`** — locked decisions + deferred items for implementation WIs.
3. **`STORY.md`** update — Stage 1+ WI list with dependencies after design review.
4. **Cross-link** from [`analysis-inline-chat-foundation` STORY](../../in-progress/analysis-inline-chat-foundation/STORY.md)
   noting persistence deferred here.

## Acceptance criteria

- Object ref and relation type taxonomy documented with Analysis / Model / Knowledge examples.
- Read API request/response shapes defined (OpenAPI-level sketch acceptable).
- Inline chat restore sequence documented (host mount → relation/chat lookup → `startSession` +
  hydrate).
- Explicit call-out: **analysis inline chat UI presence is not persisted** in the foundation story;
  this story owns the fix.
- Mock retirement plan for `relatedContentService` and `chatReferencesService` documented.
- No open product decisions blocking WI-409 (first implementation WI).

## Verification

- Design doc review only (no code tests for this WI).
