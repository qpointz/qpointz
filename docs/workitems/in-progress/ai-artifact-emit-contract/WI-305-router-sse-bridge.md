# WI-305 — Registry-driven router + SSE bridge

Status: `done`  
Type: `✨ feature` / `♻️ refactor`  
Area: `ai`  
Story: [`STORY.md`](STORY.md)

## Depends on

- **WI-303** (`ArtifactDescriptorRegistry` — canonical descriptor shape).
- **WI-304** (coordinator emits `ProtocolFinal`; duplicate rules defined).

## Goal

Replace hardcoded `when` tables in router and SSE bridge with **registry lookups keyed by `sourceEvent`**.

## Layer mapping (single descriptor → all layers)

| Layer | Lookup key | Descriptor fields used |
|-------|------------|------------------------|
| [`AgentEventRouter`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/events/AgentEventRouter.kt) | `sourceEvent` + protocol id / artifact type | `persistKind`, `pointerKeys`, `destinations`, `artifactKind` |
| [`LangChain4jChatRuntime`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/LangChain4jChatRuntime.kt) | `protocolId` on `ProtocolFinal` | `wirePartType`, `presentation` |
| Persistence projector | `persistKind` | payload from routed event content |

Remove parallel hardcodes:

- `STRUCTURED_FINAL_ARTIFACT_POINTER_KEYS` (line ~49)
- `canonicalArtifactRule` `when (artifactType)` table (line ~99)
- `protocolFinalToStructured` inline maps (runtime ~157)

## Duplicate-artifact rules (with WI-304)

- `ToolResult` → route only descriptors with `sourceEvent: tool.result` (e.g. `sql-validation`).
- `ProtocolFinal` → route only descriptors with `sourceEvent: protocol.final` (e.g. `generated-sql`, `inferred-facet`).
- If tool result contains `artifactType: generated-sql` but coordinator already emitted `ProtocolFinal`, router **skips** tool-result mapping for `sql.generated`.

## Deliverables

- [ ] Registry-driven `derivedArtifactEvents` and `ProtocolFinal` routing
- [ ] Registry-driven `protocolFinalToStructured` in chat runtime
- [ ] [`StandardPersistenceProjectorTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjectorTest.kt) — persist kinds align with registry

**Tests:**

- [ ] `AgentEventRouterRegistryTest` — SQL turn produces `sql.validation` + `sql.generated`, not two `sql.generated`
- [ ] `LangChain4jChatRuntimeStructuredTest` (`:ai:mill-ai-autoconfigure:test`)
- [ ] Existing [`ChatRuntimeEventToSseMapperTest`](../../../../ai/mill-ai-service/src/test/kotlin/io/qpointz/mill/ai/service/ChatRuntimeEventToSseMapperTest.kt) still passes

## Acceptance criteria

- [ ] `ProtocolFinal` for SQL → SSE `partType: sql`; facet → `partType: facet-proposal`.
- [ ] Pointers (`last-sql`, `last-metadata-facet-proposal`) from descriptor `pointerKeys`.
- [ ] Adding a new descriptor in YAML requires **no** new Kotlin `when` branches in router or runtime.
