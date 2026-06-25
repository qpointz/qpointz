# WI-353 — Facet artefact lifecycle: chat-scope assignment, Accept/Reject, event bus

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `metadata`, `ui`  
Depends on: [WI-351](WI-351-multi-artifact-protocol-runtime.md), [WI-352](WI-352-metadata-content-entity-and-seed.md), [WI-347](WI-347-metadata-authoring-capability.md)  
**Stage:** **4** — branch `feat/meta-authoring-lifecycle` (see [`STORY.md`](STORY.md))

## Problem Statement

Facet capture today ends at a **persisted chat artefact** with **`writeScopeUrns[]`** on the body.
Operators need facets **applied to chat scope** when inferred, with a simple **Accept / Reject** UX —
not a separate M-23 promotion step before scope merge.

**Reject** must remove both the chat artefact **and** the scope assignment(s) created for that
capture. The mechanism should extend to other artefact kinds later.

## Architectural note — internal event bus (not operational drift)

Scope assignment and retraction use **`:core:mill-events`** with **in-process** transport
(`InMemoryEventTransport` / `SpringEventTransport`). This follows the normative design in
[`general-event-bus.md`](../../../design/platform/general-event-bus.md) — WI-360 is the **first
production consumer** of that foundation (**WI-311**–**WI-314**). The split is **architectural**
(AI artefact store vs metadata scope writes; thin REST/controllers), **not** a workaround for
operational drift. **Kafka / outbox** is explicitly **out of scope** (**P-50** backlog).

## Goal

End-to-end **facet artefact lifecycle**:

1. LLM **`propose_facet_assignment`** → **`facet-proposal`** artefact persisted + SSE to UI  
2. **`EventPublisher`** emits **`artifact.facet.persisted`**  
3. **Handler** applies facet to each URN in **`writeScopeUrns[]`** via **`FacetProposalMerger`** (chat scope)  
4. UI shows facet card with **Accept** / **Reject**  
5. **Accept** — lock artefact (`status: accepted`); scope rows **unchanged**  
6. **Reject** — emit retraction event → handler **removes scope assignments** + **deletes** chat artefact  

## In Scope

### 1. Event types (`:core:mill-events` catalog + payloads)

Add to [`EventTypes.kt`](../../../../core/mill-events/src/main/kotlin/io/qpointz/mill/events/catalog/EventTypes.kt):

| Constant | Routing key | When |
| -------- | ----------- | ---- |
| `ARTIFACT_FACET_PERSISTED` | `artifact.facet.persisted` | After `ArtifactStore.save` for `facet-proposal` with non-empty **`writeScopeUrns[]`** |
| `ARTIFACT_RETRACTED` | `artifact.retracted` | User **Reject** (or future generic delete API) |

| Event | Handler outcome |
| ----- | ---------------- |
| **`artifact.facet.persisted`** | Assign facet into **`writeScopeUrns[]`** using **WI-352** merger |
| **`artifact.retracted`** | Kind-specific cleanup — facet handler removes scope rows + deletes artefact |

**Publish path:** [`ArtifactObserver`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/ArtifactObserver.kt) invoked from [`StandardPersistenceProjector`](../../../../ai/mill-ai/src/main/kotlin/io/qpointz/mill/ai/persistence/StandardPersistenceProjector.kt) post-save — **not** inline in CAPTURE tool or REST controller.

**Generic retraction (locked):** REST publishes **`artifact.retracted`** with `{ artifactId, kind, conversationId, correlationId }`; **`EventConsumer`** beans route by **`kind`**. Facet handler tombstones scope rows by **`sourceArtifactId`** (= `correlationId`). SQL / other kinds register handlers later without changing the REST surface.

### 2. `ArtifactStore` extensions (`mill-ai` + `mill-ai-persistence`)

- `delete(artifactId)` (or soft-delete + hide from GET replay)
- Optional `status`: `pending` | `accepted` | `retracted` on `ArtifactRecord` / wire DTO

### 3. REST (`mill-ai-service`)

- `POST /api/v1/ai/chats/{chatId}/artifacts/{artifactId}/accept`
- `POST /api/v1/ai/chats/{chatId}/artifacts/{artifactId}/reject`  
  → publishes **`artifact.retracted`**; does **not** call metadata delete inline in controller

### 4. Metadata scope write (`mill-metadata-*` + handlers)

Register **`EventConsumer`** beans via [`eventConsumer { on(...) }`](../../../../core/mill-events/src/main/kotlin/io/qpointz/mill/events/dsl/EventConsumerDsl.kt) in **`mill-metadata-autoconfigure`** or **`mill-ai-autoconfigure`** (discovered by [`EventsAutoConfiguration`](../../../../core/mill-events-autoconfigure/src/main/kotlin/io/qpointz/mill/events/configuration/EventsAutoConfiguration.kt)):

- **`FacetArtifactPersistedHandler`**: read artefact body → **`FacetProposalMerger.planAssignments`** → persist scope facet rows with **`sourceArtifactId`** = artefact id
- **`FacetArtifactRetractedHandler`**: tombstone assignments where **`sourceArtifactId`** matches; **`ArtifactStore.delete`**
- **Chat scope only** for v1 — never auto-assign to **global** from capture

### 5. UI (`mill-ui`)

- **`FacetCondensedPreview`** (or wrapper): **Accept** / **Reject** when `status === pending`
- Accept → call accept REST; disable buttons / show locked state
- Reject → call reject REST; remove card from thread (optimistic + replay)

### 6. Design doc update ([`GAPS.md`](GAPS.md) §13)

- Rewrite [`ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md) — capture-time scope assign + Accept/Reject lifecycle
- Note new event types in [`general-event-bus.md`](../../../design/platform/general-event-bus.md) (catalog rows only)

## Out of Scope

- Global / team scope promotion from chat (remains operator / M-23)
- Full generic retraction for **SQL** artefacts (event shape only; handler stub OK)
- mill-events Kafka / outbox (**P-50** backlog)

## Acceptance Criteria

- [ ] **`ArtifactObserver`** publishes **`artifact.facet.persisted`** after projector save (not in capture tool)
- [ ] Successful capture → artefact on GET/SSE → facet visible in **chat scope** read APIs
- [ ] **Accept** sets artefact accepted; scope assignments remain
- [ ] **Reject** removes artefact from replay **and** reverses scope assignments for that capture
- [ ] Handlers registered via **`mill-events`** `EventConsumer` DSL; controller publishes events only
- [ ] Unit + `testIT` for persist handler and retract handler (facet kind)
- [ ] Vitest: Accept / Reject button wiring (mock fetch)

## Suggested commit

`[feat] WI-353: facet artefact lifecycle with scope assign and Accept/Reject`
