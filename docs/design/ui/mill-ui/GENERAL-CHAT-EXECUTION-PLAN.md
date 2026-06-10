# General Chat -- Execution Plan

Execution tasks and tracking for the General Chat redesign. See [GENERAL-CHAT-DESIGN.md](GENERAL-CHAT-DESIGN.md) for architecture, types, and design decisions.

**Approach**: Mock-first, UX-first. Each phase ships a complete UX with mocked data before any backend wiring.

---

## Milestones

### Milestone 1: Foundation + Text (Phases L, S, 0, 1a, 1b) -- EXECUTE NOW

Delivers the full architecture end-to-end with text + markdown. Validates: layout, event model, reducer, service interface, streaming UX, feature flags. Test against real backend at the end.

### Milestone 2: Complex Message Types (Phases 2-7) -- DEFERRED

Execute after Milestone 1 is validated against the backend. Data grids, charts, and knowledge cards layer on the proven foundation.

---

## Milestone 1

---

### Phase L: Layout Overhaul -- COMPLETE

Pure layout and UX changes. Works with the **current** Message model. See [Layout Architecture](GENERAL-CHAT-DESIGN.md#layout-architecture) for the design.

| File | Change | Status |
|------|--------|--------|
| `src/components/chat/ChatArea.tsx` | Three-layer layout: pinned toolbar (top), scrollable content (middle), floating input (bottom). Transparent gradient overlays (no backdrop blur). ThinkingIndicator above input. | Done |
| `src/components/chat/MessageList.tsx` | Scroll padding for both overlays, scroll-to-bottom affix button via `useAutoScroll` | Done |
| `src/components/chat/MessageBubble.tsx` | Borderless assistant messages (delegates to `MessageContent`), user bubbles unchanged | Done |
| `src/components/chat/MessageInput.tsx` | Floating style, delegates to `ChatInputBox` | Done |

---

### Phase S: Shared Components + Service Unification -- COMPLETE

Cross-cutting infrastructure shared between general and inline chat.

| File | Change | Status |
|------|--------|--------|
| `src/components/common/ChatInputBox.tsx` | ChatGPT-style input with attach/dictate/send buttons, `compact` mode, feature-flag gated | Done |
| `src/components/common/MessageContent.tsx` | Shared markdown renderer (ReactMarkdown + CodeBlock), `compact` mode | Done |
| `src/components/common/ChatEmptyState.tsx` | Shared welcome/empty state, `compact` mode | Done |
| `src/components/chat/ThinkingIndicator.tsx` | Animated RingsLoader + italic text, Mantine Transition | Done |
| `src/components/common/RingsLoader.tsx` + `.module.css` | SVG three-ring spinner (CSS keyframes), ported from mill-grinder-ui | Done |
| `src/hooks/useAutoScroll.ts` | Shared auto-scroll hook (viewport ref, scroll-to-bottom tracking) | Done |
| `src/utils/streamUtils.ts` | `sleep()` + `streamResponse()` extracted from duplicated mock code | Done |
| `src/types/chat.ts` | `CreateChatParams` (optional context), `ChatSummary`, unified `ChatService` (6 methods: `createChat`, `sendMessage`, `listChats`, `getChatByContext`, `loadConversation`, `subscribe`) | Done |
| `src/types/inlineChat.ts` | Added `chatId: string \| null` to `InlineChatSession`, added `SET_SESSION_CHAT_ID` action, removed `InlineChatService` interface | Done |
| `src/services/chatService.ts` | Unified mock with context-aware response pools, `chatContextMap` / `contextToChatMap` / `generalChatList` tracking | Done |
| `src/context/InlineChatContext.tsx` | Rewired to use `chatService` (createChat with context params on first message, sendMessage by chatId) | Done |
| `src/context/ChatContext.tsx` | Optimistic UI: temp conversation on first message, `REPLACE_CONVERSATION_ID` swaps temp for real chatId. `RENAME_CONVERSATION` + `SET_THINKING` actions. | Done |
| `src/features/defaults.ts` | Added `chatAttachButton`, `chatDictateButton` flags (default `false`) | Done |
| `src/components/layout/AppShell.tsx` | Removed auto-create conversation on empty list | Done |
| `src/components/inline-chat/InlineChatInput.tsx` | Rewired to use `ChatInputBox` with `compact` mode | Done |
| `src/components/inline-chat/InlineChatMessage.tsx` | Rewired to use `MessageContent` with `compact` mode | Done |
| `src/components/inline-chat/InlineChatPanel.tsx` | Rewired to use `useAutoScroll` + `ChatEmptyState` | Done |
| Deleted `src/services/inlineChatService.ts` | Dead code -- all inline chat now uses unified `chatService` | Done |
| Deleted `src/services/mockApi.ts` | Dead code -- superseded by individual service files | Done |
| `src/services/api.ts` | Removed `inlineChatService` export | Done |
| Bug fix: `&[data-active]` CSS selector | Fixed in `Sidebar.tsx`, `QuerySidebar.tsx`, `SchemaTree.tsx` -- conditional style spread | Done |
| All test files | Updated mocks: `chatService` replaces `inlineChatService`, `FeatureFlagProvider` wrappers, async `createConversation` handling | Done |

---

### Phase 0: Foundation

Core model rewrite. Requires Phases L + S. See [Message Model](GENERAL-CHAT-DESIGN.md#message-model), [ChatContext](GENERAL-CHAT-DESIGN.md#chatcontext--reducer-design), and [ChatService](GENERAL-CHAT-DESIGN.md#chatservice-interface) in the design doc.

| File | Change |
|------|--------|
| `src/types/chat.ts` | New Message model: `UserMessage`, `AssistantMessage` (with `parts[]` + `status`), `MessagePart` union, `ChatEvent`, `ErrorPart`. UUIDs for all IDs. |
| `src/context/ChatContext.tsx` | `UPSERT_PART`, `SET_MESSAGE_STATUS`, `REPLAY_EVENTS`, `ADD_USER_MESSAGE`. New `sendMessage` flow. localStorage migration. |
| `src/services/chatService.ts` | Implement remaining mock methods: `loadConversation`, `subscribe`. Update `sendMessage` to yield `ChatEvent`s. |
| `src/components/chat/MessageComposer.tsx` | **NEW** -- compositional assistant renderer. Part grouping, renderer dispatch, streaming indicator, borderless. |
| `src/components/chat/MessageList.tsx` | Dispatch `UserMessage` -> `MessageBubble`, `AssistantMessage` -> `MessageComposer`. Per-message `status` replaces global `isLoading`. |
| `src/components/chat/MessageBubble.tsx` | Strip assistant branch; user messages only. |
| `src/features/defaults.ts` | 8 new chat message type flags (all default `true`). |
| `src/components/__tests__/*` | Update for new `Message` type. New reducer tests. |

---

### Phase 1a: Text Renderer

| File | Change |
|------|--------|
| `src/components/chat/renderers/TextRenderer.tsx` | **NEW** -- plain text, left-aligned, `pre-wrap`, max-width ~900px, theme-aware. |
| `src/services/chatService.ts` | Mock yields text `ChatEvent`s with word-by-word streaming. |
| `src/components/chat/MessageComposer.tsx` | Wire `TextRenderer` for `type === 'text'` parts. |

---

### Phase 1b: Markdown Renderer

| File | Change |
|------|--------|
| `src/components/chat/renderers/MarkdownRenderer.tsx` | **NEW** -- `ReactMarkdown` + `CodeBlock` extracted from `MessageBubble`. Max-width ~900px, theme-aware. |
| `src/services/chatService.ts` | Mock yields markdown `ChatEvent`s with streaming. |
| `src/components/chat/MessageComposer.tsx` | Wire `MarkdownRenderer` for `type === 'markdown'` parts. |

---

### Milestone 1 Validation Gate

1. All mock scenarios work: text streaming, markdown streaming, conversation switching, history replay
2. Feature flags enable/disable text and markdown independently
3. localStorage migration handles old conversations
4. Dark/light mode correct
5. Swap mock `ChatService` for real backend -- test against live SSE
6. Confirm: event shape, streaming, error handling, conversation loading
7. Decision: proceed to Milestone 2

---

## Milestone 2 (DEFERRED)

See [Data Architecture](GENERAL-CHAT-DESIGN.md#data-architecture) and [Part Type Definitions](GENERAL-CHAT-DESIGN.md#part-type-definitions) in the design doc.

### Phase 2a: Extract shared data components

- Extract `DataGrid.tsx` from `QueryResults.tsx` -- consumes `DataSource`, paged loading, skeleton rows
- Extract `DataToolbar.tsx` from `QueryResults.tsx` -- status bar, row count/"unknown", export
- Create `SqlPanel.tsx` -- read-only SQL display
- Create `DataSource` interface + `DataSourceFactory` + implementations (full, remote, partial)
- Mock `DataSource` for all three strategies
- Refactor `QueryResults.tsx` and `QueryEditor.tsx` to use shared components
- Verify: Analysis view unchanged

### Phase 2b: Chat data renderer (condensed)

- `DataRenderer.tsx` -- condensed tabbed card (Data tab with limited rows, SQL tab, "Expand" button)
- Adapts to `sourceType` with appropriate loading states
- Wire into `MessageComposer`

### Phase 2c: Chat data expanded view

- Full content area with `DataGrid` (full paging) + `DataToolbar` (export) + `SqlPanel`
- Remote/partial: paged loading with indicators
- Back/close button to return to chat

### Phase 3: Chart Addition

- `ChartView.tsx` -- echarts wrapper, consumes `DataSource` (pre-fetches for remote/partial with progress indicator)
- Update `DataRenderer` + expanded view with Chart tab

### Phase 4-7: Knowledge Cards

Each is a standalone renderer + mock data:

- **Phase 4**: `DescriptiveRenderer.tsx` + mock from `mockSchema.ts`
- **Phase 5**: `RelationalRenderer.tsx` + mock from `mockSchema.ts` relations; reuses `SqlPanel`
- **Phase 6**: `ConceptRenderer.tsx` + mock from `mockConcepts.ts`; reuses `SqlPanel`
- **Phase 7**: `ConstraintRenderer.tsx` + mock constraints

---

## Delivery Sequence

```
Phase L (Layout) ........... COMPLETE
  |
  v
Phase S (Shared + Unify) ... COMPLETE
  |
  v
Phase 0 (Foundation) ....... NEXT
  |
  +---> Phase 1a (Text) ---> Phase 1b (Markdown)
  |                                |
  |                                v
  |                      [Validate against backend]
  |                                |
  |     +--------------------------+
  |     |          |         |         |         |
  |     v          v         v         v         v
  |  Phase 2    Phase 4   Phase 5   Phase 6   Phase 7
  |  (2a->2b->2c)->Phase 3
```

---

## Tracking Checklist

### Phase L: Layout Overhaul -- COMPLETE

- [x] **L-1** ChatArea: replace current layout with three-layer structure (position: relative container)
- [x] **L-2** ChatArea: pinned top toolbar -- absolute positioning, transparent gradient fade
- [x] **L-3** ChatArea: toolbar contains conversation title (left) + empty Group slot (right)
- [x] **L-4** ChatArea: remove `maxWidth: 900px` constraint from message area container
- [x] **L-5** MessageInput: change to `position: absolute; bottom: 0` (floating overlay)
- [x] **L-6** MessageInput: remove `borderTop` and opaque background
- [x] **L-7** MessageInput: add bottom-to-top transparent gradient fade
- [x] **L-8** MessageInput: keep centered `max-width` on inner input bar
- [x] **L-9** MessageList: add top padding to clear toolbar overlay
- [x] **L-10** MessageList: add bottom padding to clear floating input overlay
- [x] **L-11** MessageList: scroll-to-bottom affix -- track scroll position via `useAutoScroll`
- [x] **L-12** MessageList: affix shows when scrolled up >200px from bottom, hides when near bottom
- [x] **L-13** MessageList: affix click smooth-scrolls to end of conversation
- [x] **L-14** MessageList: affix styling -- theme-aware background, subtle shadow, down-arrow icon
- [x] **L-15** MessageBubble: user messages -- keep existing bubble styling
- [x] **L-16** MessageBubble: assistant messages -- remove Paper wrapper, delegate to `MessageContent`
- [x] **L-17** Verify: textarea auto-grow does not shrink message pane
- [x] **L-18** Verify: content scrolls freely behind both overlays
- [x] **L-19** Verify: dark mode and light mode both work correctly
- [x] **L-20** Verify: existing chat functionality unchanged

### Phase S: Shared Components + Service Unification -- COMPLETE

**Shared UI components**

- [x] **S-1** Create `ChatInputBox` -- ChatGPT-style input with attach/dictate/send, `compact` mode
- [x] **S-2** Create `MessageContent` -- shared markdown renderer, `compact` mode
- [x] **S-3** Create `ChatEmptyState` -- shared welcome/empty state, `compact` mode
- [x] **S-4** Create `ThinkingIndicator` -- animated RingsLoader + text, Mantine Transition
- [x] **S-5** Create `RingsLoader` + CSS module -- SVG ring animation ported from mill-grinder-ui
- [x] **S-6** Create `useAutoScroll` hook -- viewport ref, auto-scroll, scroll-to-bottom tracking
- [x] **S-7** Create `streamUtils` -- extract `sleep()` + `streamResponse()` from duplicated code

**Service unification (general + inline use same backend)**

- [x] **S-8** Add `CreateChatParams` (optional context fields) to `types/chat.ts`
- [x] **S-9** Add `ChatSummary` type for chat listing
- [x] **S-10** Extend `ChatService`: `createChat(params?)`, `listChats()`, `getChatByContext()`
- [x] **S-11** Add `chatId: string | null` to `InlineChatSession`
- [x] **S-12** Add `SET_SESSION_CHAT_ID` action to `InlineChatAction`
- [x] **S-13** Remove `InlineChatService` interface from `types/inlineChat.ts`
- [x] **S-14** Unify mock: context-aware response pools, `chatContextMap` / `contextToChatMap`
- [x] **S-15** Wire `InlineChatContext` to use `chatService` (createChat on first message, sendMessage by chatId)
- [x] **S-16** Delete `inlineChatService.ts` and `mockApi.ts` (dead code)
- [x] **S-17** Remove `inlineChatService` from `api.ts` barrel

**Optimistic UI + behavior**

- [x] **S-18** Optimistic conversation creation on first message (temp ID + `REPLACE_CONVERSATION_ID`)
- [x] **S-19** `RENAME_CONVERSATION` action for backend-driven chat rename via SSE
- [x] **S-20** `SET_THINKING` action + `thinkingMessage` in `ChatState`
- [x] **S-21** Remove auto-create conversation on empty list (`AppShell.tsx`)

**Feature flags**

- [x] **S-22** Add `chatAttachButton` flag (default `false`)
- [x] **S-23** Add `chatDictateButton` flag (default `false`)

**Inline chat rewiring**

- [x] **S-24** `InlineChatInput` → uses `ChatInputBox` with `compact`
- [x] **S-25** `InlineChatMessage` → uses `MessageContent` with `compact`
- [x] **S-26** `InlineChatPanel` → uses `useAutoScroll` + `ChatEmptyState`

**Bug fixes**

- [x] **S-27** Fix `&[data-active]` CSS selector in NavLink styles (Sidebar, QuerySidebar, SchemaTree)
- [x] **S-28** Fix `style.minHeight` error in ChatInputBox (Mantine TextareaAutosize)

**Tests**

- [x] **S-29** Update all test mocks: `chatService` replaces `inlineChatService`
- [x] **S-30** Update `ChatAppShell.test.tsx` for no-auto-create behavior
- [x] **S-31** All 260 tests passing (2 pre-existing failures in `NotFoundPage` / `AccessDeniedPage` fixed in test coverage round)

**Test coverage round** (added after Phase S)

- [x] **T-1** Fix 15 broken tests: `ChatArea` (7), `ChatAppShell` (5), `NotFoundPage` (1), `AccessDeniedPage` (1) — added `MemoryRouter` wrappers and `searchService` mock, fixed button text drift
- [x] **T-2** Add `searchService` tests (30 tests) — query filtering, case-insensitive matching, max results, route building, synonym/tag matching, short query guard
- [x] **T-3** Add 8 service contract tests: `schemaService`, `conceptService`, `queryService`, `chatService`, `statsService`, `featureService`, `chatReferencesService`, `relatedContentService` (56 tests total)
- [x] **T-4** Add `GlobalSearch` component tests (11 tests) — expand/collapse, results rendering, type badges, no results + "Ask in Chat", keyboard nav
- [x] **T-5** Add `ChatArea` auto-send tests (3 tests) — router state `searchQuery` triggers `sendMessage({ newConversation: true })`, AI response after auto-send
- [x] **T-6** Add `sendMessage` `newConversation` option test — forces new conversation even when one is active
- [x] **T-7** Add `FeatureFlagContext` tests (4 tests) — default flags, merge with remote, error fallback, unknown keys
- [x] **T-8** Add `ChatReferencesContext` tests (6 tests) — fetch/cache, dedup, prefetch, error handling
- [x] **T-9** Add `RelatedContentContext` tests (6 tests) — same pattern as ChatReferences
- [x] **T-10** Install `@vitest/coverage-v8`, add `test:coverage` script, configure thresholds
- [x] **T-11** Final state: **377 tests across 37 files, all passing**. Coverage: 52.6% statements, 39.3% branches, 45.7% functions, 55.4% lines

### Phase 0: Foundation

**Types (`src/types/chat.ts`)**

- [ ] **0-1** Define `UserMessage` interface
- [ ] **0-2** Define `AssistantMessage` interface (with `parts[]` + `status`)
- [ ] **0-3** Define `Message` union type
- [ ] **0-4** Define `BaseMessagePart` interface
- [ ] **0-5** Define `TextPart` and `MarkdownPart`
- [ ] **0-6** Define `SqlPart`, `DataPart`, `ChartPart` (stubs for Milestone 2)
- [ ] **0-7** Define `DescriptivePart`, `RelationalPart`, `ConceptPart`, `ConstraintPart` (stubs)
- [ ] **0-8** Define `ErrorPart`
- [ ] **0-9** Define `MessagePart` discriminated union
- [ ] **0-10** Define `ChatEvent` interface
- [ ] **0-11** Replace `generateId()` with `crypto.randomUUID()`
- [ ] **0-12** Update `Conversation` interface to use new `Message` union

**ChatContext reducer (`src/context/ChatContext.tsx`)**

- [ ] **0-13** Add `UPSERT_PART`: create `AssistantMessage` if new `messageId`
- [ ] **0-14** `UPSERT_PART`: replace part if same `type` exists, otherwise append
- [ ] **0-15** Add `SET_MESSAGE_STATUS` action
- [ ] **0-16** Add `ADD_USER_MESSAGE` action
- [ ] **0-17** Add `REPLAY_EVENTS` action
- [ ] **0-18** Rewrite `sendMessage`: fire-and-forget + consume SSE events
- [ ] **0-19** localStorage migration for old conversations
- [ ] **0-20** Keep existing actions unchanged

**ChatService (`src/services/chatService.ts`)**

- [ ] **0-21** Implement mock `loadConversation` (returns `ChatEvent[]`)
- [ ] **0-22** Implement mock `subscribe` (yields `ChatEvent`s via SSE simulation)
- [ ] **0-23** Update mock `sendMessage` to yield `ChatEvent`s (not raw strings)
- [ ] **0-24** Verify `createChat`, `listChats`, `getChatByContext` still work (already implemented)

**MessageComposer (`src/components/chat/MessageComposer.tsx`)**

- [ ] **0-25** Create component: receives `AssistantMessage`, reads `parts[]`
- [ ] **0-26** Group parts by rendering strategy
- [ ] **0-27** Stub unsupported parts with "unsupported content" placeholder
- [ ] **0-28** Streaming indicator when `status === 'streaming'`
- [ ] **0-29** Borderless rendering

**MessageList + MessageBubble**

- [ ] **0-30** Dispatch `UserMessage` to `MessageBubble`
- [ ] **0-31** Dispatch `AssistantMessage` to `MessageComposer`
- [ ] **0-32** Replace global `isLoading` with per-message `status`
- [ ] **0-33** Remove assistant rendering from `MessageBubble`
- [ ] **0-34** Keep only user bubble rendering

**Feature flags + Tests**

- [ ] **0-35** Add 8 chat message type flags to `defaults.ts`
- [ ] **0-36** Update test files for new `Message` type
- [ ] **0-37** Reducer test: `UPSERT_PART` creates new message
- [ ] **0-38** Reducer test: `UPSERT_PART` appends new part type
- [ ] **0-39** Reducer test: `UPSERT_PART` replaces same part type
- [ ] **0-40** Reducer test: `SET_MESSAGE_STATUS` transitions
- [ ] **0-41** Reducer test: `REPLAY_EVENTS` bulk-processes events
- [ ] **0-42** Verify: full send-receive cycle with mock

### Phase 1a: Text Renderer

- [ ] **1a-1** Create `TextRenderer.tsx`
- [ ] **1a-2** Left-aligned, `pre-wrap`, max-width ~900px, theme-aware
- [ ] **1a-3** Wire into `MessageComposer` for `type === 'text'`
- [ ] **1a-4** Mock: word-by-word streaming text events
- [ ] **1a-5** Verify: text streams progressively
- [ ] **1a-6** Verify: `chatTextMessage` flag works

### Phase 1b: Markdown Renderer

- [ ] **1b-1** Create `MarkdownRenderer.tsx`
- [ ] **1b-2** Extract `ReactMarkdown` + `CodeBlock` from `MessageBubble`
- [ ] **1b-3** Left-aligned, max-width ~900px, all markdown components, theme-aware
- [ ] **1b-4** Wire into `MessageComposer` for `type === 'markdown'`
- [ ] **1b-5** Mock: streaming markdown events
- [ ] **1b-6** Verify: partial markdown renders gracefully
- [ ] **1b-7** Verify: code blocks with syntax highlighting
- [ ] **1b-8** Verify: `chatMarkdownMessage` flag works

### Milestone 1 Validation Gate

- [ ] **V-1** All mock scenarios work
- [ ] **V-2** Conversation switching works
- [ ] **V-3** History replay works
- [ ] **V-4** Feature flags work independently
- [ ] **V-5** localStorage migration works
- [ ] **V-6** Dark/light mode correct
- [ ] **V-7** Swap mock for real backend
- [ ] **V-8** Event shape matches backend
- [ ] **V-9** Streaming works end-to-end
- [ ] **V-10** Error handling works
- [ ] **V-11** Decision: proceed to Milestone 2

### Phase 2a: Shared Components + DataSource (DEFERRED)

- [ ] **2a-1** Define `DataSource` interface
- [ ] **2a-2** Define `DataPage` interface
- [ ] **2a-3** Implement `FullDataSource`
- [ ] **2a-4** Implement `RemoteDataSource` (with sliding-window cache)
- [ ] **2a-5** Implement `PartialRemoteDataSource`
- [ ] **2a-6** Create `DataSourceFactory`
- [ ] **2a-7** Mock `DataSource` implementations (all three strategies)
- [ ] **2a-8** Extract `DataGrid.tsx` from `QueryResults.tsx`
- [ ] **2a-9** Extract `DataToolbar.tsx` from `QueryResults.tsx`
- [ ] **2a-10** Create `SqlPanel.tsx`
- [ ] **2a-11** Refactor `QueryResults.tsx` to use shared components
- [ ] **2a-12** Refactor `QueryEditor.tsx`
- [ ] **2a-13** Verify: Analysis view unchanged

### Phase 2b: Chat Data Condensed (DEFERRED)

- [ ] **2b-1** Create `DataRenderer.tsx` (condensed tabbed card)
- [ ] **2b-2** Data tab with limited rows, adapts to `sourceType`
- [ ] **2b-3** SQL tab read-only
- [ ] **2b-4** "Expand" button
- [ ] **2b-5** Wire into `MessageComposer`
- [ ] **2b-6** Mock: sql + data events
- [ ] **2b-7** Verify: `chatDataMessage` flag works

### Phase 2c: Chat Data Expanded (DEFERRED)

- [ ] **2c-1** Full content area view
- [ ] **2c-2** `DataGrid` with full paged loading
- [ ] **2c-3** Remote/partial loading indicators
- [ ] **2c-4** `DataToolbar` with export
- [ ] **2c-5** `SqlPanel` full display
- [ ] **2c-6** Back/close button
- [ ] **2c-7** Full-width layout, sidebar slots

### Phase 3: Chart (DEFERRED)

- [ ] **3-1** Create `ChartView.tsx`
- [ ] **3-2** `ChartView` consumes `DataSource`, pre-fetches for remote/partial
- [ ] **3-3** Chart tab in condensed + expanded views
- [ ] **3-4** Mock: chart events
- [ ] **3-5** Verify: `chatChartMessage` flag works

### Phase 4: Descriptive (DEFERRED)

- [ ] **4-1** Create `DescriptiveRenderer.tsx`
- [ ] **4-2** Mock from `mockSchema.ts`
- [ ] **4-3** Verify: flag works

### Phase 5: Relational (DEFERRED)

- [ ] **5-1** Create `RelationalRenderer.tsx`
- [ ] **5-2** Mock from `mockSchema.ts` relations
- [ ] **5-3** Verify: flag works

### Phase 6: Concept (DEFERRED)

- [ ] **6-1** Create `ConceptRenderer.tsx`
- [ ] **6-2** Mock from `mockConcepts.ts`
- [ ] **6-3** Verify: flag works

### Phase 7: Constraint (DEFERRED)

- [ ] **7-1** Create `ConstraintRenderer.tsx`
- [ ] **7-2** Mock constraints
- [ ] **7-3** Verify: flag works

---

*Last updated: February 12, 2026*
