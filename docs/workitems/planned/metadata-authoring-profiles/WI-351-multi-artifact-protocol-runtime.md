# WI-351 — Multi-artifact protocol, runtime fan-out, and chat UI

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `ui`  
Depends on: [WI-345](WI-345-metadata-authoring-design-contract.md)  
**Blocks:** [WI-347](WI-347-metadata-authoring-capability.md), [WI-350](WI-350-schema-authoring-description-tool-cleanup.md)

## Problem Statement

One assistant turn must be able to produce **multiple structured chat artefacts** (e.g. several
`facet-proposal` cards from one user message). Today:

- [`LangChain4jAgent`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/langchain4j/LangChain4jAgent.kt) keeps a single `captureBinding` and emits **one** `ProtocolFinal`
- [`AgentEventToSseMapper`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt) maps structured finals with `mode: replace` — later parts can obscure earlier ones in some paths
- Persistence supports multiple [`ArtifactRecord`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ArtifactStore.kt) rows per `turnId`, but emission does not fan out
- GET replay and mill-ui **can** render N `facet-proposal` rows when `artifacts[]` contains N entries — live SSE + agent aggregation are the gap

**Facet capability rework (WI-347) must not proceed until this WI is done** — multi-facet authoring
depends on batch capture emission, not ad hoc single-artefact hacks.

## Goal

Deliver **platform** multi-artefact support: batch `ProtocolFinal` contract, agent aggregation,
router/persistence/SSE fan-out to **N** flat wire artefacts, and mill-ui verification for N cards
per turn.

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

### 2. Agent loop (`LangChain4jAgent`, `SchemaExplorationAgent` if applicable)

- Collect **all** successful CAPTURE tool results in one iteration (not last-wins)
- Aggregate into **one** `AgentEvent.ProtocolFinal` with `{ results: [...] }` when protocol is `multi`
- Terminate turn after batch capture round (same as today, but with full result set)
- Failed capture in a parallel batch: do not drop siblings; remediate per tool result (existing capture-remediation pattern)

### 3. Fan-out pipeline

| Component | Change |
|-----------|--------|
| [`RegistryAgentEventRouter`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/runtime/events/RegistryAgentEventRouter.kt) / [`StandardPersistenceProjector`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjector.kt) | Split batch payload → **N** `ArtifactRecord` rows (same `persistKind`, same `turnId`) |
| [`AgentEventToSseMapper`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/sse/AgentEventToSseMapper.kt) | For `multi` protocols: emit **N** `item.part.updated` (`presentation: structured`, appropriate `wirePartType`) — use **append** or equivalent so live stream retains all parts; UI `APPEND_MESSAGE_ARTIFACT` path must receive each |
| [`ArtifactWireMapper`](../../../../ai/mill-ai-service/src/main/kotlin/io/qpointz/mill/ai/service/ArtifactWireMapper.kt) | GET replay: N stored rows → N wire `artifacts[]` entries (verify; extend if batch stored as one row today) |

### 4. Design doc update

- [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) — § batch `ProtocolFinal`, fan-out, `multi` flag
- [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) — N structured parts per turn

### 5. mill-ui (minimal)

- [`assistantReplyView.ts`](../../../../ui/mill-ui/src/utils/assistantReplyView.ts) — plural section title when multiple `facet-proposal` on one message
- Vitest: [`artifactGroups.ts`](../../../../ui/mill-ui/src/components/chat/artifactPreview/artifactGroups.ts) + [`MessageArtifactComposer`](../../../../ui/mill-ui/src/components/chat/artifactPreview/MessageArtifactComposer.tsx) with **2+** `facet-proposal` artefacts → **2+** cards

### 6. Tests (`mill-ai`, `mill-ai-service`)

- Unit: batch payload normalizer scalar → `results[1]`
- Unit: fan-out produces 2+ routed persist events from one batch `ProtocolFinal`
- Agent test: two parallel CAPTURE successes (harness / test capability) → two replay artefacts on GET
- **Interim path (if needed):** N × scalar `ProtocolFinal` with fan-out only in mapper — must still pass two-artefact test; document deprecation in WI-347

## Out of Scope

- Catalog-generic tools, prompts, `MetadataReadPort` (**WI-346**, **WI-347**)
- Removing `capture_*` tools (**WI-350**) — but WI-350 should **use** this batch path
- Facet-type-specific scenario packs (**WI-349**)
- Composite single wire artefact with embedded array (fan-out to N flat rows only)
- YAML agent profiles (**WI-348**)

## Acceptance Criteria

- [ ] `metadata.faceting.capture` protocol declares `multi: true` and `finalSchema.results[]`
- [ ] Parallel successful CAPTURE tools in one iteration → **one** batch `ProtocolFinal` → **N** persisted artefacts + **N** SSE structured parts
- [ ] GET `TurnResponse.artifacts` contains **N** entries for an N-capture turn
- [ ] mill-ui renders **N** `FacetCondensedPreview` cards when `artifacts[]` has N `facet-proposal` rows
- [ ] Design docs updated (`artifact-foundation`, `chat-artefact-architecture`)
- [ ] **WI-347 is not started** until this WI is complete and merged on the story branch

## Suggested commit

`[feat] WI-351: batch ProtocolFinal fan-out and multi-artifact chat replay`
