# AI v3 chat — conversation-derived metadata & chat-scoped facets

This document is the **normative design** for how **candidate metadata** produced during a chat (especially **facet-shaped proposals**) is attributed to a **`chatId`**, **promoted** under **explicit user intent**, and **fed back** into subsequent agent turns—without silently overwriting global or team catalogues.

**Transport and structured SSE parts** (how proposals may appear on the wire) are described in [`ai-v3-chat-transport-extensions.md`](./ai-v3-chat-transport-extensions.md). This file owns **scope**, **prelude**, **merge precedence**, and **lifecycle** vocabulary.

**Story:** [`metadata-authoring-profiles`](../../workitems/completed/20260629-metadata-authoring-profiles/STORY.md) (**WI-360** lifecycle, **WI-354**–**WI-364**).

## Facet proposal artefact (conversation-sourced)

A **facet proposal** is a structured outcome of an assistant or tool turn that suggests **metadata assignments** (entity ref, **`facetType`**, provisional JSON payload, provenance). Sketch fields (non-normative wire until implemented):

| Field | Role |
|-------|------|
| `entityRef` | Target entity URN or stable id the proposal applies to |
| `facetType` | `urn:mill/metadata/facet-type:…` |
| `payload` | Provisional facet JSON (subject to validation on promotion) |
| `provenance` | `chatId`, assistant `itemId` / turn id, optional model/capability ids |
| `confidence` | Optional ranking for UI only |

Proposals may surface as future structured `item.part.updated` rows (see transport doc). The **text bubble** remains independent; proposals are **ignored** by the thin client until a dedicated renderer or promotion flow exists.

### Capture vs metadata-scope merge (authoring tools — locked GAPS §3c)

**Chat scope URN:** `urn:mill/metadata/scope:chat-<chatId>` (conversation GUID).

When the agent uses **`list_metadata_scopes`** before **`propose_facet_assignment`**:

| `list_metadata_scopes` result | Chat artefact (SSE/replay) | Metadata scope |
| ----------------------------- | -------------------------- | -------------- |
| **Writable scope** (HTTP chat: chat scope; `writable: true`) | **Persisted** | **`scopeUrn`** on artefact for **consumer merge** (promotion UI, M-23, MCP). No silent Metadata REST write. |
| **Global only** (`writable: false`) | — | **`metadata-authoring`** must **not** capture to global in chat — read-only for authoring. |
| **Empty `[]`** | **Persisted** (orphan proposal) | **Not** merged into any metadata scope. **Consumer responsibility**. |

**Read vs write:** **`metadata`** QUERY tools consume merged metadata from **all** scopes (global ∪ chat per **`MetadataReadContext`**). **`metadata-authoring`** CAPTURE targets **`writable: true`** scopes only; default writable scope lives on **`AgentContext`**.

## Chat scope registry (`metadata_scope`) — **LOCKED**

Each conversation gets a **`metadata_scope`** row (not a separate AI sidecar). Created **idempotently** when the chat is first used for metadata-aware agent work.

| Column / domain field | Value |
| --------------------- | ----- |
| **`scope_res`** | `urn:mill/metadata/scope:chat-<chatId>` |
| **`scope_type`** | **`CHAT`** |
| **`reference_id`** | **`chatId`** |
| **`display_name`** | **`Chat <title>`** — `<title>` = persisted chat name (`chatName`) |
| **`owner_id`** | Chat owner **`userId`** |
| **`visibility`** | **`PRIVATE`** |

**Lifecycle:** create on first agent turn if missing; **update `display_name`** when the user renames the chat. Promoted facet assignments reference this scope URN via normal Metadata facet-instance storage.

**Supersedes** prior “Option A sidecar only” recommendation — **Option B (Metadata `metadata_scope`)** is the primary store. Option A remains a historical alternate in backlog notes only.

## User action and idempotency

- **No silent writes:** material is **not** promoted to any durable scope until the user confirms (label TBD: **Promote**, **Save to chat context**, **Pin**, etc.).
- **Idempotency:** promotion should be safe to retry with a **client-supplied idempotency key** or **server hash** of `(chatId, facetType, entityRef, normalized payload)` to avoid duplicate assignments.

### Merge precedence

