# General Chat -- Design

Architecture, types, and design decisions for the multi-type agent message system.

---

## Design Principles

- **SSE event-driven** -- the backend produces a stream of typed SSE events. Each event carries a `messageId` and an event type. Events with the same `messageId` compose into a single message box that progressively enhances as more events arrive.
- **messageId = box** -- a `messageId` (UUID) maps to one box in the chat. An event either **creates** a new box (first event with that `messageId`) or **updates** an existing box (subsequent events with the same `messageId`).
- **Compositional messages** -- a message is NOT defined by a single type discriminator. It accumulates **parts** from SSE events. A data message starts with just SQL (one event), then gets a data grid (second event), then maybe a chart (third event). The UI renders whatever parts are available.
- **Incremental delivery** -- each event type (and its corresponding renderer) is a separate deliverable. Ship text first, then markdown, then SQL, then data grid, then chart, etc.
- **Reference, not copy** -- mill-grinder-ui is a reference for understanding real data shapes. mill-ui gets its own clean types, design system, and patterns. No code is copied from mill-grinder-ui.
- **Feature-flag gated** -- each part type has its own flag. Disabled parts fall back to text or are hidden.
- **Mock-first** -- every phase ships a complete UX with mocked data before any backend wiring. The `ChatService` interface is the seam -- swap mock for real when the backend is ready.

---

## Transport Architecture (CQRS-style)

Commands go via REST API. All state updates arrive as SSE events.

- **REST (command side)**: `POST /chat/send` to submit a user message, `POST /chat/action` to trigger actions, `POST /chat/conversations` to create conversations, etc. REST calls return acknowledgement only (e.g. `{ ok: true }`) -- they do NOT return message content.
- **SSE (event side)**: After a REST command, the backend pushes one or more `ChatEvent`s over an SSE connection. The frontend's `UPSERT_PART` reducer processes these identically whether they arrive live or are replayed.
- **Loading existing conversations**: Returns a list of `ChatEvent[]` -- the same shape as SSE events. The frontend replays them through the same `UPSERT_PART` path to rebuild message state. (Not yet confirmed whether this is REST `GET` or a dedicated SSE burst; design accommodates either.)
- **Single event shape everywhere**: `ChatEvent { messageId, part }` is the universal atom. Live SSE, history replay, and mock all produce the same type. The reducer doesn't know or care about the source.
- **Message IDs are UUIDs** (`crypto.randomUUID()`).

### Example flow

User asks: "Show me top customers by revenue"

```
Event Stream:
  messageId=<uuid-1>, type=sql,         payload={ sql: "SELECT ..." }
  messageId=<uuid-1>, type=data,        payload={ fields: [...], sourceType: "full", rows: [...] }
  messageId=<uuid-2>, type=text,        payload={ content: "Here are your top customers..." }
  messageId=<uuid-1>, type=chart,       payload={ chartType: "bar" }
  messageId=<uuid-3>, type=descriptive, payload={ entities: [...] }
```

Frontend result:

- **Box 1**: A data card that first shows SQL tab, then adds Data tab, then adds Chart tab -- all updating the same box
- **Box 2**: A plain text message
- **Box 3**: A descriptive knowledge card

### Key behaviors

- Events for the same `messageId` **update** an existing box (add/replace parts)
- Events with a new `messageId` **create** a new box in the chat list
- Order within a `messageId` is meaningful (SQL before DATA) but order across `messageId`s is interleaved
- Text and markdown events can stream progressively (word-by-word chunks with same `messageId`)

### ChatService interface

Both general and inline chat use the **same backend service** (`ChatService`). The only differences are:

1. **`createChat` payload** -- general chat passes no context; inline chat passes `contextType`, `contextId`, `contextLabel`, `contextEntityType`.
2. **Chat listing** -- `listChats()` returns only general (non-contextual) chats for the sidebar. Inline chats are scoped to their context and are **not** visible in the general chat list.
3. **Inline chat retrieval** -- `getChatByContext(contextType, contextId)` looks up an existing inline chat by its context (retrieval mechanism TBD).

