# WI-297 — Expand shell and navigation

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` |
| **Area** | `ui` |
| **Depends on** | [**WI-295**](WI-295-ai-sql-view-expand-design.md) |
| **Enables** | [**WI-298**](WI-298-ai-sql-view-expand-sql-wiring.md) |

## Goal

Implement **ChatExpandHost** — full chat content pane, back arrow, message-anchor return, expand registry dispatch.

## Deliver

### `ui/mill-ui/src/components/chat/expand/`

- `ChatExpandHost.tsx`, `ExpandHeader.tsx`, `expandRegistry.ts`, `useChatExpand.tsx`

### Integration

- [`ChatArea`](../../../../ui/mill-ui/src/components/chat/ChatArea.tsx): hide [`MessageList`](../../../../ui/mill-ui/src/components/chat/MessageList.tsx) when expand active.
- v1: **`general`** chat only.

### Tests

- Vitest: open/close; scroll-to-message; registry dispatch.

## Acceptance criteria

- [ ] Expand from message A; back → message A visible.
- [ ] Only chat types with `expand` transition mount host.
