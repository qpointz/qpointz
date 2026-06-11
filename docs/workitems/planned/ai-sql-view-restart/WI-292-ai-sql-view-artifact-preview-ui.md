# WI-292 — Artefact preview UI (`SqlDataCondensedPreview`)

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🧪 test` |
| **Area** | `ui` |
| **Depends on** | [**WI-289**](WI-289-ai-sql-view-design-contract.md), [**WI-291**](WI-291-ai-sql-view-query-execute-attach.md) |
| **Enables** | [**WI-293**](WI-293-ai-sql-view-chat-surfaces-parity.md) |

## Goal

Implement **`chatArtifactTreatments`** framework and first preview: **`SqlDataCondensedPreview`** for
**`general`** chat only — condensed, non-destructive, tabbed SQL ↔ Data card with action bar.
`inline-analysis` uses `hostIntegrations` instead (WI-293).

Port additive files from old branch `origin/feat/ai-chat-sql-result-view` as reference;
**no salvage code**.

## Deliver

### Framework — `ui/mill-ui/src/components/chat/artifactPreview/`

| File | Role |
|------|------|
| `types.ts` | `ChatType`, `ArtefactKind`, `ArtifactTreatment`, `ArtifactRenderGroup` |
| `chatArtifactTreatments.ts` | **`ChatType` → (kind → treatment)** registry (WI-289) |
| `registry.tsx` | Kind → preview/card component |
| `hostIntegrations.ts` | Per-`ChatType` host-apply handlers |
| `MessageArtifactComposer.tsx` | Applies treatment by `chatType` |
| `ArtifactPreviewRouter.tsx` | Top-level assistant layout (sql-primary / facet-primary sections) |
| `ChatArtifactCard.tsx` | Shared card shell |
| `ChatArtifactActionBar.tsx` | Slim in-card actions |

### SQL composite preview

- **`SqlReadOnlyPanel`**, **`SqlDataCondensedPreview`**, **`useChatArtifactRun`**, **`useOpenInAnalysis`**, **`useLazyArtifactData`**
- Actions: Run, Copy, Export, Expand (WI-298), Open in Analysis

### Integration (extend artefacts UI — do not delete)

- **General SQL:** `ArtifactPreviewRouter` → `SqlDataCondensedPreview` (replaces basic `SqlArtifactCard` for `general` only).
- **Facet / schema / unknown:** route through existing [`ArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifacts/ArtifactCard.tsx)
  via `resolveCardComponent` — keep [`SchemaCaptureArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifacts/SchemaCaptureArtifactCard.tsx),
  [`UnknownArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifacts/UnknownArtifactCard.tsx) from artefacts branch.
- Extend [`chatArtifactParse.ts`](../../../../ui/mill-ui/src/utils/chatArtifactParse.ts) for `partType=data` only — **keep** artefacts-branch
  `schema-capture` / `unknown` parsing; **no** prose/JSON salvage helpers.
- [`MessageBubble`](../../../../ui/mill-ui/src/components/chat/MessageBubble.tsx): assistant path uses `ArtifactPreviewRouter` with `chatType` prop.

### Tests

- Vitest: `chatArtifactTreatments` lookup; grouping; `useOpenInAnalysis` handoff (no executionId).

## Out of scope

- Expand implementation → WI-295–WI-298.
- InlineChatContext wiring → WI-293.
- Salvage paths.

## Acceptance criteria

- [ ] One tabbed card per SQL-bearing reply in `general` chat.
- [ ] SQL arrives via structured SSE (artefacts foundation) — not prose salvage.
- [ ] Facet/schema/unknown still render via artefacts-branch `ArtifactCard`.
- [ ] Open in Analysis handoff works (no auto-save, no executionId).
- [ ] `npm run test` passes for new/changed tests.
