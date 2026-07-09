# WI-402 — Inline artifact pill shell

**Status:** complete

## Goal

Shared `InlineArtifactPillStrip` with colored type badges and single-line headlines. Expand
affordance refined in post-WI-403 polish (hover-only expand icon; see `WI-403-post-polish.md`).

## Delivered

- `InlineArtifactPillStrip.tsx`, `inlineArtifactTypeBadge.ts`, `InlineArtifactTypeBadge.tsx`
- `inlineArtifactHeadline.ts`
- Pill CSS in `InlineArtifactStrip.module.css`

## Verify

```bash
cd ui/mill-ui && npx vitest run src/components/chat/artifactPreview/__tests__/inlineArtifactTypeBadge.test.ts src/components/chat/artifactPreview/__tests__/inlineArtifactHeadline.test.ts
```
