# ai-v3 — mill-ui general chat (REST/SSE + multi-agent + artefact hooks)

Wire [ui/mill-ui](../../../../ui/mill-ui) **General Chat** and shared **`chatService`** (including inline chat consumers) to the **unified AI v3** HTTP API under `/api/v1/ai/chats`, replacing mock-only behaviour with **server-backed** list / create / load history / rename / delete / streaming message send.

**Baseline parity:** conversational flow aligns with [`mill-ai-v3-cli`](../../../../ai/mill-ai-v3-cli) (REST + SSE), adapted for browser `fetch`, cookies, and the existing **`ThinkingIndicator`** surfaces.

**Breaking-change policy (two layers):**

1. **Public SSE / wire contract — forward compatible:** mill-ui MUST follow the transport rules in [`ChatSseEvent.kt`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) (e.g. **V1 frozen** `presentation` / `partType` for text; **future** structured values **reserved**). For **`item.part.updated`**, a text-first UI **must not fail the stream** on unknown `presentation` / `partType` — it **skips** applying unknown combinations to the main text bubble and may pass them to the **WI-231** extension hook / dev logging (same spirit as **ItemToolCall** KDoc: thin consumers ignore safely). Unknown top-level SSE **`type`** strings: **skip** without crashing (unless JSON is unparseable — treat as transport error).

2. **mill-ui-only breaks (no deprecation shims):** TypeScript **`ChatService`** shapes, **`mockChatService`** fixtures, and **`localStorage`** (`chat-conversations` today) **may change or be cleared aggressively** when switching to REST — **do not** keep dual legacy parsers or migrate old cached threads unless product explicitly asks (**WI-230** defines cutover).

