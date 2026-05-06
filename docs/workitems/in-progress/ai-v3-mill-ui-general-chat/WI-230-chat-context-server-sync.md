# WI-230 — ChatContext server as source of truth

Status: `done`  
Type: `✨ feature`  
Area: `ui`  
Story: [`STORY.md`](STORY.md) — follows **WI-229**.

## Depends on

- **WI-229** (**`ChatService`** + profiles + SSE progress API).

## Reviewer checklist

- General chat list/detail/rename/delete/stream all hit server; **no localStorage authority** when REST mode is on — **cutover** per acceptance (**no** stale local list before first server fetch; **no** `setItem` while REST active).
- **`thinkingMessage`** driven by **`item.diagnostic`** (and tame tools), not a static **`Thinking...`** once the server emits diagnostics.
- **`chatAgentPicker`**: gated in [`defaults`](../../../../ui/mill-ui/src/features/defaults.ts); picker only affects **new** chats (document **same `profileId`** on **`sendMessage`** auto-create vs **New chat**).
- **`viewChat`** default **`true`** after **WI-230** aligns with **Story** expectation; **verify** **operator doc** for **`mill.ai.enabled` off** deployments (remote **`viewChat: false`** or keep default **false**).

## Goal

Drive **General Chat** from the API: sidebar list from `GET /api/v1/ai/chats`, open chat loads messages via chat detail endpoint, delete and rename call backend. Stop treating **localStorage** as the authoritative store when REST mode is active (avoid split-brain with server).

## Acceptance criteria

- [x] **`REST / localStorage` cutover (normative)** — When REST backend is active per **`isRestChatBackendActive()`** ([`chatService.ts`](../../../../ui/mill-ui/src/services/chatService.ts); Vitest stays on mocks):
  - Do **not** **`dispatch(LOAD_CONVERSATIONS)`** from **`localStorage`** (`STORAGE_KEY` **`chat-conversations`** in [`ChatContext.tsx`](../../../../ui/mill-ui/src/context/ChatContext.tsx)): REST boot **`removeItem`s** storage then **`listChats()`** — **never** hydrated from cached threads.
  - While REST active: **disable** the `useEffect` that **`setItem`** on every `state.conversations` change (otherwise split-brain returns).
  - **First paint / flicker:** until the first **`listChats()`** resolves, **`initialized`** stays **false** and chrome disables new chat/send — sidebar **empty**.
  - **User messaging (optional):** deferred (story allows skip).
- [x] **`viewChat`** (**General Chat**): **`defaultFeatureFlags.viewChat: true`** in [`defaults.ts`](../../../../ui/mill-ui/src/features/defaults.ts). Operators without AI remain expected to **`GET /api/v1/features`** override or ship **`viewChat: false`** (**[`STORY.md`](STORY.md)**).
- [x] **[`FEATURE-FLAGS.md`](../../../../ui/mill-ui/docs/FEATURE-FLAGS.md) reconciliation:** **[`FeatureFlagContext.tsx`](../../../../ui/mill-ui/src/features/FeatureFlagContext.tsx)** merge semantics clarified in Architecture; inventory **Default** columns synced with [`defaults.ts`](../../../../ui/mill-ui/src/features/defaults.ts) (**incl.** `chatAgentPicker`).

- [x] Initial load (REST): fetch general chat list → map to sidebar `Conversation` summaries
- [x] Selecting a conversation (`transcriptHydrated === false`): load transcript via **`getChatDetail`**
- [x] `deleteConversation`: **`DELETE`** (tolerate failure); update local state
- [x] `renameConversation`: **`PATCH`**; update local state
- [x] **`sendMessage`**: SSE **`onProgress`** → **`thinkingMessage`** (no literal **`Thinking…`** preload)
- [x] **`createConversation`** and **`sendMessage`** auto-create use **`resolveGeneralChatAgentProfileId()`** (picker → **sessionStorage** → **`VITE_MILL_AI_PROFILE`** → omit)
- [x] **`chatAgentPicker`** gates **`Select`** in [`AppShell.tsx`](../../../../ui/mill-ui/src/components/layout/AppShell.tsx); **`listAgentProfiles`** via **`ChatContext`**
- [x] **`profileId`** surfaced in **`ChatArea`** badge + **`Sidebar`** description after transcript merge
- [x] **Inline chat**: KDoc policy on [`InlineChatContext.tsx`](../../../../ui/mill-ui/src/context/InlineChatContext.tsx); **`createChat`** passes **`resolveGeneralChatAgentProfileId()`**
- [x] Inline **`thinkingMessage`** via **`ThinkingIndicator`** in **`InlineChatPanel`**

## Primary paths

- [ui/mill-ui/src/context/ChatContext.tsx](../../../../ui/mill-ui/src/context/ChatContext.tsx)
- [ui/mill-ui/src/components/layout/AppShell.tsx](../../../../ui/mill-ui/src/components/layout/AppShell.tsx)
- [ui/mill-ui/src/features/defaults.ts](../../../../ui/mill-ui/src/features/defaults.ts)
- [ui/mill-ui/src/features/FeatureFlagContext.tsx](../../../../ui/mill-ui/src/features/FeatureFlagContext.tsx)
- [ui/mill-ui/docs/FEATURE-FLAGS.md](../../../../ui/mill-ui/docs/FEATURE-FLAGS.md)
- [ui/mill-ui/src/components/layout/Sidebar.tsx](../../../../ui/mill-ui/src/components/layout/Sidebar.tsx) (if list/delete wiring needs tweaks)

## Out of scope

- `chatReferencesService` mock (unchanged unless trivial alignment)