```typescript
interface ChatService {
  /** Create a new chat. Context params → inline chat; no params → general chat. */
  createChat(params?: CreateChatParams): Promise<CreateChatResult>;

  /** Submit user message -- REST POST, fire-and-forget */
  sendMessage(chatId: string, text: string): void;

  /** Load historical events for an existing conversation */
  loadConversation(chatId: string): Promise<ChatEvent[]>;

  /** Subscribe to live SSE event stream */
  subscribe(chatId: string): AsyncIterable<ChatEvent>;

  /** List general (non-contextual) chats for sidebar. Inline chats excluded. */
  listChats(): Promise<ChatSummary[]>;

  /** Retrieve chatId for an inline chat by context. Returns null if none exists. */
  getChatByContext(contextType: string, contextId: string): Promise<string | null>;
}
```

The mock implementation simulates all methods: `createChat` returns a UUID and tracks context mappings, `sendMessage` triggers a delayed event sequence on the mock stream, `loadConversation` returns pre-built event arrays, `subscribe` yields events with timed delays, `listChats` returns only non-contextual chats, and `getChatByContext` looks up by `"contextType:contextId"` key.

---

## Message Model

### Core types

```typescript
/** User messages remain simple */
interface UserMessage {
  id: string;            // UUID
  conversationId: string;
  role: 'user';
  content: string;
  timestamp: number;
}

/** Assistant messages accumulate parts from SSE events */
interface AssistantMessage {
  id: string;            // UUID -- the messageId from SSE
  conversationId: string;
  role: 'assistant';
  parts: MessagePart[];  // accumulated from SSE events, ordered by arrival
  status: 'streaming' | 'complete' | 'error';
  timestamp: number;
}

type Message = UserMessage | AssistantMessage;

/** The universal event atom */
interface ChatEvent {
  messageId: string;     // UUID
  part: MessagePart;
}

/** Base shape -- all parts share this */
interface BaseMessagePart {
  type: string;
}

/** Discriminated union -- each SSE event type maps to a part type */
type MessagePart =
  | TextPart
  | MarkdownPart
  | SqlPart
  | DataPart
  | ChartPart
  | DescriptivePart
  | RelationalPart
  | ConceptPart
  | ConstraintPart
  | ErrorPart;
```

### Message types overview

| # | Type | Description | Streaming |
|---|------|-------------|-----------|
| 1 | **Text** | Plain text response. No formatting. | Yes (word-by-word) |
| 2 | **Markdown** | Rich text with headers, code blocks, tables, lists, bold/italic. | Yes (progressive) |
| 3 | **SQL** | SQL query with optional explanation. Arrives first in data messages. | No |
| 4 | **Data** | Tabular results. Arrives after SQL on same messageId. | No |
| 5 | **Chart** | Chart visualization. Arrives after SQL + Data on same messageId. | No |
| 6 | **Descriptive** | Metadata about model objects (schema, table, column). | No |
| 7 | **Relational** | Relationships between tables/columns -- foreign keys, join paths. | No |
| 8 | **Concept** | Business concept definitions. | No |
| 9 | **Constraint** | Data quality rules/expectations. | No |
| 10 | **Error** | Failed query, connection drop, or other error. | No |

---

## Part Type Definitions

### Text

```typescript
interface TextPart extends BaseMessagePart {
  type: 'text';
  content: string;  // accumulated text content
}
```

- **UPSERT behavior**: replace `content` on same `messageId` (streaming -- service accumulates, sends full content each time)

### Markdown

```typescript
interface MarkdownPart extends BaseMessagePart {
  type: 'markdown';
  content: string;  // accumulated markdown content
}
```

- **UPSERT behavior**: same as text -- replace/append content on same `messageId`

### SQL

```typescript
interface SqlPart extends BaseMessagePart {
  type: 'sql';
  sql: string;
  explanation?: string;
}
```

- Arrives first in the typical data message flow. The data box immediately shows with just a SQL tab.

### Data

```typescript
interface DataPart extends BaseMessagePart {
  type: 'data';
  fields: FieldInfo[];          // always present -- column metadata
  sourceType: 'full' | 'remote' | 'partial';

  // Full data: rows are inline
  rows?: any[][];

  // Remote dataset: fetch via service
  datasetId?: string;

  // Partial remote: initial rows + scroll cursor
  scrollId?: string;

  // Optional metadata
  totalRows?: number;           // known for full, unknown/estimated for remote
}
```

