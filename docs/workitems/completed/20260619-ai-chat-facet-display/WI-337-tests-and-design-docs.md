# WI-337 — Tests and design docs

**Story:** [`ai-chat-facet-display`](STORY.md) · **Backlog:** U-16  
**Seq:** 3 · **Depends on:** [WI-336](WI-336-facet-condensed-preview.md)

## Status: complete

## Goal

Lock behaviour with Vitest and update design docs so general-chat facet presentation is documented
alongside SQL condensed preview.

## Scope

### Tests

| File | Coverage |
|------|----------|
| `artifactPreview/__tests__/FacetCondensedPreview.test.tsx` (new) | Tab labels (`Facet:Descriptive` + JSON); facet panel renders field labels when descriptor mocked; JSON tab content; `reserveLayout` action-bar region present |
| `artifactPreview/__tests__/chatArtifactTreatments.test.ts` | `resolveArtifactTreatment('general', 'facet-proposal')` → `condensed-preview`; inline hosts still `conversation-card` |
| Optional | `FacetReadOnlyBody` unit test if not added in WI-335 |

Follow patterns from [`SqlDataCondensedPreview.test.tsx`](../../../../ui/mill-ui/src/components/chat/artifactPreview/__tests__/SqlDataCondensedPreview.test.tsx) (mock `facetTypeService`, MantineProvider wrapper).

### Design docs

| Doc | Update |
|-----|--------|
| [`docs/design/ai/chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md) | §5 treatment table: general `facet-proposal` = condensed box; §7 module list: add `FacetCondensedPreview`; mermaid optional |
| [`docs/design/agentic/artifact-foundation.md`](../../../design/agentic/artifact-foundation.md) | Replace general-chat `FacetProposalArtifactCard` reference with `FacetCondensedPreview`; note inline still uses card |

## Acceptance criteria

- [x] `npm run test` passes for new/updated tests in `ui/mill-ui`
- [x] `npm run build` and `npm run lint` pass
- [x] Design docs reflect general vs inline facet treatment split and shared read-only facet layer
- [x] Manual smoke steps in [STORY.md](STORY.md) verified on local stack with metadata + AI enabled

## Out of scope

- Public user docs under `docs/public/src/` (optional follow-up)
- BACKLOG row → `done` (story closure only, per RULES)

## Commit

`[docs] WI-337: facet condensed preview tests and design docs`
