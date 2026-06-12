# WI-296 — Expand shell + SQL expand wiring

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `done` |
| **Type** | `✨ feature` |
| **Area** | `ui` |
| **Depends on** | [**WI-295**](WI-295-ai-sql-view-query-data-view.md), [**WI-291**](WI-291-ai-sql-view-preview-framework.md) |
| **Enables** | [**WI-298**](WI-298-chat-profile-switch.md) |

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

- [x] Expand from message A; back → message A visible.
- [x] Full expand flow on `general` chat with paging.
- [x] Condensed ↔ expand visually consistent (chat-native).
- [x] Re-run in expand without closing pane.
