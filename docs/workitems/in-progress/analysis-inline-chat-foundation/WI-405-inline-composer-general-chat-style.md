# WI-405 - Inline composer General Chat style

## Goal

Present the inline chat composer in the same pane as the transcript with General Chat chrome (minimal
height, autosize), removing the separate bordered footer box.

## Implementation

- `ChatInputBox`: `variant` prop (`default` | `compact` | `inline`).
- `InlineChatInput`: `variant="inline"`, no `borderTop`.
- `InlineChatPanel`: floating gradient composer layout (mirrors `ChatArea`).

## Acceptance criteria

- Inline drawer composer visually matches General Chat surface treatment at drawer width.
- Composer autosizes; no fixed tall footer box separating transcript from input.
- `npm run test -- --run` passes.

## Status

Complete (Stage 8).
