# AI v3 chat — transport extensions & artefacts (design)

This note defines **extension points** for turning streamed assistant output into **durable chat artefacts** (SQL text, result metadata, charts, metadata facet proposals) without breaking the **V1 conversational text** path.

**Canonical artefact reference:** [`artifact-foundation.md`](./artifact-foundation.md) — descriptor model, emission pipeline, implemented POC table, add-artifact checklist.

**Normative scope for chat-bound metadata facets** (promotion UX, merge precedence, lifecycle) lives in [`ai-v3-chat-metadata-scope.md`](./ai-v3-chat-metadata-scope.md) (**WI-233**). This document only **cross-links** that material so facet rules are not duplicated here.

## Baseline contract

- **Wire model:** [`ChatSseEvent`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) JSON in SSE `data:` rows, discriminated by `type`.
- **V1 frozen path:** `presentation = "conversation"` and `partType = "text"` on `item.part.updated` — mill-ui main bubble appends `content` with `mode` (`append` / future `replace` / `patch`).
- **Forward compatibility:** unknown `presentation` / `partType` combinations **must not** abort the stream for thin text UIs; mill-ui parses recognized structured parts into cards and uses an **`unknown`** fallback for other `presentation: structured` payloads.
- **Correlation:** all events for one logical assistant item share the same `itemId` (and ordered `sequence`).

## Live stream vs durable replay

| Phase | Source | Consumer responsibility |
|-------|--------|-------------------------|
| **Live SSE** | `POST /api/v1/ai/chats/{id}/messages` | Parse `data:` JSON; accumulate text deltas; extension reducers for structured parts (`onNonTextPartUpdated`) and end-of-turn hints (`onItemCompleted`). |
| **Replay** | `GET /api/v1/ai/chats/{id}` → `TurnResponse` list | Durable `text` per turn; optional `assistantReplyView` (`conversation` \| `sql-primary` \| `facet-primary` \| `schema-primary` \| `artifact-primary`) for mill-ui layout when persistence populates it. **Future:** artefact payloads or ids alongside turns for full parity with live SSE. |

**Design intent:** the **logical sequence** of `item.*` events should be **reconstructible** from durable storage so a reconnecting client or audit view can render the same artefact timeline as the live stream.

## Per-reply views (mill-ui) — extension guide

mill-ui routes assistant chrome similarly to legacy mill-grinder-ui **intent-based** layouts (`resultIntent`), but the v3 stack uses **SSE parts + an optional end-of-turn summary**, not a REST `resultIntent` field on each message.

### Concepts

| Layer | Purpose |
|-------|---------|
| **`ChatMessageArtifact`** (`ui/mill-ui/src/types/chat.ts`) | Normalised structured payloads: `sql`, `facet-proposal`, `schema-capture`, **`unknown`** (fallback). Parsed from structured `item.part.updated` rows. |
| **`AssistantReplyView`** | Coarse layout enum: `conversation`, `sql-primary`, `facet-primary`, **`schema-primary`**, **`artifact-primary`**. Derived in [`assistantReplyView.ts`](../../../ui/mill-ui/src/utils/assistantReplyView.ts). |
| **`item.completed` summary** | [`ChatSseEvent.ItemCompleted`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) carries `presentation` / `partType`. [`AgentEventToSseMapper`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt) repeats the **last** structured pair seen in the turn. Streaming clients still ignore `content` when text deltas were emitted. |
| **GET transcript** | [`TurnResponse.assistantReplyView`](../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) is an optional string aligned with the same enum values; **`null`** until persistence stores it. **`Message.artifacts`** are stream-only in mill-ui today. |

### How to add a new structured artefact + view

See **[`artifact-foundation.md`](./artifact-foundation.md) §8** for the full checklist. Summary:

1. **Wire contract (server)** — capability YAML `artifacts:` with `wirePartType` and `presentation: structured`
2. **Runtime bridge** — [`ArtifactEmissionCoordinator`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/ArtifactEmissionCoordinator.kt) or protocol executor → [`AgentEventToSseMapper`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt)
3. **mill-ui — parse & types** — [`chatArtifactParse.ts`](../../../ui/mill-ui/src/utils/chatArtifactParse.ts), [`assistantReplyView.ts`](../../../ui/mill-ui/src/utils/assistantReplyView.ts)
4. **mill-ui — rendering** — card under `artifacts/`, register in [`ArtifactCard`](../../../ui/mill-ui/src/components/chat/artifacts/ArtifactCard.tsx)
5. **Tests** — mapper + Vitest + optional scenario pack
6. **Persistence (optional)** — GET replay parity

