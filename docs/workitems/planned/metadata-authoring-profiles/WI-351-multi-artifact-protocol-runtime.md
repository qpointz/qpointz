# WI-351 — Multi-artifact protocol, runtime fan-out, and chat UI

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `ui`  
Depends on: [WI-345](WI-345-metadata-authoring-design-contract.md)  
**Blocks:** [WI-347](WI-347-metadata-authoring-capability.md), [WI-350](WI-350-schema-authoring-description-tool-cleanup.md)  
**Related:** [WI-353](WI-353-facet-artifact-lifecycle-events.md) — artefact `status: pending` on new facet-proposal rows

## Problem Statement

One assistant turn must be able to produce **multiple structured chat artefacts** (e.g. several
`facet-proposal` cards from one user message). Today:

- [`LangChain4jAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt) keeps a single `captureBinding` and emits **one** `ProtocolFinal`
- [`AgentEventToSseMapper`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt) maps structured finals with `mode: replace` — later parts can obscure earlier ones in some paths
- Persistence supports multiple [`ArtifactRecord`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ArtifactStore.kt) rows per `turnId`, but emission does not fan out
- GET replay and mill-ui **can** render N `facet-proposal` rows when `artifacts[]` contains N entries — live SSE + agent aggregation are the gap

**Facet capability rework (WI-347) must not proceed until phase A is done** — multi-facet same-kind batch is the prerequisite. **Phase B (story close):** heterogeneous SQL + facet artefacts per turn ([`GAPS.md`](GAPS.md) §10).

## Goal

Deliver **first-class** multi-artefact support: batch `ProtocolFinal` contract, agent aggregation,
router/persistence/SSE fan-out to **N** flat wire artefacts, **per-turn `artifactIds[]` + list pointers**,
**GET `TurnResponse.artifacts[]` hydration**, and mill-ui verification for N cards per turn.

Capability-specific prompts and catalog tools remain **WI-347**.

## Design reference

- [`STORY.md`](STORY.md) § *Multi-facet batch*, § *Protocol: batch ProtocolFinal*
- WI-345 § A3 (multi-facet rules), § A4 (batch envelope)

## In Scope

### 1. Protocol manifest extension (`mill-ai`)

- Add optional **`multi: true`** on `structured_final` protocols (or equivalent flag parsed by [`CapabilityManifest`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/capability/CapabilityManifest.kt))
- **`finalSchema`** may require **`results`** array; document item shape = today's single capture payload
- **First consumer:** `metadata.faceting.capture` in [`metadata-authoring.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/metadata-authoring.yaml) — wire `multi: true` + `results[]` schema
- **Backward compat:** normalizer accepts legacy **scalar** single-proposal payload → `results` length 1

### 2b. Mixed-kind turns (phase B — [`GAPS.md`](GAPS.md) §10)

- One turn may emit **both** `sql-query.generated-sql` and `metadata.faceting.capture` structured finals
- Agent collects **multiple protocol finals** (or equivalent multi-lane aggregation) — not only single-protocol `{ results[] }`
- Persistence / SSE: **all** artefact kinds on the turn appear in `artifacts[]`; partial failure per §9 across lanes

### 2. Agent loop (`LangChain4jAgent` only — [`GAPS.md`](GAPS.md) §14)

- **Delete** [`SchemaExplorationAgent.kt`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/SchemaExplorationAgent.kt) — unused; production path is [`LangChain4jChatRuntime`](../../../../ai/mill-ai-autoconfigure/src/main/kotlin/io/qpointz/mill/ai/autoconfigure/chat/LangChain4jChatRuntime.kt) → **`LangChain4jAgent`**
- **Keep** [`SchemaExplorationAgentProfile`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/profile/SchemaExplorationAgentProfile.kt) — profile id `schema-exploration` (YAML in WI-348)

- Collect **all** successful CAPTURE tool results in one iteration (not last-wins)
- **Partial batch failure (locked — [`GAPS.md`](GAPS.md) §9):** when parallel captures mix success/failure, include **every success** in the batch `ProtocolFinal` `{ results[] }` — **do not stop** after the first failure; **do not** discard successful siblings
- Aggregate into **one** `AgentEvent.ProtocolFinal` with `{ results: [...] }` when protocol is `multi` (successful items only)
- Terminate capture round after processing **all** parallel CAPTURE results; failed items are remediated in a **subsequent** tool iteration (WI-347 prompts)

