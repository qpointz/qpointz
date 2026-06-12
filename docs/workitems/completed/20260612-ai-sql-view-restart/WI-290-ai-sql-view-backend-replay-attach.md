# WI-290 — Backend replay wire + attach-result

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `done` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ai`, `ui` (types/service seam only) |
| **Depends on** | [**WI-289**](WI-289-ai-sql-view-design-contract.md), artefacts prerequisite gate |
| **Enables** | [**WI-291**](WI-291-ai-sql-view-preview-framework.md), [**WI-292**](WI-292-ai-sql-view-chat-wiring.md) |

## Goal

Enable **history reload** and **client Run persistence**:

1. `GET /api/v1/ai/chats/{id}` returns wire artefacts for mill-ui chat-type treatment on history open.
2. `POST …/turns/{turnId}/execution-result` persists client query metadata after Run (no server SQL execution).

**Service-layer only.** Artifact emission and `last-sql` pointer routing are provided by
[`ai-artifact-emit-contract`](../../in-progress/ai-artifact-emit-contract/STORY.md). **Do not modify**
`ai/mill-ai` runtime in this WI.

## Deliver

### GET replay — [`ArtifactWireMapper`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapper.kt) (new)

Map persisted [`ArtifactRecord`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ArtifactRecord.kt) rows
to consumer DTOs:

| Persisted (`kind` / payload) | Wire `kind` | Source |
|------------------------------|-------------|--------|
| `sql.generated` / `artifactType: generated-sql` | `sql` | Agent emission (foundation) |
| `sql.result` / `artifactType: sql-result` | `data` | Client attach (this WI) |
| Facet / metadata kinds | `facet-proposal` | Agent emission (foundation) |
| `sql.validation` | *(not exposed)* | Audit only |

- `deriveAssistantReplyView` → `sql-primary` / `facet-primary` (extend for schema when wire supports it)

### [`UnifiedChatService`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt)

- Inject [`ArtifactStore`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ArtifactStore.kt)
- `getChat`: resolve each turn's `artifactIds` → wire artefacts via `ArtifactWireMapper`
- Return [`TurnResponse`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) list (not raw `ConversationTurn`)
- `attachExecutionResult`: persist `sql.result` artifact; `conversationStore.attachArtifacts`

### HTTP + DTOs

- Extend `TurnResponse`: `artifacts: List<ArtifactResponse>`, derived `assistantReplyView`
- Add `AttachExecutionResultHttpRequest`, `ExecutionColumnDto`, `ArtifactResponse`
- `POST /api/v1/ai/chats/{chatId}/turns/{turnId}/execution-result` in [`AiChatController`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt)
- Extend [`ChatService`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/ChatService.kt) interface
- Wire `ArtifactStore` in [`AiV3ChatServiceAutoConfiguration`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/AiV3ChatServiceAutoConfiguration.kt)

### mill-ui (types + service seam)

- Extend [`ChatMessageArtifact`](../../../../ui/mill-ui/src/types/chat.ts) with `kind: 'data'`
- Add `AttachExecutionResultRequest` + `attachExecutionResult` to `ChatService` interface
- Implement in [`chatService.ts`](../../../../ui/mill-ui/src/services/chatService.ts)
- [`chatWire.ts`](../../../../ui/mill-ui/src/types/chatWire.ts) — `ArtifactResponseWire` on `TurnResponseWire` (may already exist)
- New [`artifactWireParse.ts`](../../../../ui/mill-ui/src/utils/artifactWireParse.ts) — `parseWireArtifacts` (used by WI-292)

**Run execution itself** lands in WI-291 (`useChatArtifactRun`); this WI provides attach API + types.

### Tests

- `ArtifactWireMapperTest` — `sql.generated`, `sql.result`, facet mapping
- `UnifiedChatServiceTest` — GET replay with `artifactIds`; attach round-trip
- `AiChatControllerTest` — attach endpoint happy/404 paths
- `mill-ai-persistence` testIT: turn ↔ artefact linkage (if not already covered)

## Out of scope

- **`mill-ai` runtime** — coordinator, router, descriptors
- Preview UI, expand, `ChatContext` wiring → WI-291–292
- Server-side query execution
- Salvage paths

## Acceptance criteria

- [x] GET chat detail includes wire artefacts for turns with `artifactIds`.
- [x] `assistantReplyView` populated when SQL artefact exists.
- [x] Attach endpoint persists `sql.result`; GET replay returns `kind: data` with `executionId`.
- [x] No changes to `ai/mill-ai` runtime modules.
- [x] Tests green for scoped modules.
