# AI v3 chat — conversation-derived metadata & chat-scoped facets

This document is the **normative design** for how **candidate metadata** produced during a chat (especially **facet-shaped proposals**) is attributed to a **`chatId`**, **promoted** under **explicit user intent**, and **fed back** into subsequent agent turns—without silently overwriting global or team catalogues.

**Transport and structured SSE parts** (how proposals may appear on the wire) are described in [`ai-v3-chat-transport-extensions.md`](./ai-v3-chat-transport-extensions.md). This file owns **scope**, **prelude**, **merge precedence**, and **lifecycle** vocabulary.

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

## User action and idempotency

- **No silent writes:** material is **not** promoted to any durable scope until the user confirms (label TBD: **Promote**, **Save to chat context**, **Pin**, etc.).
- **Idempotency:** promotion should be safe to retry with a **client-supplied idempotency key** or **server hash** of `(chatId, facetType, entityRef, normalized payload)` to avoid duplicate assignments.

## Storage model (recommendation + alternates)

**Recommended (conceptual):** a **chat-scoped projection** keyed by `chatId`, loaded by the AI v3 runtime on each `sendMessage`:

- **Option A — logical scope in AI v3 persistence:** a sidecar table or JSON projection `chat_metadata_scope(chat_id, …)` holding **promoted** facet rows and tombstones. Reads merge into **`MetadataReadContext`** before the agent runs.
- **Option B — Metadata REST with explicit `scope`:** `POST` facet assignments to the Metadata service with scope = **chat** (exact slug pattern TBD with platform bootstrap). **Permission boundaries** must forbid arbitrary clients from writing catalogue scopes.
- **Option C — slug per chat** (e.g. `mill.chat.<chatId>`): simple mentally but may collide with resolver rules; requires platform sign-off.

Implementers pick **one** primary path; alternates remain **design alternates**, not duplicate live stores.

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

## Cross-links

- [`ai-v3-chat-transport-extensions.md`](./ai-v3-chat-transport-extensions.md) — SSE / replay mapping for structured parts including facet proposals
- [`schema-facet-ai-tool-field-mapping.md`](../metadata/schema-facet-ai-tool-field-mapping.md) — how schema tools consume facet-shaped catalogue data
- [`metadata-documentation.md`](../metadata/metadata-documentation.md) — facets & scopes overview

## Reviewer sign-off (metadata + AI owners)

Fill before merge when this doc is normative for the MR:

- [ ] **Metadata owner** — name, date
- [ ] **AI owner** — name, date
