# WI-290 — Backend GET artefact replay wire

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ai` |
| **Depends on** | [**WI-289**](WI-289-ai-sql-view-design-contract.md) |
| **Enables** | [**WI-293**](WI-293-ai-sql-view-chat-surfaces-parity.md) |

## Goal

`GET /api/v1/ai/chats/{id}` returns enough artefact payload for mill-ui to apply **chat-type-specific
treatment** on history open (preview, host-apply, or card) — without re-running the agent.

**Service-layer wire mapping only.** Artifact emission and `last-sql` pointer routing are already
provided by [`ai-artifact-emit-contract`](../../in-progress/ai-artifact-emit-contract/STORY.md)
(`RegistryAgentEventRouter`, descriptors). **Do not modify** `mill-ai` runtime in this WI.

## Deliver

### [`ArtifactWireMapper`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapper.kt) (new)

- Map persisted [`ArtifactRecord`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ArtifactRecord.kt) rows
  to consumer DTOs (`sql`, `data`, `facet-proposal`).
- Recognise artefacts-branch persist kinds: `sql.generated`, `sql.result`, facet kinds.
- `deriveAssistantReplyView` → `sql-primary` / `facet-primary`.

### [`UnifiedChatService.getChat`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt)

- Inject [`ArtifactStore`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ArtifactStore.kt).
- Resolve each turn's `artifactIds` → wire artefacts via `ArtifactWireMapper`.
- Return [`TurnResponse`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) list (not raw `ConversationTurn`).

### [`TurnResponse`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt)

- Add `artifacts: List<ArtifactResponse>` (kind + payload).
- Derive `assistantReplyView` when SQL/data artefact present.

### [`AiV3ChatServiceAutoConfiguration`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/AiV3ChatServiceAutoConfiguration.kt)

- Wire `ArtifactStore` into `UnifiedChatService` bean.

### Tests

- `ArtifactWireMapperTest` — `sql.generated`, `sql.result`, facet mapping.
- `UnifiedChatServiceTest` — GET returns artefacts on turns with `artifactIds`.
- `mill-ai-persistence` testIT: turn ↔ artefact linkage round-trip (if not already covered).

## Out of scope

- **`mill-ai` runtime** — `AgentEventRouter`, `LangChain4jAgent`, `ArtifactEmissionCoordinator` (artefacts story).
- Attach-result POST (WI-291).
- Server-side query execution.
- mill-ui consumption (WI-292 / WI-293).
- Salvage paths.

## Acceptance criteria

- [ ] GET chat detail includes wire artefacts for turns with `artifactIds`.
- [ ] `assistantReplyView` populated when SQL artefact exists.
- [ ] `ArtifactWireMapper` maps `sql.generated` from live agent turns (emitted by artefacts foundation).
- [ ] No changes to `ai/mill-ai` runtime modules.
- [ ] Tests green for scoped modules.