### 3. Fan-out pipeline

| Component | Change |
|-----------|--------|
| [`RegistryAgentEventRouter`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/events/RegistryAgentEventRouter.kt) / [`StandardPersistenceProjector`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjector.kt) | Split batch payload → **N** `ArtifactRecord` rows (same `persistKind`, same `turnId`); **`attachArtifacts(turnId, all N ids)`** once ([`GAPS.md`](GAPS.md) §15) |
| [`ActiveArtifactPointerStore`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ActiveArtifactPointerStore.kt) + JPA | **List pointers** for `pointerCardinality: multiple` — **`metadata-facet-proposals`** replaces `last-metadata-facet-proposal`; **`appendAll`** per batch ([`GAPS.md`](GAPS.md) §15) |
| [`ArtifactDescriptor`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/core/artifact/ArtifactDescriptor.kt) / manifest | New **`pointerCardinality`**: `single` (default) \| `multiple` |
| [`AgentEventToSseMapper`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt) | Batch fan-out → **N** `item.part.updated`; **append** for 2..N; **`item.completed`** with `partType: "multi"` when N>1 ([`GAPS.md`](GAPS.md) §16) |
| [`ChatSseEvent.ItemCompleted`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) | Optional **`structuredPartCount`**, **`partTypes[]`**; breaking wire extension |
| [`UnifiedChatService`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/UnifiedChatService.kt) / [`ArtifactWireMapper`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapper.kt) | GET replay: **N** stored rows → **N** `TurnResponse.artifacts[]` entries in stable order ([`GAPS.md`](GAPS.md) §15) |

### 4. Design doc update ([`GAPS.md`](GAPS.md) §13)

- [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) — § batch `ProtocolFinal`, fan-out, `multi` flag, **list pointers**, per-turn GET hydration
- [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) — N structured parts per turn; `TurnResponse.artifacts[]` normative
- [`ai-v3-chat-transport-extensions.md`](../../../design/agentic/ai-v3-chat-transport-extensions.md) — **breaking** SSE: `partType: multi`, append mode ([`GAPS.md`](GAPS.md) §16)

### 5. mill-ui (SSE live + replay)

- [`assistantReplyView.ts`](../../../../ui/mill-ui/src/utils/assistantReplyView.ts) — plural section title when multiple `facet-proposal`; `deriveAssistantReplyView` when N artefacts before `item.completed`
- [`chatService.ts`](../../../../ui/mill-ui/src/services/chatService.ts) — parse `partType: "multi"` / `structuredPartCount` on `item.completed` (forward to `onItemCompleted`)
- Vitest:
  - [`chatService.test.ts`](../../../../ui/mill-ui/src/services/__tests__/chatService.test.ts) — **N** `item.part.updated` (structured) + `item.completed` multi hint
  - [`ChatContext`](../../../../ui/mill-ui/src/context/ChatContext.tsx) reducer or dedicated test — **N** `APPEND_MESSAGE_ARTIFACT` on one message
  - [`artifactGroups.ts`](../../../../ui/mill-ui/src/components/chat/artifactPreview/artifactGroups.ts) + [`MessageArtifactComposer`](../../../../ui/mill-ui/src/components/chat/artifactPreview/MessageArtifactComposer.tsx) — **2+** `facet-proposal` → **2+** cards (L6)

### 6. Tests (`mill-ai`, `mill-ai-service`, `ui/mill-ui`)

**Proof strategy (locked — [`GAPS.md`](GAPS.md) §1):** layered mock-LLM + per-layer unit tests; no legacy `capture_*`, no live LLM in WI-351.

