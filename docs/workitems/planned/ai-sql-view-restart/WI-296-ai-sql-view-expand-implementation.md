# WI-296 — Expand shell + SQL expand wiring

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` |
| **Area** | `ui` |
| **Depends on** | [**WI-295**](WI-295-ai-sql-view-query-data-view.md), [**WI-291**](WI-291-ai-sql-view-preview-framework.md) |
| **Enables** | [**WI-297**](WI-297-ai-sql-view-closure.md) |

## Goal

Implement **ChatExpandHost** and wire **Expand** for `general` + `sql-data-composite`: full paging,
action parity, chat-native **SqlDataExpandedView**.

Merged former expand shell (297) + SQL expand wiring (298) into one delivery unit.

## Deliver

### `ui/mill-ui/src/components/chat/expand/`

- `ChatExpandHost.tsx`, `ExpandHeader.tsx`, `expandRegistry.ts`, `useChatExpand.tsx`
- **`SqlDataExpandedView`:** ExpandHeader, SqlReadOnlyPanel, `QueryDataView` expanded mode, shared action bar

### Integration

- [`ChatArea.tsx`](../../../../ui/mill-ui/src/components/chat/ChatArea.tsx): hide [`MessageList`](../../../../ui/mill-ui/src/components/chat/MessageList.tsx) when expand active; v1 **`general`** only
- Register `sql-data-composite` in `expandRegistry` (`general` only)
- Expand button on `SqlDataCondensedPreview` when treatment allows
- Action parity: Run, Copy, Export, Open in Analysis (SQL-only handoff)

### Tests

- Vitest: open/close expand; scroll-to-message; registry dispatch
- No Expand on `inline-analysis`; paging in expanded view; back navigation

## Acceptance criteria

- [ ] Expand from message A; back → message A visible.
- [ ] Full expand flow on `general` chat with paging.
- [ ] Condensed ↔ expand visually consistent (chat-native).
- [ ] Re-run in expand without closing pane.
