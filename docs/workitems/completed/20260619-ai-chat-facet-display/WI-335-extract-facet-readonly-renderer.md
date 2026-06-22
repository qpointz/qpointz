# WI-335 — Extract shared facet read-only renderer

**Story:** [`ai-chat-facet-display`](STORY.md) · **Backlog:** U-16  
**Seq:** 1 · **Depends on:** —

## Status: complete

## Goal

Extract schema-driven **read-only** facet presentation from
[`EntityDetails.tsx`](../../../../ui/mill-ui/src/components/data-model/EntityDetails.tsx) into shared
modules under [`ui/mill-ui/src/components/data-model/facets/`](../../../../ui/mill-ui/src/components/data-model/facets/)
so chat facet proposals and the Data Model use the same visual renderer.

## Context

Draft files from an earlier interrupted spike **must be reverted** before starting this WI — redo
extraction cleanly (do not build on unintegrated drafts).

## Scope

### New / updated modules

| Module | Role |
|--------|------|
| `FacetPayloadReadOnly.tsx` | Recursive schema-driven read-only fields (OBJECT / ARRAY / primitives, stereotypes) |
| `FacetHyperlinkReadOnly`, `FacetEmailReadOnly` | Exported from `FacetPayloadReadOnly.tsx` (move from `EntityDetails`) |
| `facetDisplayUtils.ts` | `facetBoxBaseTitle` |
| **`FacetReadOnlyBody.tsx`** (new) | High-level body picker: structural vs schema vs JSON fallback |

### `FacetReadOnlyBody` behaviour

| Condition | Render |
|-----------|--------|
| `facetTypeKey.endsWith(':structural')` && `modelStructuralFacet` flag | [`StructuralFacet`](../../../../ui/mill-ui/src/components/data-model/facets/StructuralFacet.tsx) |
| `descriptor?.payload` present | `FacetPayloadReadOnly` |
| else | `SyntaxCodeEditor` or `CodeHighlight` JSON fallback |

Props (sketch): `facetTypeKey`, `payload`, `descriptor: FacetTypeManifest | null`, optional `structuralFacetEnabled`.

### EntityDetails refactor

- Remove inline `FacetHyperlinkReadOnly`, `FacetEmailReadOnly`, `facetBoxBaseTitle`, `renderReadOnlyField`
- Import shared modules; replace `renderReadOnlyField(...)` call sites with `<FacetPayloadReadOnly ... />`
- Move `appendEmailStereotypeValidationErrors` to shared module if still needed by edit flows

## Acceptance criteria

- [x] Revert/delete unintegrated draft files under `data-model/facets/` from the interrupted spike (if still present)
- [x] `FacetPayloadReadOnly`, `FacetReadOnlyBody`, `facetBoxBaseTitle` live under `data-model/facets/`
- [x] `EntityDetails.tsx` uses shared imports — **no visible UX change** in Data Model
- [x] [`EntityDetails.test.tsx`](../../../../ui/mill-ui/src/components/__tests__/EntityDetails.test.tsx) passes unchanged (or updated only if test ids shift)
- [x] No duplicate read-only facet logic left in `EntityDetails` for inferred / captured single / multi-row cards
- [x] Unit test for `FacetReadOnlyBody` or `FacetPayloadReadOnly` with a minimal descriptive descriptor (optional but recommended)

## Out of scope

- Chat-specific UI (`FacetCondensedPreview`) — WI-336
- Edit / form mode in EntityDetails — unchanged except import paths

## Commit

`[feat] WI-335: extract shared facet read-only renderer for chat and Data Model`
