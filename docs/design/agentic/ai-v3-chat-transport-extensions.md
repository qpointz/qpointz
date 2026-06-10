# AI v3 chat — transport extensions & artefacts (design)

This note defines **extension points** for turning streamed assistant output into **durable chat artefacts** (SQL text, result metadata, charts, metadata facet proposals) without breaking the **V1 conversational text** path.

**Normative scope for chat-bound metadata facets** (promotion UX, merge precedence, lifecycle) lives in [`ai-v3-chat-metadata-scope.md`](./ai-v3-chat-metadata-scope.md) (**WI-233**). This document only **cross-links** that material so facet rules are not duplicated here.

## Baseline contract

- **Wire model:** [`ChatSseEvent`](../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) JSON in SSE `data:` rows, discriminated by `type`.
- **V1 frozen path:** `presentation = "conversation"` and `partType = "text"` on `item.part.updated` — mill-ui main bubble appends `content` with `mode` (`append` / future `replace` / `patch`).
- **Forward compatibility:** unknown `presentation` / `partType` combinations **must not** abort the stream for thin text UIs; they are **no-ops** for the main bubble and may be forwarded to an **extension hook** (see `src/types/chatTransport.ts` in mill-ui).
- **Correlation:** all events for one logical assistant item share the same `itemId` (and ordered `sequence`).

## Live stream vs durable replay

| Phase | Source | Consumer responsibility |
|-------|--------|-------------------------|
| **Live SSE** | `POST /api/v1/ai/chats/{id}/messages` | Parse `data:` JSON; accumulate text deltas; extension reducers for structured parts (`onNonTextPartUpdated`) and end-of-turn hints (`onItemCompleted`). |
| **Replay** | `GET /api/v1/ai/chats/{id}` → `TurnResponse` list | Durable `text` per turn; optional `assistantReplyView` (`conversation` \| `sql-primary` \| `facet-primary`) for mill-ui layout when persistence populates it. **Future:** artefact payloads or ids alongside turns for full parity with live SSE. |

**Design intent:** the **logical sequence** of `item.*` events should be **reconstructible** from durable storage so a reconnecting client or audit view can render the same artefact timeline as the live stream.

## Per-reply views (mill-ui) — extension guide

mill-ui routes assistant chrome similarly to legacy mill-grinder-ui **intent-based** layouts (`resultIntent`), but the v3 stack uses **SSE parts + an optional end-of-turn summary**, not a REST `resultIntent` field on each message.

### Concepts

| Layer | Purpose |
|-------|---------|
| **`ChatMessageArtifact`** (`ui/mill-ui/src/types/chat.ts`) | Normalised structured payloads attached to a `Message` (today: `sql`, `facet-proposal`). Parsed from structured `item.part.updated` rows. |
| **`AssistantReplyView`** | Coarse layout enum: `conversation` (prose-first), `sql-primary`, `facet-primary`. Derived in [`assistantReplyView.ts`](../../../ui/mill-ui/src/utils/assistantReplyView.ts) from artefacts and/or the completion hint. |
| **`item.completed` summary** | [`ChatSseEvent.ItemCompleted`](../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) carries `presentation` / `partType`. [`ChatRuntimeEventToSseMapper`](../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/ChatRuntimeEventToSseMapper.kt) repeats the **last** structured pair seen in the turn so clients can branch without re-scanning all parts. Streaming clients still ignore `content` when text deltas were emitted. |
| **GET transcript** | [`TurnResponse.assistantReplyView`](../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) is an optional string aligned with the same enum values; **`null`** until persistence stores it. |

### How to add a new structured artefact + view

Work in order; skip steps that do not apply (e.g. UI-only layout tweak).

1. **Wire contract (server)**  
   - Choose a stable **`partType`** (kebab-case, e.g. `chart`, `data-preview`) and **`presentation = "structured"`** on [`ChatRuntimeEvent.StructuredPart`](../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/chat/ChatRuntimeEvent.kt) / emitted [`ItemPartUpdated`](../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt).  
   - **JSON in `content`:** keep a single string payload (object serialised as JSON) to match existing `ItemPartUpdated.content`.

2. **Runtime bridge**  
   - Emit `ChatRuntimeEvent.StructuredPart` from the agent/runtime when the protocol or tool path finalises (pattern: [`LangChain4jChatRuntime.protocolFinalToStructured`](../../../ai/mill-ai-v3-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/LangChain4jChatRuntime.kt) for known `protocolId`s).

3. **mill-ui — parse & types**  
   - Extend **`ChatMessageArtifact`** union and **`parseChatStructuredPart`** ([`chatArtifactParse.ts`](../../../ui/mill-ui/src/utils/chatArtifactParse.ts)); add **`sseItemPartContentLooksLikeStructuredArtifact`** heuristics if the blob must be distinguished from V1 text when metadata is missing on the wire.  
   - Extend **`deriveAssistantReplyView`** with **`AssistantReplyView`**: add a new literal (e.g. `chart-primary`) and precedence rules vs existing kinds.  
   - If GET replay should restore the layout without artefacts, extend allowed values for **`TurnResponse.assistantReplyView`** and **`assistantReplyViewFromWire`**.

