# WI-291 — Preview framework + condensed SQL

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ui` |
| **Depends on** | [**WI-289**](WI-289-ai-sql-view-design-contract.md), [**WI-290**](WI-290-ai-sql-view-backend-replay-attach.md) |
| **Enables** | [**WI-292**](WI-292-ai-sql-view-chat-wiring.md) |

## Goal

Implement **`chatArtifactTreatments`** framework and first preview: **`SqlDataCondensedPreview`** for
**`general`** chat — condensed, non-destructive, tabbed SQL ↔ Data card with action bar.

**Fresh implementation** on the artefacts foundation. Use `origin/feat/ai-chat-sql-result-view` as
**read-only UX reference** only — **no salvage code**, no blind copy of integration files.

## Deliver

### Framework — `ui/mill-ui/src/components/chat/artifactPreview/`

| File | Role |
|------|------|
| `types.ts` | `ChatType`, `ArtefactKind`, `ArtifactTreatment`, `ArtifactRenderGroup` |
| `chatArtifactTreatments.ts` | **`ChatType` → (kind → treatment)** registry (WI-289) |
| `artifactGroups.ts` | Group `sql` + `data` → `sql-data-composite` |
| `registry.tsx` | Kind → preview/card component |
| `hostIntegrations.ts` | Per-`ChatType` host-apply handlers |
| `MessageArtifactComposer.tsx` | Applies treatment by `chatType` |
| `ArtifactPreviewRouter.tsx` | Top-level assistant layout (sql/facet/schema sections) |
| `ChatArtifactCard.tsx` | Shared card shell |
| `ChatArtifactActionBar.tsx` | Slim in-card actions |

### SQL composite preview

- **`SqlReadOnlyPanel`**, **`SqlDataCondensedPreview`**
- **`useChatArtifactRun`** — Run via `queryService`; call `attachExecutionResult` (WI-290)
- **`useOpenInAnalysis`**, **`useLazyArtifactData`**
- Actions: Run, Copy, Export, Expand (stub/wire in WI-296), Open in Analysis

### Foundation integration rules (mandatory)

- **No salvage imports** — do not use `resolveMessageArtifacts`, `assistantDisplayContent`, or prose inference; use `message.artifacts` from SSE + GET only
- **General SQL:** `SqlDataCondensedPreview` (evolve styling from [`SqlArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifacts/SqlArtifactCard.tsx))
- **Facet / schema-capture / unknown:** `conversation-card` → existing [`ArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifacts/ArtifactCard.tsx) via `registry.tsx` (include `SchemaCaptureArtifactCard`, `UnknownArtifactCard`)
- Extend [`chatArtifactParse.ts`](../../../../ui/mill-ui/src/utils/chatArtifactParse.ts) for `partType=data` only — **keep** schema-capture/unknown parsing
- Reuse [`assistantReplyView.ts`](../../../../ui/mill-ui/src/utils/assistantReplyView.ts) for `schema-primary` / `artifact-primary` section titles in `ArtifactPreviewRouter`

### Tests

- Vitest: `chatArtifactTreatments` lookup; grouping; `useOpenInAnalysis` handoff (no executionId)
- No dependency on salvage helper symbols in any new file

## Out of scope

- `ChatContext` / `InlineChatContext` wiring → WI-292
- Expand implementation → WI-294–296
- `MessageBubble` host props → WI-292

## Acceptance criteria

- [ ] Framework compiles and unit tests pass in isolation.
- [ ] `SqlDataCondensedPreview` renders SQL from `Message.artifacts` (mock/fixture).
- [ ] Run calls `queryService` + attach API (mocked).
- [ ] Facet/schema/unknown route through existing `ArtifactCard` components.
- [ ] No salvage symbols in new files.
