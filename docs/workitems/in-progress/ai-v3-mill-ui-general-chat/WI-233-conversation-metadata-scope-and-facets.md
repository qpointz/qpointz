# WI-233 — Conversation-derived metadata facets & chat-scoped scope

Status: `done`  
Type: `📐 design` (contracts + sequencing; minimal or no runtime code in this story)  
Area: `ai`, `metadata`, `ui`  
Story: [`STORY.md`](STORY.md) — **normative architecture** for facet promotion / chat scope (**implementation mostly follow-up stories**).

## Depends on

- Product intent from **`STORY`** + **WI-231** cross-links (technical transport is **WI-231**, not duplicated here).

## Reviewer checklist (metadata + AI owners)

- Single design file **`docs/design/agentic/ai-v3-chat-metadata-scope.md`** (or **one** agreed merge location) owns **promotion**, **`chatId`↔scope**, **capability prelude / merge precedence**, **lifecycle sketch** (**deactivate** / **delete** follow-up).
- Alignment with **`MetadataReadContext`**, **`merge_action`**, layered sources (**links in acceptance criteria**) — **no silent global merges**.
- **Prelude ambiguity closed:** reviewer confirms design doc picks **server-side enrichment by `chatId`** as **primary**, with **alternate** explicit request-field API called out as **follow-up backlog** (**review finding #2**).
- **Optional** **`chatMetadataPromotion`** stub is **feature-flagged** and **no-op** unless justified.

## Goal

Conversations produce **candidate metadata** (e.g. **facet assignments** surfaced by the assistant or tooling). That material must eventually:

1. Be **attributable to this chat** (not silently merged into global catalog until policy says so).
2. Be **storable under explicit user intent** (**promote / save / pin** — exact UX label TBD) into a **`chat`-specific metadata scope** (or equivalent abstraction aligned with Mill’s facet **scope** model — see [metadata-documentation.md](../../../design/metadata/metadata-documentation.md)).
3. Become **inputs to subsequent turns** inside the **same chat** so **metadata-related capabilities** (schema exploration, RAG over facets, value-mapping hooks, etc.) **see** the user-approved rows when the agent/runtime builds context for each turn (**not** by implying the UI sends extra fields in [`SendMessageHttpRequest`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) — see acceptance **Message API ↔ facet prelude**).
4. Remain **manageable** over the life of the chat: users must eventually be able to **revoke** or **soft-disable** promoted facets so stale context does not stick forever. Align vocabulary with platform metadata: **`merge_action`** (**`SET`**, **`TOMBSTONE`**, **`CLEAR`**) and **hard delete** rules for captured assignments under a scope, as in [`mill-metadata-domain-model.md`](../../../design/metadata/mill-metadata-domain-model.md) §4–5 and read resolution via **`MetadataReadContext`** / scope ordering in [`metadata-layered-sources-and-ephemeral-facets.md`](../../../design/metadata/metadata-layered-sources-and-ephemeral-facets.md). **Implementing** list / deactivate / delete UX and backing APIs is a **follow-up story**; this WI only **reserves** extension points and documents the intended contract.

This WI is **primarily specification**: naming the scope model, linkage **`chatId` ↔ scope**, promotion flow (UI gesture + REST/command stub), **lifecycle hooks (sketch only)**, and how agent runs **merge** persisted chat-scope facets with catalogue defaults. **`WI-229–232`** remain focused on wired transport; **`WI-231`** transport-extensions doc must **cross-link** metadata-facet artefacts to this WI.

## Acceptance criteria

- [x] **Design artefact**: add **`docs/design/agentic/ai-v3-chat-metadata-scope.md`** (or merged section inside **`WI-231`** transport doc — single owner only) documenting:
  - **Artefact type**: conversation-sourced **facet proposal** (payload shape sketch: entity ref, **`facetType`**, provisional payload JSON, provenance **`itemId` / turn**).
  - **User action**: **explicit confirm** before persistence (no silent writes); idempotency considerations.
  - **Storage model options** (pick one recommendation + alternates): e.g. **dedicated scope slug per chat** (`mill.chat.<chatId>` pattern **TBD**, conflicts with resolver); **logical scope** keyed in **AI v3 persistence** sidecar; vs **Metadata API** **`POST`** with **`scope`** when service supports programmatic assignment — note **permission** boundaries.
  - **Message API ↔ facet prelude (normative — closes review gap):** Today’s [`SendMessageHttpRequest`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) carries **`message` only** (`POST …/messages` — [`AiChatController`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt)). The UI **does not** attach pinned facet payloads per message for this wave.
    - **Primary recommended model:** **`chatId`-scoped server-side enrichment** — on each **`sendMessage`**, the AI v3 stack loads **persisted promoted facet projection** for that chat (once persistence exists), merges scopes per metadata rules, and supplies **`MetadataReadContext`** / agent prelude **before** running the agent. **Alternate (follow-up backlog):** extend **`SendMessageHttpRequest`** (or add `PATCH` chat prelude / correlate endpoint) — document trade-offs (**bandwidth**, **tampering**, **caching**) vs server-side lookup.
    - **Runtime contract** wording below MUST use this split so implementers cannot read “attach on sendMessage body” vs “server resolves by **`chatId`**” two different ways **without choosing one as primary**.
  - **Runtime contract** (adjusted): agent runs for chat **`chatId`** receive **effective metadata read context** reflecting **pinned chat-scope facets** ∪ catalogue defaults ∪ team/global (**SchemaFacetCatalogAdapter**-class merge) via the **chosen prelude model** (**server-side default** vs **explicit request**, above).
  - **Lifecycle (design reservation — follow-up story)**: sketch how users **list** promoted rows for a chat, **deactivate** (prefer **overlay** semantics — e.g. **`TOMBSTONE`** / **`CLEAR`**-style suppression so audit remains) vs **delete** (hard removal when product allows), and how that changes **`MetadataReadContext`** or chat-local projection for the next agent run. Candidate API shapes only, e.g. `GET` chat-scope facet rows, `DELETE` assignment, `PATCH { active: false }` — **no implementation** in this story wave.
  - **Open API candidates** (no implementation mandated here): e.g. `POST /api/v1/ai/chats/{chatId}/metadata-scope/promotions` carrying facet rows; **or** reuse Metadata REST with scope header — list trade-offs.

- [x] **Cross-links**: from **`WI-231`** artefact bullets and from [schema facet / AI mappings](../../../design/metadata/schema-facet-ai-tool-field-mapping.md) — short pointer added.

- [ ] **`mill-ui`** (optional scaffolding only): stub **promote-to-chat-scope** … — **deferred** (no `chatMetadataPromotion` stub in this MR; extension reserved in design doc).

## Out of scope (here)

- Full metadata service mutations, migrations, authoritative scope naming in **`platform-bootstrap.yaml`**
- Teaching every capability automatically — phase-in list of **capability IDs** consuming chat scope (**follow-up backlog**).
- **Manageability product**: dedicated UI to browse / **deactivate** / **delete** chat-scope facet rows, and production REST for those operations — reserved in the design doc above; **implement in a follow-up story** once chat scope persistence exists.

## Suggested backlog labels

`ai-v3-chat-metadata-scope`, `metadata-chat-facet-promotion`, `ai-v3-context-from-chat-facets`, `ai-v3-chat-scope-facet-lifecycle`
