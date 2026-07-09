# WI-400 - Inline chat UI polish

## Goal

Refine the inline chat drawer UX for Analysis copilot: clearer copilot chrome, responsive artifact
strips in narrow columns, and a richer empty state with starter prompts — without changing backend
contracts or host action semantics.

## Requirements

### Copilot chrome

- Analysis sessions show **Analysis copilot** as the drawer title (query name in context banner).
- Context-type accent colors stay consistent (teal/cyan for analysis).

### Empty state

- Before the first user message, show the compact empty state instead of a generic greeting bubble.
- Analysis empty state includes clickable **starter prompts** (mock-friendly keywords).

### Message layout

- Tighter assistant padding in the narrow drawer.
- Artifact-only assistant replies use a light surface (strips carry the visual weight).

### Artifact strips

- Strips stack identity and actions vertically in narrow drawer widths (~280–380px).
- Action bar may wrap; buttons stay tappable without horizontal overflow.

### Tests

- Unit tests for inline chat label/suggestion helpers.
- Existing inline chat and strip tests must pass.

## Acceptance criteria

- `/analysis` inline drawer reads as Analysis copilot, not generic "Context Chats".
- Starter prompts send a message when clicked (empty state).
- SQL strip remains usable at `minRightPx` split width without clipped Apply actions.
- `npm run test -- --run` passes for mill-ui.

## Verification

```bash
cd ui/mill-ui && npm run test -- --run
cd ui/mill-ui && npm run build
```
