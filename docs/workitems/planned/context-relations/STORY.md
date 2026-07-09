# Context relations (related objects platform)

**Status:** `planned` — design gate only; no implementation WIs yet beyond WI-408.

**Backlog:** [U-19](../BACKLOG.md)  
**Branch (when started):** `feat/context-relations` from `origin/dev`  
**Milestone:** TBD (add to `MILESTONE.md` on story start)

**Normative design (target):** `docs/design/platform/context-relations.md` (to be created in WI-408)  
**Open decisions:** [`GAPS.md`](GAPS.md)

---

## Goal

Introduce a **generic, centrally managed** model for **related objects** across Mill hosts (Analysis,
Model, Knowledge, General Chat): durable relations, a unified read API, and a single UI integration
layer — replacing ad hoc mock services and ephemeral React caches.

This story is **platform / mill-ui infrastructure**. It does **not** implement Analysis copilot
artifact strips, copilot automation, or inline drawer UX (those stay in
[`analysis-inline-chat-foundation`](../completed/20260709-analysis-inline-chat-foundation/STORY.md)).

---

## Problem statement

Today “what is related to this object?” is fragmented:

| Concern | Current implementation | Persisted? |
|---------|------------------------|------------|
| Inline chat bound to a host | `InlineChatContext` (in-memory) + `ai_chat.context_type/context_id` | Chat **transcript** yes (after first message); **UI presence** (session list, red dot, drawer) **no** |
| General chats linked to a host | `ChatReferencesContext` + `chatReferencesService` | **Mock only** |
| Cross-domain links (model ↔ concept ↔ analysis) | `RelatedContentContext` + `relatedContentService` | **Mock only** |
| Concept ↔ object relations | [`concept-object-relations`](../planned/concept-object-relations/STORY.md) (planned, narrow domain) | Event projection (not started) |
| Saved query ↔ inline chat | Convention: `context_id` = saved query `id` — **no FK, no relation row** | Partial / implicit |

### Analysis inline chat — persistence gap (explicit)

The [`analysis-inline-chat-foundation`](../completed/20260709-analysis-inline-chat-foundation/STORY.md) story
delivers copilot UX and host binding, but **does not** persist:

- Whether an inline session **exists** for the current host after a full page reload
- Drawer open/closed preference (except split width in `localStorage`)
- Per-session copilot settings (`automation.mode`) across reloads
- A first-class **relation** from a saved query to its contextual chat (only implicit
  `ai_chat.context_id` after the user sends a first message)

**Follow-up for this story:** restore host-bound inline presence from durable relations + existing
`getChatByContext`, and unify “related conversations / related content” under one registry.

---

## Scope

### In scope (story arc)

- Canonical **`ObjectRef`** identity (`domain`, `type`, `id`, optional `subtype`, `label`)
- Typed **relations** (`has-contextual-chat`, `related-conversation`, `related-to`, …)
- Durable **read model** + REST API for relations from a source object
- **UI provider** replacing parallel `ChatReferencesContext` / `RelatedContentContext` caches
- **Inline chat restore** on host mount (Analysis, Model, Knowledge)
- Bridge to existing `ai_chat` contextual binding (no duplicate chat stores)
- Design alignment with [`concept-object-relations`](../planned/concept-object-relations/STORY.md)
  and backlog items **PS-4a–PS-4f** (artifact-derived relations)

### Out of scope (initial slices)

- User-authored relation editing UI (beyond system-created edges)
- Full global search / vector graph
- Replacing `saved_query` catalog or schema relation facets
- mill-grinder-ui (legacy)

---

## Relationship to other stories

| Story | Relationship |
|-------|----------------|
| [`analysis-inline-chat-foundation`](../completed/20260709-analysis-inline-chat-foundation/STORY.md) | Defers **relation persistence + restore** here; keeps copilot behavior |
| [`concept-object-relations`](../planned/concept-object-relations/STORY.md) | Domain-specific **concept↔object** pipeline; should emit/consume shared relation model |
| [`artifact-publish-validation`](../planned/artifact-publish-validation/STORY.md) | Orthogonal (publish gate vs object graph) |
| Backlog **PS-4a–PS-4f** | Artifact/run → object edges; index into same relation read model when ready |

---

## Work Items

### Stage 0 — Design gate

| Stage | WI | Scope |
|-------|-----|--------|
| 0 — Design | WI-408 | Object ref + relation taxonomy, API sketch, persistence strategy, UI integration plan |

- [ ] WI-408 — Context relations design (`WI-408-context-relations-design.md`)

### Stage 1+ (TBD after WI-408)

Planned themes (numbers assigned at design lock):

- Contract module + relation types
- Persistence + read API
- Contextual-chat relation writer (bridge `createChat` / `ai_chat`)
- Unified `useObjectRelations` mill-ui provider
- Inline host restore + badge migration
- Tests + public/design docs

---

## Verify (when implemented)

```bash
cd ui/mill-ui && npm run test -- --run
./gradlew :ai:mill-ai-service:test :ai:mill-ai-persistence:testIT
```

Manual:

- Open Analysis query with prior inline chat → reload → drawer/session indicators restore without
  re-clicking inline chat
- Model entity shows related concepts / analyses from API (not mock hash)
- General Chat conversations linked to a host appear in drawer popover from durable data

---

## Assumptions

- Contextual chat remains **one chat per `(userId, contextType, contextId)`** (`UnifiedChatService`);
  this story adds an explicit **relation projection**, not a second chat store.
- Mock `relatedContentService` / `chatReferencesService` are **retired** once read API exists.
- WI numbering continues from **408**; implementation WIs start at **409** after design approval.
