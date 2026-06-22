# ai-chat-facet-display

**Backlog:** [U-16](../../BACKLOG.md) · **Status:** `closed` (merge-ready **2026-06-19**)

## Goal

When **general chat** (`/chat`) receives a **`facet-proposal`** artefact from the metadata /
schema-authoring capability, render it in the **same condensed artefact shell as SQL query results**
(`ChatArtifactCard` + tabs + action-bar column) — not the current stub
[`FacetProposalArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifacts/FacetProposalArtifactCard.tsx).

The **Facet** tab uses the same read-only facet presentation as the Data Model
([`EntityDetails`](../../../../ui/mill-ui/src/components/data-model/EntityDetails.tsx)). The **JSON**
tab shows the raw wire payload. Action buttons are **not** implemented in this story; the action-bar
slot is **reserved** for a follow-up (promote, copy, etc.).

**Reference implementation:** [`SqlDataCondensedPreview`](../../../../ui/mill-ui/src/components/chat/artifactPreview/SqlDataCondensedPreview.tsx)

## Branching

```bash
git fetch origin dev
git checkout -b feat/ai-chat-facet-display origin/dev
```

## Prerequisites (on `dev`)

| Story | Relevance |
|-------|-----------|
| [`ai-sql-view-restart`](../../completed/20260612-ai-sql-view-restart/STORY.md) (WI-289–298) | `ChatArtifactCard`, `SqlDataCondensedPreview`, `chatArtifactTreatments`, expand host |
| [`ai-artifact-emit-contract`](../../completed/20260616-ai-artifact-emit-contract/STORY.md) (WI-300–308) | `facet-proposal` SSE / wire emission via `metadata-authoring` |
| [`ai-v3-mill-ui-general-chat`](../../completed/20260506-ai-v3-mill-ui-general-chat/STORY.md) (WI-233) | Chat metadata-scope **design** only — persistence is out of scope here |

## Problem statement

Today, `general` + `facet-proposal` uses `conversation-card` mode and routes to
`FacetProposalArtifactCard`: entity id, facet type key, and collapsible JSON. That reads as a blank
stub compared to SQL/data artefacts and does not reuse metadata facet descriptors or field layout.

## Architecture decisions (locked)

### 1. General chat only

Only `chatType: general` switches to `condensed-preview` for `facet-proposal`. Inline model /
knowledge / analysis hosts keep `conversation-card` → existing stub card until a later story.

### 2. SQL shell parity

`FacetCondensedPreview` mirrors `SqlDataCondensedPreview`:

- [`ChatArtifactCard`](../../../../ui/mill-ui/src/components/chat/artifactPreview/ChatArtifactCard.tsx)
- Mantine `Tabs`: dynamic facet title tab + **JSON**
- [`ChatArtifactActionBar`](../../../../ui/mill-ui/src/components/chat/artifactPreview/ChatArtifactActionBar.tsx) with `reserveLayout` (empty handlers)

### 3. Shared read-only facet renderer

Extract schema-driven read-only rendering from `EntityDetails` into
`ui/mill-ui/src/components/data-model/facets/` so chat and Data Model stay visually aligned.
WI-335 reverts any unintegrated draft spike files and redoes extraction cleanly.

### 4. Facet type lookup

Load descriptor via `facetTypeService.get(normalizeFacetTypeKeyForApi(facetTypeKey))`. On failure,
fall back to generic object / JSON display — no crash.

### 5. Action bar reservation

`ChatArtifactActionBar` gains `reserveLayout?: boolean` so the tab row aligns with SQL artefacts
when `enabledActions` is empty. Planned future actions (comments in `types.ts`): `promote`,
`copy-json`.

### 6. No feature flag

`FacetCondensedPreview` is **always on** for general chat when a `facet-proposal` artefact is
present — no new feature flag (unlike optional `chatSqlExecute` gating for SQL run/export).

### 7. First tab label format

First tab label: **`Facet:<Type>`** where `<Type>` is the facet type display title from
`facetBoxBaseTitle` (e.g. `Facet:Descriptive`, `Facet:Structural`). Entity URN remains in the panel
header inside the tab, not in the tab label.

### 8. WI-335 starts clean

Revert uncommitted draft files [`FacetPayloadReadOnly.tsx`](../../../../ui/mill-ui/src/components/data-model/facets/FacetPayloadReadOnly.tsx) and
[`facetDisplayUtils.ts`](../../../../ui/mill-ui/src/components/data-model/facets/facetDisplayUtils.ts) from the interrupted spike; redo extraction cleanly in WI-335.

## Code map

| Area | Path |
|------|------|
| SQL reference shell | `ui/mill-ui/src/components/chat/artifactPreview/SqlDataCondensedPreview.tsx` |
| Treatments | `ui/mill-ui/src/components/chat/artifactPreview/chatArtifactTreatments.ts` |
| Preview registry | `ui/mill-ui/src/components/chat/artifactPreview/registry.tsx` |
| Composer router | `ui/mill-ui/src/components/chat/artifactPreview/MessageArtifactComposer.tsx` |
| Current stub | `ui/mill-ui/src/components/chat/artifacts/FacetProposalArtifactCard.tsx` |
| Data Model source | `ui/mill-ui/src/components/data-model/EntityDetails.tsx` |
| Shared facets (target) | `ui/mill-ui/src/components/data-model/facets/` |
| Facet type API | `ui/mill-ui/src/services/facetTypeService.ts` |
| Wire parse | `ui/mill-ui/src/utils/chatArtifactParse.ts`, `artifactWireParse.ts` |
| Design | `docs/design/ai/chat-artefact-architecture.md` |

## Implementation order

| Seq | WI | Depends | Delivers |
|-----|-----|---------|----------|
| 1 | [WI-335](WI-335-extract-facet-readonly-renderer.md) | — | Shared `FacetPayloadReadOnly`, `FacetReadOnlyBody`; EntityDetails refactor |
| 2 | [WI-336](WI-336-facet-condensed-preview.md) | WI-335 | `FacetCondensedPreview`, `reserveLayout`, treatment + registry wiring |
| 3 | [WI-337](WI-337-tests-and-design-docs.md) | WI-336 | Vitest, design doc updates |

## Test commands

```bash
cd ui/mill-ui
npm run test -- FacetCondensedPreview chatArtifactTreatments EntityDetails
npm run build
npm run lint
```

Manual: general chat with `schema-authoring` profile → capture facet → verify tabbed box matches SQL
artefact chrome; Facet tab matches Data Model fields; JSON tab shows wire object; inline model chat
unchanged.

## Out of scope

- Facet action buttons (promote, copy, open in Data Model, expand pane)
- Inline chat hosts (`inline-model`, `inline-knowledge`, `inline-analysis`) tabbed shell
- Chat-scope facet persistence / agent prelude ([`ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md))

## Work Items

- [x] WI-335 — Extract shared facet read-only renderer from EntityDetails ([`WI-335-extract-facet-readonly-renderer.md`](WI-335-extract-facet-readonly-renderer.md))
- [x] WI-336 — FacetCondensedPreview (SQL shell parity + reserved action bar) ([`WI-336-facet-condensed-preview.md`](WI-336-facet-condensed-preview.md))
- [x] WI-337 — Vitest coverage + design doc updates ([`WI-337-tests-and-design-docs.md`](WI-337-tests-and-design-docs.md))

## References

- [`docs/workitems/RULES.md`](../../RULES.md)
- [`docs/design/ai/chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md)
- [`docs/design/agentic/artifact-foundation.md`](../../../design/agentic/artifact-foundation.md)
- [`docs/design/agentic/ai-v3-chat-metadata-scope.md`](../../../design/agentic/ai-v3-chat-metadata-scope.md) (follow-up persistence story)
