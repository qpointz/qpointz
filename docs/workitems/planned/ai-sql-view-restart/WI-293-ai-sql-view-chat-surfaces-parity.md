# WI-293 — Chat-type artefact treatment wiring

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` |
| **Area** | `ui` |
| **Depends on** | [**WI-290**](WI-290-ai-sql-view-get-artifact-replay.md), [**WI-292**](WI-292-ai-sql-view-artifact-preview-ui.md) |
| **Enables** | [**WI-294**](WI-294-ai-sql-view-condensed-verification.md) |

## Goal

Wire artefact **stream + GET hydration** into every chat host; resolve **`ChatType`** and apply the
matching row from **`chatArtifactTreatments`** (WI-289). Different chat types **must not** share one
global preview path.

Build on artefacts-branch SSE parsing in [`ChatContext`](../../../../ui/mill-ui/src/context/ChatContext.tsx)
and [`chatService.ts`](../../../../ui/mill-ui/src/services/chatService.ts) — add wire replay and
artifact update hooks only; **no salvage** on stream end.

## Deliver

### Shared infrastructure

- GET hydration: [`parseWireArtifacts`](../../../../ui/mill-ui/src/utils/artifactWireParse.ts) in `turnToMessage`.
- `updateMessageArtifacts` / `SET_MESSAGE_ARTIFACTS` for Run → data artefact updates.
- `resolveChatType(context)`:
  - General `/chat` → `general`
  - Inline drawer → `inline-{contextType}`
- Pass `chatType` into [`MessageArtifactComposer`](WI-292-ai-sql-view-artifact-preview-ui.md).

### Per chat type (v1)

| ChatType | Wiring |
|----------|--------|
| **`general`** | Full `SqlDataCondensedPreview` + Run/Export/Expand/Open in Analysis |
| **`inline-analysis`** | `hostIntegrations`: structured `sql` → editor via `useInlineChatListener`; **no** preview card |
| **`inline-model`** | Facet `conversation-card` via `ArtifactCard`; SQL compact stub |
| **`inline-knowledge`** | Facet/concept card via `ArtifactCard` |

### Context updates

- [`InlineChatContext`](../../../../ui/mill-ui/src/context/InlineChatContext.tsx): wire replay + artefact stream (artefacts SSE base).
- [`ChatArea`](../../../../ui/mill-ui/src/components/chat/ChatArea.tsx): expand host, artifact callbacks.

### Feature flags

- `chatSqlExecute` — gates Run/Export on preview chat types only.

## Out of scope

- Expand pane body → WI-295–WI-298.
- Salvage inference.
- Runtime emission changes.

## Acceptance criteria

- [ ] `general` `/chat`: live structured SQL (from coordinator) → Run → Data → GET reload lazy hydrate.
- [ ] `inline-analysis`: SQL updates editor; drawer has **no** `SqlDataCondensedPreview`.
- [ ] `inline-model` / `inline-knowledge`: `ArtifactCard` for facet/schema/unknown.
- [ ] GET hydrates `Message.artifacts` on all chat types.
- [ ] No client salvage code paths.