Promoted chat-scope facets participate in **`MetadataReadContext`** resolution together with catalogue defaults and team/global sources. Ordering and **`merge_action`** semantics must align with [`metadata-documentation.md`](../metadata/metadata-documentation.md), [`mill-metadata-domain-model.md`](../metadata/mill-metadata-domain-model.md), and [`metadata-layered-sources-and-ephemeral-facets.md`](../metadata/metadata-layered-sources-and-ephemeral-facets.md). **There must be no silent promotion into global scopes.**

## Message API ↔ facet prelude (normative)

Today’s [`SendMessageHttpRequest`](../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) carries **`message` only** for `POST …/chats/{id}/messages`. For this wave the UI **does not** attach pinned facet payloads in the HTTP body.

**Primary model (recommended):** **`chatId`-scoped server-side enrichment** — on each `sendMessage`, the stack loads **persisted promoted facet projection** for that `chatId` (once persistence exists), resolves **`MetadataReadContext`** / prelude **before** invoking the agent. The client trusts the server to assemble the same effective context policy users approved in-chat.

**Alternate (explicit request — backlog):** extend the send DTO or add `PATCH`/correlate endpoints so the UI sends prelude blobs. Trade-offs: **bandwidth**, **tampering**, **cache invalidation**, **version skew** versus server-side lookup. If adopted, the design must state **precedence** when both body and server store exist.

**Runtime contract:** Agent runs for `chatId` receive an **effective metadata read context** reflecting **pinned chat-scope facets** ∪ catalogue defaults ∪ team/global (e.g. **SchemaFacetCatalogAdapter**-class merge) via the chosen prelude model. Capabilities consume that context consistently; phased introduction of capability IDs that read chat-scope is a **follow-up backlog** item.

## Lifecycle (design reservation — follow-up story)

Users must eventually **list**, **deactivate**, and optionally **hard-delete** promoted rows per chat:

- **Deactivate / suppress:** prefer **overlay** semantics — e.g. **`TOMBSTONE`** / **`CLEAR`**-style **`merge_action`** so audit survives and **`MetadataReadContext`** suppresses stale assignments on the next run.
- **Delete:** hard removal when product allows; semantics for subsequent agent runs must be explicit (effective context no longer sees the assignment).

Candidate API sketches only (no implementation in this wave):  
`GET /api/v1/ai/chats/{chatId}/metadata-scope/promotions`, `PATCH { active: false }`, `DELETE …`, or reuse Metadata REST with **`scope`** and authz headers.

### Optional UI scaffolding

mill-ui **optional stub** (**`chatMetadataPromotion`**, default **false** — see **WI-233** WI file) may wire a **no-op** handler to reserve the seam; defer full UX until persistence exists.

## Open API candidates (no mandated implementation here)

| Approach | Summary |
|---------|---------|
| `POST …/ai/chats/{chatId}/metadata-scope/promotions` | Dedicated AI-plane promotion; simplifies authz coupling to chats |
| Metadata `POST` with scope | Reuse Metadata mutations; needs scope + permission model |
| Prelude-only patch | `PATCH` chat prelude blob — alternate to server-side store; watch tampering |

## Facet artefact lifecycle (WI-360 — implemented)

- **`POST …/artifacts/{artifactId}/accept`** — pending → **accepted** (facet proposal locked).
- **`POST …/artifacts/{artifactId}/reject`** — publishes **`artifact.retracted`**; tombstones scope rows by **`sourceArtifactId`** and removes the chat artefact.
- **`artifact.facet.persisted`** — on facet artefact save with non-empty **`writeScopeUrns[]`**, assigns scope rows via **`FacetArtifactScopeService`**.
- Legacy **`schema-authoring.capture`** replay remains supported via **`FacetProposalWire`** and a registry fallback descriptor (capability removed in WI-361).

Operator lifecycle and event-bus wiring: [`metadata-facet-catalog-v3.md`](metadata-facet-catalog-v3.md).

## Cross-links

- [`ai-v3-chat-transport-extensions.md`](./ai-v3-chat-transport-extensions.md) — SSE / replay mapping for structured parts including facet proposals
- [`schema-facet-ai-tool-field-mapping.md`](../metadata/schema-facet-ai-tool-field-mapping.md) — how schema tools consume facet-shaped catalogue data
- [`metadata-documentation.md`](../metadata/metadata-documentation.md) — facets & scopes overview

## Reviewer sign-off (metadata + AI owners)

Fill before merge when this doc is normative for the MR:

- [ ] **Metadata owner** — name, date
- [ ] **AI owner** — name, date
