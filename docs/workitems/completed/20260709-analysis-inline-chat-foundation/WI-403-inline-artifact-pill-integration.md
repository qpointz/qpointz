# WI-403 — Inline artifact pill integration

**Status:** complete

## Goal

Integrate SQL/facet strips with pill shell, icon-only actions, facet accept/reject via shared lifecycle hook.

## Delivered

- `inlineArtifactActionPlacement.ts` — strip vs popover action split (Copy in popover only)
- Icon `InlineArtifactStripActionBar` with accept/reject/open-in-model
- `facetProposalLifecycle.ts` shared with `FacetCondensedPreview`
- `FacetInlineArtifactStrip` + `SqlDataInlineArtifactStrip` refactored to pill shell
- `chatArtifactTreatments` inline facet: accept/reject
- `InlineChatMessage` artifact-only `fit-content` width

## Post-polish (`ed6b272d`)

See [`WI-403-post-polish.md`](WI-403-post-polish.md) — per-artifact applied dot, hover expand hint,
facet reject strikethrough, scroll signature, apply icon and layout fixes.

## Verify

```bash
cd ui/mill-ui && npx vitest run src/components/chat/artifactPreview
```