This story **does not** require “hard error on unknown `partType`” for SSE — that would **contradict** the intentional forward-extensible server contract (**review finding #1**).

---

## For reviewers

- **Suggested read order:** this file → **`WI-229`** (HTTP + SSE contract) → **`WI-230`** (state + UX) → **`WI-231`** (artefact seam) ↔ **`WI-233`** (metadata scope spec; overlaps **WI-231** cross-links) → **`WI-232`** (verification + design sign-off).
- **Risk areas:** SSE parity with CLI + **forward-compatible** handling per [`ChatSseEvent`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) (unknown structured parts **no-op** in text UI, **not** hard-fail); **`credentials: 'include'`** on **all** `/api/v1/ai/**` calls including **`GET /api/v1/ai/profiles`**; **`item.diagnostic`** / tool anti-spam; **general vs inline** `profileId` policy; **`WI-231`/`WI-233`** duplication — **single normative owner** for metadata-scope prose; **localStorage cutover** without flicker (**WI-230**); **`viewChat`** vs **`mill.ai.enabled`** (see **Feature flags** row); AI-off stacks must remote-disable **`viewChat`** or omit default flip.
- **MR expectations:** `./gradlew` unchanged unless a WI explicitly touches **`ai/`** (e.g. KDoc); **`ui/mill-ui`** must pass **`npm run test`** / **`npm run build`** (**`WI-232`**); MR description summarizes commands + manual smoke; **`WI-233`** needs metadata + AI owner **ack** (see **`WI-232`**).
- **Implement in order:** **WI-229 → WI-230 → WI-231** (parallel design work ok for **WI-233**) → **`WI-232`** last for the wired stack; **`WI-233`** design doc may land in the **same MR** as **WI-231** or an adjacent one — track checkbox in **`WI-233`**.

---

**Story archive:** `docs/workitems/completed/20260506-ai-v3-mill-ui-general-chat/` (this folder; closed **2026-05-06**).  
**Delivery branch:** `feat/mill-ui-general-chat`.

## Work Items

Implementation order:

- [x] **WI-229** — REST + SSE client, profiles **`GET`**, `profileId` on create, streaming progress (**[`WI-229-rest-sse-chat-service-client.md`](WI-229-rest-sse-chat-service-client.md)**)
- [x] **WI-230** — `ChatContext` server source of truth, SSE wait UX, **reserved agent picker** (**[`WI-230-chat-context-server-sync.md`](WI-230-chat-context-server-sync.md)**)
- [x] **WI-231** — Chat **artefact** transport extension points (design + TS/K scaffolding only) (**[`WI-231-chat-artefact-extension-points.md`](WI-231-chat-artefact-extension-points.md)**)
- [x] **WI-233** — **Conversation-derived metadata facets** → **chat-scoped** storage & capability context (**[`WI-233-conversation-metadata-scope-and-facets.md`](WI-233-conversation-metadata-scope-and-facets.md)**)
- [x] **WI-232** — Vitest + manual verification (**[`WI-232-tests-verification-mill-ui-chat.md`](WI-232-tests-verification-mill-ui-chat.md)**)

## Scope summary

| Area | Intent |
|------|--------|
| **REST** | Sidebar + detail: **`GET`** chats, **`GET`** chat+m messages, **`POST`** create (`profileId`, context optional), **`PATCH`** rename, **`DELETE`**. Credentials **`include`** when security is on. |
| **SSE** | **`POST …/messages`**: parse **`data:`** JSON — **existing** event types only ([`ChatSseEvent`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt)); append text deltas; **no new server event taxonomy** for this story. |
| **Wait UX** | **`item.diagnostic`** drives **`thinkingMessage`** / [`ThinkingIndicator`](../../../../ui/mill-ui/src/components/chat/ThinkingIndicator.tsx) (replace static “Thinking…”); **`item.tool.*`** as **short** cues (debounce / collapse if noisy); clear on first text delta or **`completed`** / **`failed`**. |
| **Multi-agent** | **`GET /api/v1/ai/profiles`** for registry; **`profileId`** locked at **`POST`** create; feature-flag **`chatAgentPicker`** (default **off**) reserves **`Select`** in general-chat chrome (**`WI-230`**); fallback chain: picker → **`sessionStorage`** last-used → **`VITE_MILL_AI_PROFILE`** → omit → server **defaultProfile**. |
| **Feature flags** | **`viewChat`:** set **`viewChat: true`** in [`defaultFeatureFlags`](../../../../ui/mill-ui/src/features/defaults.ts) (**`WI-230`**) so **`/chat`** is available **without** remote merge during dev/review. **Operational coupling:** [`AiChatController`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt) / [`AiProfileController`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiProfileController.kt) beans exist **only when** **`mill.ai.enabled`**. Builds **without** AI must therefore **either** ship **`viewChat: false`** via [`GET /api/v1/features`](../../../../ui/mill-ui/src/services/api.ts) (partial JSON overriding default) **or** keep **`viewChat` false** until AI is enabled — otherwise **`/chat`** surfaces a broken API. **`WI-230`** documents this contract; optional **graceful Chat unavailable** UX is backlog if not implemented here. |
| **State** | **General Chat:** drop localStorage as source of truth when REST mode active. **Inline:** shared service; **`profileId`** policy documented (**`WI-230`**). |
| **Artefacts (north star)** | SQL / metadata / charts as **durable chat artefacts**: **§ design + seams only** (**`WI-231`** — persistence & execute-SQL are **later**). |
| **Chat metadata scope** (**`WI-233`**) | **Facet-shaped** artefacts from the conversation promoted by **explicit user action** into a **`chat`-bound metadata scope**; **metadata-facing capabilities** on **later turns** must **respect** that scope (merge precedence vs global/team scopes per domain rules). **Manageability** (**list / deactivate / delete** promoted rows, aligned with **`MetadataReadContext`** and **`merge_action`** semantics) is **designed and backlog-reserved** — **follow-up story** after persistence exists. Specification + stubs only in this story wave unless a minimal no-op hook is justified. |

## Out of scope (this story)

- Execute generated SQL from chat UI, charts, persisted structured artefact JSON in DB / transcript (**design hooks only** here).
- New SSE **`type`** strings invented only for prettier spinners (**use diagnostics + tame tool cues**).
- Rich inline **tool inspector** (optional minimal status line only).
- Regenerating **OpenAPI clients** — hand-written **`fetch`** per mill-ui norms.
- **Chat-scope facet lifecycle product** (**list / deactivate / delete** UI + REST) — specified and backlog-labelled in **`WI-233`**; **follow-up story** once persistence exists.

## North star (follow-ups)

Capture **SQL, results metadata, charts, entity refs** as first-class artefacts (emit ↔ replay ↔ persist). Separately (**`WI-233`**), ensure **conversation-sourced facets** can be **user-promoted** into a **chat-specific metadata scope** so **_metadata capabilities_** (schema agents, facet RAG, etc.) **consume** them on **subsequent questions** in the **same chat** — without overwriting global catalogue until policy allows promotion upstream. Users must later be able to **turn off or remove** stale chat-scope facets (**deactivate** / **delete**); that **lifecycle** is specified and extension-reserved here, **implemented** in a **follow-up**.

This story prepares **typed extension seams** and **coordination specs** without delivering full parity to legacy grinder data cards nor full Metadata service UX.

## References

- [Story workflow & placement](../../RULES.md)
- [GENERAL-CHAT-DESIGN.md](../../../design/ui/mill-ui/GENERAL-CHAT-DESIGN.md) — compositional message model (legacy grinder **intent**, not copied code).
- [Metadata facets & scopes](../../../design/metadata/metadata-documentation.md#metadata-facets) — **scope** precedence / merge semantics grounding **`WI-233`**
- [Metadata domain model — scopes & merge_action](../../../design/metadata/mill-metadata-domain-model.md)
- [Schema facet payloads ↔ AI tools](../../../design/metadata/schema-facet-ai-tool-field-mapping.md) — capability-side consumption patterns
- [`FEATURE-FLAGS.md`](../../../../ui/mill-ui/docs/FEATURE-FLAGS.md) — **`WI-230`**: **Architecture** § remote merge must match [`FeatureFlagContext.tsx`](../../../../ui/mill-ui/src/features/FeatureFlagContext.tsx); **Default** columns vs [`defaults.ts`](../../../../ui/mill-ui/src/features/defaults.ts) (**full reconciliation**, not **`viewChat`** only)
- [`AiChatController`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiChatController.kt) — `/api/v1/ai/chats`
- [`AiProfileController`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/AiProfileController.kt) — `/api/v1/ai/profiles`
- [`AgentProfileResponse` / chat DTOs](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/)
- [`ChatSseEvent`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) — SSE payloads
- [`AiChatSettings.defaultProfile`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/chat/AiChatSettings.kt) — server default when **`profileId` omitted**
- Related planned story [`../ai-v3/STORY.md`](../ai-v3/STORY.md) (**WI-082**) — broader v3 / UI integration
