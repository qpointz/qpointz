# WI-292 — Chat-type wiring + GET hydrate

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` |
| **Area** | `ui` |
| **Depends on** | [**WI-290**](WI-290-ai-sql-view-backend-replay-attach.md), [**WI-291**](WI-291-ai-sql-view-preview-framework.md) |
| **Enables** | [**WI-293**](WI-293-ai-sql-view-condensed-verification.md) |

## Goal

Wire artefact **stream + GET hydration** into every chat host; resolve **`ChatType`** and apply the
matching row from **`chatArtifactTreatments`** (WI-289). Different chat types **must not** share one
global preview path.

Build on artefacts-branch SSE parsing in [`ChatContext`](../../../../ui/mill-ui/src/context/ChatContext.tsx)
and [`chatService.ts`](../../../../ui/mill-ui/src/services/chatService.ts) — add wire replay and
artifact update hooks only; **no salvage** on stream end.

## Deliver

### Shared infrastructure

- GET hydration: [`parseWireArtifacts`](../../../../ui/mill-ui/src/utils/artifactWireParse.ts) in `turnToMessage` (today drops `turn.artifacts`)
- `updateMessageArtifacts` / reducer action for Run → data artefact updates
- `resolveChatType(context)`:
  - General `/chat` → `general`
  - Inline drawer → `inline-{contextType}`

### Message rendering

- [`MessageBubble.tsx`](../../../../ui/mill-ui/src/components/chat/MessageBubble.tsx): assistant path → `ArtifactPreviewRouter` with `chatType`, `conversationId`, `onArtifactsChange`, `precedingUserQuestion`
- Pass `chatType` into [`MessageArtifactComposer`](WI-291-ai-sql-view-preview-framework.md)

### Per chat type (v1)

| ChatType | Wiring |
|----------|--------|
| **`general`** | Full `SqlDataCondensedPreview` + Run/Export/Open in Analysis |
| **`inline-analysis`** | `hostIntegrations`: structured `sql` → editor via `useInlineChatListener`; **no** preview card |
| **`inline-model`** | Facet/schema cards via `ArtifactCard`; SQL compact stub |
| **`inline-knowledge`** | Facet/concept card via `ArtifactCard` |

### Context updates

- [`ChatContext.tsx`](../../../../ui/mill-ui/src/context/ChatContext.tsx): wire replay on `getChatDetail`; SSE structured parts unchanged from foundation
- [`InlineChatContext.tsx`](../../../../ui/mill-ui/src/context/InlineChatContext.tsx): same hydrate + stream base
- [`ChatArea.tsx`](../../../../ui/mill-ui/src/components/chat/ChatArea.tsx): artifact callbacks; expand host hook point for WI-296

### Feature flags

- `chatSqlExecute` in [`defaults.ts`](../../../../ui/mill-ui/src/features/defaults.ts) — gates Run/Export on preview chat types

## Out of scope

- Expand pane body → WI-294–296
- Salvage inference
- Runtime emission changes

## Acceptance criteria

- [ ] `general` `/chat`: live structured SQL (from coordinator) → Run → Data in message state.
- [ ] `inline-analysis`: SQL updates editor; drawer has **no** `SqlDataCondensedPreview`.
- [ ] `inline-model` / `inline-knowledge`: `ArtifactCard` for facet/schema/unknown.
- [ ] GET hydrates `Message.artifacts` on all chat types.
- [ ] No client salvage code paths.
