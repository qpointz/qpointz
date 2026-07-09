# WI-404 - Inline chat UI behaviour design

## Goal

Lock Stage 8 interaction behaviour for inline chat across Analysis, Model, and Knowledge hosts:
composer presentation, splitter resize limits, and per-host session binding (drawer open/close and
active session selection).

## Locked behaviour

### Host session binding (WI-407)

| Rule | Behaviour |
|------|-----------|
| Per-host session key | `(contextType, contextId)` — not `contextId` alone |
| Host has session | Auto-activate that session and open drawer |
| Host has no session | Close drawer; sessions remain in memory |
| Route change away from inline views | Close drawer (`/chat`, `/overview`, etc.) |
| Analysis binding | Owned by `QueryPlayground` (`activeQueryId`) |
| Model / Knowledge binding | Route sync from URL (`InlineChatRouteHostSync`) |

### Session indicators

| Indicator | Meaning |
|-----------|---------|
| Red dot on `InlineChatButton` | Session exists for **this** `contextType` + `contextId` |
| Sidebar teal chat icon | Any session exists for that sidebar row's host (not gated on drawer open) |

### Composer (WI-405)

- Composer lives in the **same pane** as the transcript (no separate bordered footer box).
- Visual treatment matches General Chat (`composerSurfaceStyle`, floating gradient footer).
- `ChatInputBox` `variant="inline"`: smaller padding, autosize up to 5 rows.

### Splitter (WI-406)

- Replace fixed `maxRightPx={560}` with `maxRightFraction={0.5}` of usable width.
- `minLeftPx={280}`, `minRightPx={260}`.
- Persisted width clamped via `horizontalSplitPaneMath`.

## Acceptance criteria

- Design decisions recorded in `GAPS.md` §17 and `INLINE-CHAT-FOUNDATION.md` interaction chapter.
- Stage 8 WIs 405–407 implement the locked rules without changing backend contracts.

## Verification

- Manual checks in `STORY.md` Stage 8 section.
- `cd ui/mill-ui && npm run test -- --run`