| Layer | Test | Assert |
|-------|------|--------|
| L1 | [`LangChain4jAgentEmitTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgentEmitTest.kt) — **two** parallel `propose_facet_assignment` (both succeed) + mock `MetadataReadPort` | One batch `ProtocolFinal` `{ results: […, …] }` |
| L1b | Same — **two** parallel captures, **one succeeds / one fails** | Batch `{ results: [one] }` only; **one** fan-out artefact; agent may continue turn for remediation (no rollback of success) |
| L2 | Batch normalizer unit test | Scalar ↔ `results[1]`; batch expands to two facet maps |
| L3 | [`StandardPersistenceProjectorTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjectorTest.kt) — injected batch routed event | N `ArtifactRecord` rows; **turn.artifactIds.size == N**; list pointer has N entries |
| L4 | [`AgentEventToSseMapperTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/sse/ChatSseEventTest.kt) — batch `ProtocolFinal` | N `facet-proposal` `item.part.updated` (1st replace, 2..N **append**); `item.completed` → `partType: "multi"`, `structuredPartCount: N` |
| L4b | [`ChatSseEventItemPartWireJsonTest`](../../../../ai/mill-ai/src/test/kotlin/io/qpointz/mill/ai/sse/ChatSseEventItemPartWireJsonTest.kt) | Wire JSON includes new `item.completed` fields when N>1 |
| L5 | [`ArtifactWireMapperTest`](../../../../ai/mill-ai-service/src/test/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapperTest.kt) + **`UnifiedChatServiceTest`** GET path | N persisted rows → N `TurnResponse.artifacts[]`; order stable |
| L6 | mill-ui Vitest — `chatService` + `MessageArtifactComposer` / `artifactGroups` | N-part SSE stream → N live artefacts + N cards; `item.completed` multi hint |

- **Interim path (if needed):** N × scalar `ProtocolFinal` with fan-out only in mapper — must still pass L3–L6; remove before story close (GAPS §21)
- **Deferred:** `mill-ai-test` live-LLM multi-facet scenarios → **WI-349**
- **Phase B:** mixed SQL + facet same-turn e2e → **WI-349** (agent changes in WI-351 §2b)

## Out of Scope

- Catalog-generic tools, prompts, `MetadataReadPort` (**WI-346**, **WI-347**)
- Removing `capture_*` tools (**WI-350**) — but WI-350 should **use** this batch path
- Facet-type-specific scenario packs (**WI-349**)
- Accept/Reject UI, scope assign, event handlers (**WI-353**)
- Composite single wire artefact with embedded array (fan-out to N flat rows only)
- YAML agent profiles (**WI-348**)

## Acceptance Criteria

- [ ] `metadata.faceting.capture` protocol declares `multi: true` and `finalSchema.results[]`
- [ ] **`SchemaExplorationAgent.kt` deleted**; **`SchemaExplorationAgentProfile` retained** ([`GAPS.md`](GAPS.md) §14)
- [ ] **List pointers:** `metadata-facet-proposals` + `appendAll`; **`pointerCardinality: multiple`** on facet descriptor ([`GAPS.md`](GAPS.md) §15)
- [ ] **GET hydration:** `TurnResponse.artifacts[]` length **N** with stable order for N-capture turn
- [ ] **SSE:** N `item.part.updated` (append after first); `item.completed` with **`partType: multi`** when N>1 ([`GAPS.md`](GAPS.md) §16)
- [ ] **UI Vitest:** live SSE path — N structured parts → N message artefacts + N composer cards
- [ ] Parallel successful CAPTURE tools in one iteration → **one** batch `ProtocolFinal` → **N** persisted artefacts + **N** SSE structured parts
- [ ] **Partial failure:** 2 parallel captures, 1 fails → **1** persisted `facet-proposal` (success not dropped); batch `results[]` length = success count only
- [ ] GET `TurnResponse.artifacts` contains **N** entries for an N-capture turn
- [ ] mill-ui renders **N** `FacetCondensedPreview` cards when `artifacts[]` has N `facet-proposal` rows
- [ ] Design docs updated (`artifact-foundation`, `chat-artefact-architecture`)
- [ ] **Phase B (story close):** one turn with `validate_sql` success + `propose_facet_assignment` → `artifacts[]` contains **both** SQL and `facet-proposal` kinds (WI-349 e2e)
- [ ] **WI-347 is not started** until phase A (same-kind facet batch) is complete and merged

## Suggested commit

`[feat] WI-351: batch ProtocolFinal fan-out and multi-artifact chat replay`
