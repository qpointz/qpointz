# AI v3 chat — transport extensions & artefacts (design)

This note defines **extension points** for turning streamed assistant output into **durable chat artefacts** (SQL text, result metadata, charts, metadata facet proposals) without breaking the **V1 conversational text** path.

**Canonical artefact reference:** [`artifact-foundation.md`](./artifact-foundation.md) — descriptor model, emission pipeline, implemented POC table, add-artifact checklist.

**Presentation + GET replay:** [`chat-artefact-architecture.md`](../ai/chat-artefact-architecture.md) — chat-type treatments, condensed/expand, `ArtifactWireMapper`, attach-result.

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
| **Replay** | `GET /api/v1/ai/chats/{id}` → `TurnResponse` list | Durable `text` per turn; `artifacts[]` wire payloads (`sql`, `data`, `facet-proposal`, `schema-capture`); optional `assistantReplyView`. Server: [`ArtifactWireMapper`](../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapper.kt). Client: [`parseWireArtifacts`](../../../ui/mill-ui/src/utils/artifactWireParse.ts). See [`chat-artefact-architecture.md`](../ai/chat-artefact-architecture.md). |

**Design intent:** the **logical sequence** of `item.*` events should be **reconstructible** from durable storage so a reconnecting client or audit view can render the same artefact timeline as the live stream.

## Per-reply views (mill-ui) — extension guide

mill-ui routes assistant chrome similarly to legacy mill-grinder-ui **intent-based** layouts (`resultIntent`), but the v3 stack uses **SSE parts + an optional end-of-turn summary**, not a REST `resultIntent` field on each message.

### Concepts

| Layer | Purpose |
|-------|---------|
| **`ChatMessageArtifact`** (`ui/mill-ui/src/types/chat.ts`) | Normalised structured payloads: `sql`, `data`, `facet-proposal`, `schema-capture`, **`unknown`**. Parsed from SSE + GET wire. |
| **`AssistantReplyView`** | Coarse layout enum: `conversation`, `sql-primary`, `facet-primary`, **`schema-primary`** (legacy wire only), **`artifact-primary`**. `schema-capture` artefacts resolve to **`facet-primary`**. Derived in [`assistantReplyView.ts`](../../../ui/mill-ui/src/utils/assistantReplyView.ts) and on GET via [`ArtifactWireMapper`](../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapper.kt). |
| **`item.completed` summary** | [`ChatSseEvent.ItemCompleted`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) carries `presentation` / `partType`. [`AgentEventToSseMapper`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt) repeats the **last** structured pair for single-part turns; when **N > 1** structured parts were streamed, `partType` is **`multi`**, with optional `structuredPartCount` and `partTypes[]`. Streaming clients still ignore `content` when text deltas were emitted. mill-ui forwards multi hints via [`ChatItemCompletedPayload`](../../../ui/mill-ui/src/types/chat.ts). |
| **GET transcript** | [`TurnResponse`](../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt): `artifacts[]`, optional `assistantReplyView`. |

### How to add a new structured artefact + view

See **[`artifact-foundation.md`](./artifact-foundation.md) §8** for the full checklist. Summary:

1. **Wire contract (server)** — capability YAML `artifacts:` with `wirePartType` and `presentation: structured`
2. **Runtime bridge** — [`ArtifactEmissionCoordinator`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/ArtifactEmissionCoordinator.kt) or protocol executor → [`AgentEventToSseMapper`](../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt)
3. **mill-ui — parse & types** — [`chatArtifactParse.ts`](../../../ui/mill-ui/src/utils/chatArtifactParse.ts) (live), [`artifactWireParse.ts`](../../../ui/mill-ui/src/utils/artifactWireParse.ts) (GET), [`assistantReplyView.ts`](../../../ui/mill-ui/src/utils/assistantReplyView.ts)
4. **mill-ui — rendering** — facet-shaped: [`FacetCondensedPreview`](../../../ui/mill-ui/src/components/chat/artifactPreview/FacetCondensedPreview.tsx) + shared [`data-model/facets/`](../../../ui/mill-ui/src/components/data-model/facets/); otherwise card under `artifacts/`, register in [`ArtifactCard`](../../../ui/mill-ui/src/components/chat/artifacts/ArtifactCard.tsx). General chat treatments: [`chat-artefact-architecture.md`](../ai/chat-artefact-architecture.md).
5. **Tests** — mapper + Vitest + optional scenario pack
6. **Persistence (optional)** — GET replay parity via `ArtifactWireMapper`

### Backlog cross-links

New `partType`s should be listed in **Planned artefact parts** once named (e.g. `"chart"`, `"data"`). Follow the same forward-compat rule: **unknown** `presentation` / `partType` pairs must not break thin text-only clients.

## Planned artefact parts (stubs)

These reserve naming; see [`artifact-foundation.md`](./artifact-foundation.md) §5 for **implemented** POC artefacts.

| Conceptual part | Role | Planned SSE signal (indicative) | Notes |
|-----------------|------|----------------------------------|-------|
| `SqlPart` | Generated SQL text + dialect metadata | `partType = "sql"` | **Implemented** — `generated-sql` descriptor |
| `FacetProposalPart` | Candidate metadata facet rows | `partType = "facet-proposal"` | **Implemented** — `inferred-facet` |
| `SchemaCapturePart` | Schema authoring capture | `partType = "schema-capture"` | **Implemented** — schema-authoring capability |
| `DataPart` | Result metadata + lazy page fetch | GET wire `kind: data`; client attach after Run | **Implemented** — WI-290 attach + WI-291 preview |
| `ChartPart` | Vega/Lite or image descriptor | structured `partType` (e.g. `"chart"`) | Rendering pipeline TBD. |

## Persistence sketch (design only)

- **Option A — JSON column** on conversation turn / assistant item: store ordered list of `{ itemId, type, payload }` mirrors SSE.
- **Option B — side table** keyed by `(chatId, itemId, sequence)` for large blobs.
- **Link to `itemId`** preserved in both options for idempotent upsert and replay ordering.

## Client extension seam (mill-ui)

- **Transport:** `ui/mill-ui/src/types/chatTransport.ts` — `isV1MainConversationTextPart`, `summarizeStructuredPartForward`.
- **SSE consumer:** `ui/mill-ui/src/services/chatService.ts` — `onNonTextPartUpdated`, **`onItemCompleted`** (completion `presentation` / `partType`).
- **Artefact parse:** `ui/mill-ui/src/utils/chatArtifactParse.ts` (SSE), `artifactWireParse.ts` (GET).
- **Reply layout:** `ui/mill-ui/src/utils/assistantReplyView.ts` — `deriveAssistantReplyView`, `assistantReplyViewFromWire`, `structuredReplySectionTitle`.
- **UI router:** `ui/mill-ui/src/components/chat/AssistantReplyRouter.tsx` — switches layouts from `Message.assistantReplyView` + artefacts.
- **General chat condensed preview:** `ui/mill-ui/src/components/chat/artifactPreview/` — `MessageArtifactComposer`, `FacetCondensedPreview`, `facetArtifactNormalize.ts`, `chatArtifactTreatments.ts`.
- **Shared facet read-only:** `ui/mill-ui/src/components/data-model/facets/` — `FacetReadOnlyBody`, `FacetPayloadReadOnly` (used by Data Model and chat).
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
- [`chat-artefact-architecture.md`](../ai/chat-artefact-architecture.md) — chat-type treatments, GET replay, shared facet layer
- [`ai-v3-chat-metadata-scope.md`](./ai-v3-chat-metadata-scope.md) (**WI-233** — normative facet wording)