### Backlog cross-links

New `partType`s should be listed in **Planned artefact parts** once named (e.g. `"chart"`, `"data"`). Follow the same forward-compat rule: **unknown** `presentation` / `partType` pairs must not break thin text-only clients.

## Planned artefact parts (stubs)

These reserve naming; see [`artifact-foundation.md`](./artifact-foundation.md) §5 for **implemented** POC artefacts.

| Conceptual part | Role | Planned SSE signal (indicative) | Notes |
|-----------------|------|----------------------------------|-------|
| `SqlPart` | Generated SQL text + dialect metadata | `partType = "sql"` | **Implemented** — `generated-sql` descriptor |
| `FacetProposalPart` | Candidate metadata facet rows | `partType = "facet-proposal"` | **Implemented** — `inferred-facet` |
| `SchemaCapturePart` | Schema authoring capture | `partType = "schema-capture"` | **Implemented** — schema-authoring capability |
| `DataPart` | Small tabular preview or result summary | structured `partType` (e.g. `"data"`) | Row/column caps; pagination TBD. |
| `ChartPart` | Vega/Lite or image descriptor | structured `partType` (e.g. `"chart"`) | Rendering pipeline TBD. |

## Persistence sketch (design only)

- **Option A — JSON column** on conversation turn / assistant item: store ordered list of `{ itemId, type, payload }` mirrors SSE.
- **Option B — side table** keyed by `(chatId, itemId, sequence)` for large blobs.
- **Link to `itemId`** preserved in both options for idempotent upsert and replay ordering.

## Client extension seam (mill-ui)

- **Transport:** `ui/mill-ui/src/types/chatTransport.ts` — `isV1MainConversationTextPart`, `summarizeStructuredPartForward`.
- **SSE consumer:** `ui/mill-ui/src/services/chatService.ts` — `onNonTextPartUpdated`, **`onItemCompleted`** (completion `presentation` / `partType`).
- **Artefact parse:** `ui/mill-ui/src/utils/chatArtifactParse.ts`.
- **Reply layout:** `ui/mill-ui/src/utils/assistantReplyView.ts` — `deriveAssistantReplyView`, `assistantReplyViewFromWire`, `structuredReplySectionTitle`.
- **UI router:** `ui/mill-ui/src/components/chat/AssistantReplyRouter.tsx` — switches layouts from `Message.assistantReplyView` + artefacts.
- **Artefact cards:** `ui/mill-ui/src/components/chat/artifacts/` — `ArtifactCard`, typed cards, `UnknownArtifactCard` fallback.
- **General chat URLs:** `ui/mill-ui/src/components/chat/ChatRouteSync.tsx` — `/chat` and `/chat/:conversationId` (server chat UUID) documented in [`GENERAL-CHAT-DESIGN.md`](../ui/mill-ui/GENERAL-CHAT-DESIGN.md) (URL routing).

## Open questions & backlog labels

- **Execute SQL** action API shape and auth — label: `ai-v3-chat-execute-sql`
- **Chart** asset hosting / sanitization — `ai-v3-chat-chart`
- **Structured SSE** versioning policy — `ai-v3-structured-sse`
- **Artefact persistence** migration — `ai-v3-chat-artefacts-persistence`
- **Metadata scope & facet lifecycle** — `ai-v3-chat-metadata-scope`, `ai-v3-chat-scope-facet-lifecycle` (see WI-233)

## References

- [`artifact-foundation.md`](./artifact-foundation.md) — **start here** for agents extending artefacts
- [`ChatSseEvent.kt`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt)
- [`AgentEventToSseMapper.kt`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt)
- [`ChatDtos.kt`](../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) (`TurnResponse.assistantReplyView`)
- [`ai-v3-chat-metadata-scope.md`](./ai-v3-chat-metadata-scope.md) (**WI-233** — normative facet wording)