4. **mill-ui — rendering**  
   - Add a small card/component under `ui/mill-ui/src/components/chat/artifacts/`.  
   - Branch in **`AssistantReplyRouter`** ([`AssistantReplyRouter.tsx`](../../../ui/mill-ui/src/components/chat/AssistantReplyRouter.tsx)): new `case` for the view, artefact ordering (artefact-first vs prose-first).

5. **Tests**  
   - Mapper: structured part then `Completed` asserts completion carries expected `presentation` / `partType`.  
   - Vitest: `parseChatStructuredPart`, `deriveAssistantReplyView`, and `consumeChatSse` / `onItemCompleted` if behaviour changes.

6. **Persistence (optional, for reload parity)**  
   - Populate **`TurnResponse.assistantReplyView`** from `ConversationTurn` (or linked artefact records) when the store knows the dominant reply kind. Until then, replay falls back to **`conversation`** unless artefacts are rehydrated.

### Backlog cross-links

New `partType`s should be listed in **Planned artefact parts** once named (e.g. `"chart"`, `"data"`). Follow the same forward-compat rule: **unknown** `presentation` / `partType` pairs must not break thin text-only clients.

## Planned artefact parts (stubs)

These are **not** implemented as server event types in the current story wave; they reserve naming and mapping targets:

| Conceptual part | Role | Planned SSE signal (indicative) | Notes |
|-----------------|------|----------------------------------|-------|
| `SqlPart` | Generated SQL text + dialect metadata | `item.part.updated` with non-conversation `presentation` or `partType = "sql"` | Execute / explain actions are **follow-up APIs** (backlog). |
| `DataPart` | Small tabular preview or result summary | structured `partType` (e.g. `"data"`) | Row/column caps; pagination TBD. |
| `ChartPart` | Vega/Lite or image descriptor | structured `partType` (e.g. `"chart"`) | Rendering pipeline TBD. |
| `FacetProposalPart` | Candidate metadata facet rows derived from chat | structured payload referencing facet shape | **Promotion** into chat-scoped metadata is specified in **WI-233** / [`ai-v3-chat-metadata-scope.md`](./ai-v3-chat-metadata-scope.md). |

## Persistence sketch (design only)

- **Option A — JSON column** on conversation turn / assistant item: store ordered list of `{ itemId, type, payload }` mirrors SSE.
- **Option B — side table** keyed by `(chatId, itemId, sequence)` for large blobs.
- **Link to `itemId`** preserved in both options for idempotent upsert and replay ordering.

## Client extension seam (mill-ui)

- **Transport:** `ui/mill-ui/src/types/chatTransport.ts` — `isV1MainConversationTextPart`, `summarizeStructuredPartForward`.
- **SSE consumer:** `ui/mill-ui/src/services/chatService.ts` — `onNonTextPartUpdated`, **`onItemCompleted`** (completion `presentation` / `partType`).
- **Artefact parse:** `ui/mill-ui/src/utils/chatArtifactParse.ts`.
- **Reply layout:** `ui/mill-ui/src/utils/assistantReplyView.ts` — `deriveAssistantReplyView`, `assistantReplyViewFromWire`.
- **UI router:** `ui/mill-ui/src/components/chat/AssistantReplyRouter.tsx` — switches grinder-style layouts from `Message.assistantReplyView` + artefacts.
- **General chat URLs:** `ui/mill-ui/src/components/chat/ChatRouteSync.tsx` — `/chat` and `/chat/:conversationId` (server chat UUID) documented in [`GENERAL-CHAT-DESIGN.md`](../ui/mill-ui/GENERAL-CHAT-DESIGN.md) (URL routing).

## Open questions & backlog labels

- **Execute SQL** action API shape and auth — label: `ai-v3-chat-execute-sql`
- **Chart** asset hosting / sanitization — `ai-v3-chat-chart`
- **Structured SSE** versioning policy — `ai-v3-structured-sse`
- **Artefact persistence** migration — `ai-v3-chat-artefacts-persistence`
- **Metadata scope & facet lifecycle** — `ai-v3-chat-metadata-scope`, `ai-v3-chat-scope-facet-lifecycle` (see WI-233)

## References

- [`ChatSseEvent.kt`](../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt)
- [`ChatRuntimeEvent.kt`](../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/chat/ChatRuntimeEvent.kt) (`StructuredPart`)
- [`ChatRuntimeEventToSseMapper.kt`](../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/ChatRuntimeEventToSseMapper.kt) (structured completion summary)
- [`ChatDtos.kt`](../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) (`TurnResponse.assistantReplyView`)
- [`ai-v3-chat-metadata-scope.md`](./ai-v3-chat-metadata-scope.md) (**WI-233** — normative facet wording)