- Arrives after SQL on same `messageId`. See [Data Fetching Strategies](#data-fetching-strategies) for details on source types.

### Chart

```typescript
interface ChartPart extends BaseMessagePart {
  type: 'chart';
  chartType: 'bar' | 'pie' | 'sunburst' | 'treemap';
}
```

- Arrives after SQL + Data on same `messageId`. Uses data from the `DataPart` already on the message.

### Descriptive

```typescript
interface DescriptivePart extends BaseMessagePart {
  type: 'descriptive';
  entities: DescriptiveEntity[];
}
interface DescriptiveEntity {
  entityType: 'schema' | 'table' | 'column';
  entityId: string;       // e.g., "sales.customers.email"
  name: string;
  description: string;
  tags?: string[];
  physicalType?: string;
  nullable?: boolean;
  parentPath?: string;    // e.g., "sales.customers" for a column
}
```

### Relational

```typescript
interface RelationalPart extends BaseMessagePart {
  type: 'relational';
  relations: RelationInfo[];
}
interface RelationInfo {
  source: string;
  target: string;
  cardinality: string;    // "1:1", "1:N", "N:M"
  joinColumns: Array<{ sourceColumn: string; targetColumn: string }>;
  sql?: string;
}
```

### Concept

```typescript
interface ConceptPart extends BaseMessagePart {
  type: 'concept';
  concepts: ConceptInfo[];
}
interface ConceptInfo {
  conceptId: string;
  name: string;
  category: string;
  description: string;
  tags?: string[];
  sqlDefinition?: string;
  relatedEntities?: string[];
}
```

### Constraint

```typescript
interface ConstraintPart extends BaseMessagePart {
  type: 'constraint';
  constraints: ConstraintRule[];
}
interface ConstraintRule {
  entityId: string;
  column: string;
  ruleType: 'not-null' | 'range' | 'unique' | 'format' | 'custom';
  expression: string;
  description?: string;
}
```

### Error

```typescript
interface ErrorPart extends BaseMessagePart {
  type: 'error';
  code?: string;
  message: string;
}
```

---

## ChatContext / Reducer Design

### UPSERT_PART (core action)

All SSE events and history replay go through this single action:

```typescript
type ChatAction =
  | { type: 'UPSERT_PART'; messageId: string; conversationId: string; part: MessagePart }
  | { type: 'SET_MESSAGE_STATUS'; messageId: string; conversationId: string; status: 'streaming' | 'complete' | 'error' }
  | { type: 'REPLAY_EVENTS'; conversationId: string; events: ChatEvent[] }
  | { type: 'ADD_USER_MESSAGE'; payload: { conversationId: string; message: UserMessage } }
  // ... existing actions unchanged (CREATE_CONVERSATION, DELETE_CONVERSATION, etc.)
```

**UPSERT_PART semantics**:

- If no message with this `messageId` exists -> create a new `AssistantMessage` with `parts: [part]`
- If the message exists and a part with the same `type` already exists -> **replace** that part (used for streaming text/markdown where content accumulates on the service side)
- If the message exists and no part with this `type` exists -> **append** the part

**REPLAY_EVENTS**: accepts `ChatEvent[]`, runs them through the same upsert logic in order. Used for loading existing conversations.

**sendMessage flow**: call `chatService.sendMessage()` (REST, fire-and-forget), add user message locally, then consume events from the SSE subscription dispatching `UPSERT_PART` per event.

**localStorage migration**: detect old flat `content` messages on load, convert to `AssistantMessage` with a single `TextPart`.

---

## MessageComposer (composition rules)

Given an `AssistantMessage`, the composer reads `parts[]` and groups them by rendering strategy:

- **Text block**: `text` + `markdown` parts render as flowing content (borderless, left-aligned, max-width ~900px)
- **Data block**: `sql` + `data` + `chart` parts compose into a single tabbed card (full-width)
- **Knowledge cards**: `descriptive`, `relational`, `concept`, `constraint` parts each render as standalone full-width cards
- **Error**: `error` parts render as error indicators

The composer does NOT dispatch by a single type. It looks at the **combination of parts** and composes the right layout. Unknown part types show a dimmed "unsupported content" placeholder.

---

## Layout Architecture

### Chat area three-layer layout

```
+--[ ChatToolbar (pinned, z-10) ]-------+  <- fixed to top, transparent gradient fade
|  Title  |        [future controls]    |
+-------------------------------------------+
|                                           |
|  [ scrollable MessageList ]               |  <- full height, top+bottom padding
|  (content scrolls behind toolbar & input) |     to clear both overlays
|                                           |
+-------------------------------------------+
|  [ ThinkingIndicator ]                    |  <- above input, animated rings + text
|  [ ChatInputBox (floating, z-10) ]        |  <- fixed to bottom, transparent gradient fade
+-------------------------------------------+
```

**Top toolbar**: `position: absolute; top: 0`. Transparent gradient fade (no backdrop blur -- avoids visible "blurred box" artifacts). Contains conversation title (left) and an empty slot for future controls (right) -- model switcher, related objects, etc.

**Bottom input**: `position: absolute; bottom: 0`. Bottom-to-top transparent gradient fade (no backdrop blur). Contains `ThinkingIndicator` (when active) and `ChatInputBox`. Textarea auto-grow does not affect content pane height. Centered `max-width` on inner input bar.

**Message area**: `flex: 1; overflow: hidden`. Full column height. Top + bottom padding on scroll area to clear overlays. **Full-width content** -- no global `maxWidth` cap. Text renderers self-limit to ~900px; data grids and cards stretch to full width.

### Message styling

- **User messages**: bubble -- Paper wrapper, background color, shadow, rounded corners, right-aligned.
- **Assistant messages**: borderless -- no Paper wrapper, no background, no shadow. Text and markdown flow as clean left-aligned content. Structured parts (data cards, knowledge cards) provide their own card styling internally.

### Scroll-to-bottom affix

When the user scrolls up beyond ~200px from bottom, a floating pill/button (down-arrow icon) appears just above the floating input. Clicking it smooth-scrolls to the end. Auto-hides when at/near bottom. Theme-aware background, subtle shadow. Implemented via `useAutoScroll` hook (shared between general and inline chat).

### Thinking indicator

When the backend is processing (or mock is simulating), a `ThinkingIndicator` component appears above the input box. Uses a `RingsLoader` SVG animation (three concentric rings rotating with color cycling) and italic text label. Controlled by `state.thinkingMessage` in `ChatState` -- set to a string (e.g. "Thinking...") to show, `null` to hide. Slides in/out via Mantine `Transition`.

### ChatInputBox (shared input component)

ChatGPT-style rounded container used by both general and inline chat:

- **Layout**: textarea (borderless, transparent bg) on top; bottom row with `[+]` attach button (left), `[mic]` dictate button + `[↑]` send button (right).
- **Attach and Dictate buttons**: controlled by `chatAttachButton` / `chatDictateButton` feature flags (default `false`). Placeholder actions for now.
- **Send button**: filled circle, dark/light inverted. Disabled (opacity 0.35) when input is empty.
- **Compact mode**: `compact` prop for inline chat -- smaller sizing, fewer max rows (3 vs 6), tighter padding.
- **Auto-resize**: uses Mantine's `autosize` + `minRows`/`maxRows` (not manual height manipulation -- `TextareaAutosize` does not support `style.minHeight`).

### Chat creation behavior

- No auto-create on app load or when chat list becomes empty. Empty state shows welcome message + input box.
- Typing a message without an active conversation triggers **optimistic creation**: a temporary conversation appears immediately with the user's message, then `chatService.createChat()` is called asynchronously. On success, `REPLACE_CONVERSATION_ID` swaps the temp ID for the backend's real `chatId`.
- The "+" button in the sidebar explicitly calls `createConversation()` for a fresh empty chat.

### "Ask in Chat" flow (Global Search integration)

When the global search yields no results, an "Ask in Chat" button navigates to `/chat` with the search query in React Router state: `navigate('/chat', { state: { searchQuery: query } })`.

The `ChatArea` component detects this via a `useEffect` watching `location.state`:

1. **Guard**: waits for `ChatContext.initialized === true` (localStorage hydration must complete first — prevents a race condition where the new conversation is overwritten by `LOAD_CONVERSATIONS`)
2. **Send**: calls `sendMessage(searchQuery, { newConversation: true })`
3. **Cleanup**: clears router state via `navigate(location.pathname, { replace: true, state: {} })` to prevent re-send on refresh

The `newConversation: true` option forces `sendMessage` to create a new conversation even when one is already active. Internally, `sendMessage` sets `conversationId = null` when this option is set, triggering the optimistic creation path.

### ChatContext `initialized` flag

`ChatContext` exposes an `initialized: boolean` that is `false` until the `useEffect` that loads conversations from `localStorage` completes. This prevents race conditions where effects that create conversations (like "Ask in Chat") fire before the initial state is hydrated.

---

## Data Architecture

### Two-view pattern for data messages

- **Condensed view** (inline in chat): compact tabbed card with limited rows (~10), read-only SQL, no export. "Expand" button to open full view.
- **Expanded view** (full content area): full data grid with sorting/export, SQL panel, potentially re-execution. Left/right sidebars available. Back/close button to return to chat.

### Shared data components

The expanded data view overlaps with the existing Analysis/Query Playground. Shared primitives live in `src/components/data/`:

```
src/components/data/
  DataGrid.tsx      -- table, sorting, column types, paged loading via DataSource
  DataToolbar.tsx   -- status bar (row count, exec time, export menu)
  SqlPanel.tsx      -- read-only SQL display: syntax highlight + format
  SqlEditor.tsx     -- editable SQL: format, copy, clear, execute
  ChartView.tsx     -- echarts wrapper
```

Usage by context:

| Component | Chat Condensed | Chat Expanded | Analysis View |
|-----------|---------------|---------------|---------------|
| `DataGrid` | limited rows, no export | full rows, sorting, pagination | full rows, sorting |
| `DataToolbar` | hidden or minimal | row count, exec time, export | row count, exec time, export |
| `SqlPanel` | read-only, collapsed | read-only, full | -- |
| `SqlEditor` | -- | optional re-execute | full editing |
| `ChartView` | compact | full-size | future |

### Data fetching strategies

The backend determines how result data is delivered. The frontend handles all three strategies transparently.

| Strategy | Description | Row count | Data in `DataPart` |
|----------|-------------|-----------|-------------------|
| **Full data** | Entire result set on client (aggregations, chart data, small sets) | Known | `{ rows: [...], fields: [...] }` |
| **Remote dataset** | Client gets `datasetId` only. Micro-batch fetch on scroll. Backend forward-only. | Unknown | `{ datasetId: "..." }` |
| **Partial remote** | First page + `scrollId`. Hybrid -- initial render immediate, more on demand. | Unknown/estimated | `{ rows: [...], scrollId: "..." }` |

### DataSource abstraction

The `DataGrid` and `ChartView` consume a `DataSource` interface -- they never fetch data directly:

```typescript
interface DataSource {
  /** Column metadata -- always available immediately */
  fields: FieldInfo[];

  /** Whether total row count is known */
  totalRows: number | null;

  /** Fetch a page of rows.
   *  - Full: returns from memory (instant).
   *  - Remote/partial: returns from cache if available, otherwise fetches from backend.
   *  - Deep back-page (beyond cache window): re-fetches from start. */
  fetchPage(offset: number, limit: number): Promise<DataPage>;

  /** Whether more data is available beyond what has been fetched */
  hasMore: boolean;

  /** Source type for UX hints */
  sourceType: 'full' | 'remote' | 'partial';

  /** Range of rows currently available in client cache (remote/partial only). null for full data. */
  cachedRange: { start: number; end: number } | null;

  /** Whether a fetch is currently in progress */
  isFetching: boolean;
}

interface DataPage {
  rows: any[][];
  offset: number;
  hasMore: boolean;
  fromCache: boolean;
}
```

### Virtual paging with sliding-window cache

For remote and partial-remote datasets, the client maintains a sliding-window cache:

- **Cache window**: configurable number of pages (e.g. ~50 pages / ~5000 rows) kept in memory as a contiguous ring buffer.
- **Short back-paging**: within the cached window, rows served instantly from memory.
- **Deep back-paging**: beyond the cache window, re-fetch from offset 0 with "Reloading from start..." indicator. Old cache discarded and rebuilt.
- **Forward scrolling**: next micro-batch fetched from backend. Oldest pages evicted when cache exceeds window size.
- **Cache key**: `datasetId` (or `scrollId`) -- each dataset has its own cache.
- **Partial remote**: same caching, but first page is pre-loaded (no initial spinner).

```
Cache window example (window size = 5 pages):

  [evicted] [evicted] [ page 3 ] [ page 4 ] [ page 5 ] [ page 6 ] [ page 7 ]
                       ^--- cache start                              ^--- cache end

  Scroll back to page 4 -> instant (in cache)
  Scroll back to page 1 -> re-fetch from start (deep back-page)
  Scroll forward to page 8 -> fetch from backend, evict page 3
```

### UX per data source type

- **Full data**: grid renders all rows, known row count in toolbar, chart renders immediately, full export.
- **Remote dataset**: spinner while first batch loads, "Loading..." row count, forward-scroll triggers fetch, short back-scroll instant from cache, deep back-scroll shows "Reloading..." indicator. Chart pre-fetches all data with progress indicator. Export may require full fetch.
- **Partial remote**: first page renders immediately, "Loading more..." on forward scroll, cache provides short back-paging. Chart may need additional fetches.
- **All types**: loading spinners on page transitions, skeleton rows during fetch, error states, retry buttons.

---

## Shared Components

Components and hooks reused between general chat and inline chat:

| Component / Hook | Location | Purpose |
|------------------|----------|---------|
| `ChatInputBox` | `src/components/common/ChatInputBox.tsx` | ChatGPT-style input with attach/dictate/send. `compact` mode for inline chat. |
| `MessageContent` | `src/components/common/MessageContent.tsx` | Markdown renderer via ReactMarkdown + CodeBlock. `compact` mode for inline chat. |
| `ChatEmptyState` | `src/components/common/ChatEmptyState.tsx` | Welcome/empty state with sparkle icon. `compact` mode for inline chat. |
| `ThinkingIndicator` | `src/components/chat/ThinkingIndicator.tsx` | Animated rings + italic text for backend processing status. |
| `RingsLoader` | `src/components/common/RingsLoader.tsx` | SVG-based three-ring animated spinner (CSS keyframes). |
| `useAutoScroll` | `src/hooks/useAutoScroll.ts` | Viewport ref, auto-scroll on dependency change, scroll-to-bottom visibility tracking. |
| `streamUtils` | `src/utils/streamUtils.ts` | `sleep()` and `streamResponse()` utilities for mock streaming. |

---

## Feature Flags

**Message type flags** (all default `true`):

- `chatTextMessage`
- `chatMarkdownMessage`
- `chatDataMessage`
- `chatChartMessage`
- `chatDescriptiveMessage`
- `chatRelationalMessage`
- `chatConceptMessage`
- `chatConstraintMessage`

**Input control flags** (all default `false`):

- `chatAttachButton` -- show "+" attach button in ChatInputBox
- `chatDictateButton` -- show microphone dictate button in ChatInputBox

---

## Renderer UX Summary

| Part type | Renderer | Style | Width |
|-----------|----------|-------|-------|
| `text` | `TextRenderer` | Borderless, left-aligned, `pre-wrap` | max ~900px |
| `markdown` | `MarkdownRenderer` | Borderless, left-aligned, ReactMarkdown + CodeBlock | max ~900px |
| `sql` + `data` [+ `chart`] | `DataRenderer` | Tabbed card (condensed) or full-area (expanded) | Full width |
| `descriptive` | `DescriptiveRenderer` | Full-width card, entity cards with type badges, links to `/model/...` | Full width |
| `relational` | `RelationalRenderer` | Full-width card, relation pairs with cardinality badges, join columns | Full width |
| `concept` | `ConceptRenderer` | Full-width card, category pills, collapsible SQL, links to `/knowledge/...` | Full width |
| `constraint` | `ConstraintRenderer` | Full-width card, rule type badges, grouped by table | Full width |
| `error` | (inline indicator) | Error styling | Full width |

---

*Last updated: February 12, 2026*
