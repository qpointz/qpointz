# WI-229 — REST + SSE chat service client (mill-ui)

Status: `done`  
Type: `✨ feature`  
Area: `ui`  
Story: [`STORY.md`](STORY.md) — **implement first**.

## Depends on

- Nothing (foundation WI).

## Reviewer checklist

- Parity with [`CliApp.kt`](../../../../ai/mill-ai-v3-cli/src/main/kotlin/io/qpointz/mill/ai/cli/CliApp.kt) and [`ChatSseEvent`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt) (**no imaginary event types**).
- **`credentials: 'include'`** on every AI **`fetch`** (chats **and** **`GET /api/v1/ai/profiles`**).
- **`profileId`** optional on **`POST /chats`**; **`listAgentProfiles`** matches [`AgentProfileResponse`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ProfileDtos.kt); **chat JSON types** stay aligned with [`ChatDtos.kt`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) + [`ChatSseEvent`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt).
- **Downstream:** unblocks **`WI-230`** (context), **`WI-231`** (SSE reducer hook), **`WI-232`** (tests).
- **Breaking-change posture:** **`realChatService`** + types may replace mock contracts outright; **`mockChatService`** tracks **current** DTO/SSE shapes only—not a compatibility layer for older mill-ui payloads. **SSE parsing** remains **forward tolerant** per **`STORY.md`** (unknown structured parts **no-op** in text bubble, **not** session-kill).

## Goal

Implement a **`realChatService`** in mill-ui that calls `mill-ai-v3-service` via the browser:

- JSON: list chats, create chat, get chat detail + transcript, PATCH rename, DELETE
- SSE: `POST …/chats/{id}/messages` with `fetch` + stream parsing (`data:` JSON lines)

Use `credentials: 'include'` for session cookies when `mill.security.enable=true`.

## Acceptance criteria

- [x] All **`fetch`** calls for **`/api/v1/ai/chats`**, **`/api/v1/ai/profiles`**, and related AI JSON/SSE endpoints use **`credentials: 'include'`** (same session-cookie contract as [`authService`](../../../../ui/mill-ui/src/services/authService.ts); **profiles must not** be an accidental omission — **review open question**).
- [x] **JSON ↔ TypeScript DTO parity:** Types for chat REST payloads **match** Kotlin wire shape (field names + nullability) — at minimum **[`ChatResponse`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt)**, **[`ChatDetailResponse`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt)** (nested **`chat`** + **`messages`**), **`TurnResponse`**, **`CreateChatHttpRequest`** / outbound create body, **`UpdateChatHttpRequest`**, **`SendMessageHttpRequest`**. SSE: parsed **`data:`** objects align with **[`ChatSseEvent`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt)** JSON (discriminated by **`type`**). **Purpose:** reviewers can diff TS types ↔ backend when **`ChatDtos`** / SSE evolve — **avoid silent wire drift** (**review finding — DTO parity**).
- [x] `ChatService` extended as needed: `getChatDetail`, `deleteChat`, `renameChat` (names per implementation), plus existing methods
- [x] SSE: handle **only** event types the backend already emits ([`ChatSseEvent`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/sse/ChatSseEvent.kt)): `item.part.updated` (text + append/replace), `item.diagnostic`, `item.tool.call` / `item.tool.result` (for **brief** wait-state cues), `item.completed`, `item.failed`, optionally `item.created` — **no new SSE event kinds** on server for this story
- [x] **Wait-state UX contract**: raise progress via **`onProgress` / tagged stream** wired from **`item.diagnostic`** (primary status text); **`item.tool.*`** → concise combined status (avoid dumping full JSON / per-tick flicker — debounce/coalesce if needed); clear when first text arrives or turn completes/fails
- [x] `item.completed` (no double-content when deltas used), `item.failed` surfaced to bubble + clear thinking
- [x] Non-text `item.part.updated` delegated to stub/extension path (wired fully in **WI-231**)
- [x] Mock implementation updated for widened interface; env or export toggle to choose mock vs REST (default REST for dev)
- [x] **`listAgentProfiles()`** (on `ChatService` or small companion module re-exported from [`api.ts`](../../../../ui/mill-ui/src/services/api.ts)): `GET /api/v1/ai/profiles`, types aligned with [`AgentProfileResponse`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ProfileDtos.kt) (`id`, `capabilityIds`)
- [x] **`CreateChatParams`** includes optional **`profileId`**; **`createChat`** sends it in JSON body when set; when picker off + no env, **omit** so server uses [`defaultProfile`](../../../../ai/mill-ai-v3/src/main/kotlin/io/qpointz/mill/ai/chat/AiChatSettings.kt); **`VITE_MILL_AI_PROFILE`** remains dev default when no UI selection
- [x] **`mockChatService`**: emit at least one **diagnostic-equivalent** progress update before text chunks so **ThinkingIndicator / wait UX** paths are covered in tests without hitting the backend
- [x] **Mock**: stub **`listAgentProfiles`** with ≥2 fake **`id`s** so picker / `profileId` branching is testable offline

## Primary paths

- [ui/mill-ui/src/services/chatService.ts](../../../../ui/mill-ui/src/services/chatService.ts)
- [ui/mill-ui/src/types/chat.ts](../../../../ui/mill-ui/src/types/chat.ts)
- [ui/mill-ui/src/services/api.ts](../../../../ui/mill-ui/src/services/api.ts)
- [`ChatDtos.kt`](../../../../ai/mill-ai-v3-service/src/main/kotlin/io/qpointz/mill/ai/service/dto/ChatDtos.kt) (**Kotlin wire reference** for REST JSON parity)

## Out of scope

- Artefact persistence, execute-SQL endpoint, charts (design only in WI-231)
