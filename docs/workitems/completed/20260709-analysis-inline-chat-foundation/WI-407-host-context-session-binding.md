# WI-407 - Host context session binding

## Goal

Fix stale drawer content when switching saved queries or routes: bind drawer visibility and active
session to the current host `(contextType, contextId)`.

## Problem

Switching Analysis queries kept the drawer open but showed the previous query's session. Cross-route
navigation (e.g. Analysis → Model) could show the wrong session or leave the drawer open on hosts
without a session.

## Implementation

- `InlineChatContext.getSessionByContext(contextType, contextId)`.
- `startSession` dedupes by type + id.
- `useInlineChatHostBinding` hook for mounted hosts (`QueryPlayground` for Analysis).
- `inlineChatRouteContext.ts` + `InlineChatRouteHostSync` for Model/Knowledge URL binding and
  closing drawer on non-inline routes.
- Indicators updated: `InlineChatButton`, sidebars use typed session lookup.

## Acceptance criteria

- Switching query A → B auto-shows B's session when it exists; hides drawer when B has none.
- Navigating to `/chat` or overview closes drawer.
- Analysis → Model shows model entity session when URL matches; hides when no session.
- Red dot reflects session for current host only.

## Status

Complete (Stage 8).
