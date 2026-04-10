# WI-082 - mill-ui Unified AI Chat Integration

Status: `planned`
Type: `feature`
Area: `ui`
Backlog refs: `TBD`

## Problem Statement

`mill-ui` general chat currently uses `NlSqlChatControllerApi`, a generated client hitting
`/api/nl2sql/chats/*` endpoints with a bespoke three-event SSE protocol
(`chat_begin_progress_event`, `chat_end_progress_event`, `chat_message_event`).
The transition to the unified AI v3 service requires replacing both the REST client and
the SSE subscription with the new `/api/v1/ai/chats/*` API and the `item.*` delta-streaming
event model (WI-080). Contextual/inline chat does not currently exist in `mill-ui` and is
out of scope for this work item.

## Goal

Migrate `mill-ui` general chat from the legacy `NlSqlChatControllerApi` to the unified
AI v3 chat service, keeping all existing general-chat user interactions intact.

## In Scope

General chat lifecycle:
- List chats (sidebar)
- Create chat
- Stream reply (SSE delta accumulation)
- Delete chat (already hard delete — align field names only)
- Favorite / unfavour chat
- Rename chat (not yet implemented in UI — add against v3)

SSE transition:
- Replace `EventSource` on `/api/nl2sql/chats/{id}/stream` with the `item.*` event model
- Implement client-side delta accumulator for `item.part.updated` chunks
- Map `item.completed` (non-streaming path) and `item.failed` to UI feedback
- Replace `chat_begin_progress_event` / `chat_end_progress_event` progress UI with
  `item.created` (stream opened) / `item.completed` or `item.failed` (stream closed)

v1 assistant presentation:
- Render assistant replies as plain text only (accumulated delta or `item.completed.content`)
- Existing intent components (`GetDataIntent`, `EnrichModelIntent`, etc.) are out of scope
  for v1; they require a structured artifact SSE extension not defined in WI-080

## Out of Scope

- Contextual / inline chat (no existing UI; tracked separately as a greenfield feature)
- Query/sidebar deletion flows unrelated to AI chat resources
- Clarification flow (`need-clarification` / `reasoning-id` protocol has no v3 equivalent)
- Structured intent rendering beyond plain-text assistant replies (second iteration)

## Dependencies

- [Agentic Runtime v3 - Chat Service and API](/C:/Users/vm/wip/qpointz/qpointz/docs/design/agentic/v3-chat-service.md)
- [REST Exception Handling Pattern](/C:/Users/vm/wip/qpointz/qpointz/docs/design/platform/rest-exception-handling-pattern.md)

## Key Integration Points

### REST client swap

| Current (`NlSqlChatControllerApi`) | v3 (`/api/v1/ai/chats/*`) |
|------------------------------------|---------------------------|
| `createChat({name})` | `POST /api/v1/ai/chats` `{chatName}` |
| `listChats()` | `GET /api/v1/ai/chats` (returns general chats only) |
| `deleteChat(chatId)` | `DELETE /api/v1/ai/chats/{chatId}` |
| `updateChat(id, {chatName, isFavorite})` | `PATCH /api/v1/ai/chats/{chatId}` same fields |
| `listChatMessages(chatId)` | `GET /api/v1/ai/chats/{chatId}/messages` |
| `postChatMessages(id, {message})` + manual EventSource | `POST /api/v1/ai/chats/{chatId}/messages` SSE response |

### SSE protocol swap

Current: separate stream endpoint + three ad-hoc event types.
v3: `POST .../messages` returns the SSE stream directly; event types are `item.*`.

```
item.created        → stream opened; show progress indicator
item.part.updated   → append delta to accumulated buffer; update UI in real time
item.completed      → stream closed; content is null (use buffer) or full text (non-streaming path)
item.failed         → stream closed with error; show error state
```

The client needs a stateful delta accumulator scoped to the active message turn.

### Data model delta

`Chat.id` → `chatId`, `Chat.name` → `chatName`. `isFavorite` unchanged.
`ConversationTurn` carries `turnId`, `role`, `text`, `createdAt` — no `content` object.
v1 assistant rendering uses `text` directly (plain text / markdown).

## Implementation Plan

1. **Regenerate API client** — add v3 chat endpoints to OpenAPI spec; regenerate `src/api/mill/`.
2. **SSE adapter** — implement `useChatStream(chatId)` hook encapsulating delta accumulation
   and `item.*` event dispatch. Replace the manual `EventSource` in `ChatProvider`.
3. **ChatProvider rewrite** — swap API calls to v3 client; replace streaming section with
   the new hook; update field name references (`id` → `chatId`, `name` → `chatName`).
4. **Assistant message rendering** — update `AssistantMessage` to render accumulated text
   (markdown); remove intent dispatch for v1.
5. **Rename UI** — add rename inline edit to the chat sidebar (was never implemented).
6. **Remove legacy code** — delete `NlSqlChatControllerApi` usage, old event handlers,
   and clarification state that has no v3 counterpart.

## Acceptance Criteria

- General chat sidebar lists, creates, deletes, favorites, and renames chats via v3 API.
- Sending a message opens an SSE stream; assistant reply streams in real time via delta accumulation.
- `item.failed` events surface as a visible error state in the chat view.
- `item.completed` with non-null `content` (non-streaming fallback) renders correctly.
- Legacy `NlSqlChatControllerApi` chat usage is fully removed.
- Clarification UI is removed or hidden (no v3 equivalent).

## Test Plan

### Unit

- `useChatStream` hook: delta accumulation, `item.completed` null-content path, `item.failed` error state.
- `ChatProvider`: create/list/delete/favorite/rename actions against mocked v3 client.

### Integration

- End-to-end UI tests covering general chat lifecycle against the real backend.

## Risks and Mitigations

- **Risk:** SSE delta model requires client-side state the current `ChatProvider` does not have.
  **Mitigation:** isolate accumulation in a dedicated `useChatStream` hook; test independently.

- **Risk:** Plain-text v1 rendering regresses intent-rich responses from the legacy backend.
  **Mitigation:** legacy backend is replaced, not run alongside; regression is not possible once cut over.

- **Risk:** OpenAPI generator output changes break other consumers of `src/api/mill/`.
  **Mitigation:** regenerate and review diff; other API classes (`MetadataApi`, `SchemaExplorerApi`) are unaffected by chat additions.

## Deliverables

- Updated work item (`docs/workitems/WI-082-mill-ui-unified-ai-chat-integration.md`)
- Regenerated v3 API client in `src/api/mill/`
- `useChatStream` SSE adapter hook
- Updated `ChatProvider` and `AssistantMessage` components
- Sidebar rename UI
