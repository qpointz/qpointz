# WI-403 post-polish — Inline artifact strip UX fixes

**Status:** complete  
**Commit:** `ed6b272d` — `[fix] Polish inline SQL artifact strips after WI-403`  
**Branch:** `feat/inline-analysis-chat-poc`

Follow-up polish after WI-403 pill integration. No new WI checkbox in `STORY.md`; tracked here for
crash recovery and MR context.

## Summary

Tightened Analysis inline artifact strip behavior and layout after the WI-403 pill shell landed.
Main themes: **per-artifact applied state**, **quieter expand affordance**, **facet reject UX**,
and **scroll stability** when artifact status changes without new content.

## Changes

### 1. Green dot = this artifact was applied

**Problem:** Identical SQL from different turns/artifacts all showed the applied dot when editor SQL
matched — looked like “this SQL is in the editor”, not “I applied this proposal”.

**Fix:**

| File | Change |
|------|--------|
| `inlineSqlArtifactKey.ts` | Stable key: `artifactId`, else `messageId:sql:ordinal` |
| `analysisHostState.ts` | `setAnalysisAppliedArtifactKey` / `isAnalysisAppliedArtifact` (replaces editor-SQL match) |
| `SqlDataInlineArtifactStrip.tsx` | Sets key on Apply / Apply & Run; dot follows key |
| `QueryPlayground.tsx` | Auto-apply uses same key via `proposalIndex` |

### 2. Hover-only expand hint

**Problem:** Chevron read as a dropdown control.

**Fix:** `HiOutlineArrowsPointingOut` (10px) in `InlineArtifactPillStrip`; hidden until hover,
focus, or popover open. Headline uses inherited 12px chat size; pills are `inline-flex` /
left-aligned (`InterleavedAssistantReply`, `InlineChatMessage`).

### 3. Facet reject UX

- `rejected` on `InlineArtifactPillStrip` — dimmed strikethrough headline
- `FacetInlineArtifactStrip` + `FacetCondensedPreview` pass `lifecycle.isRejected`

### 4. Scroll jump on facet Reject

**Problem:** Accept/reject updated facet `status` and retriggered auto-scroll.

**Fix:** `buildChatScrollSignature()` in `chatMessageHelpers.ts` ignores facet status; used by
`InlineChatPanel` and `MessageList`. ResizeObserver scroll only while loading.

### 5. Action bar polish

- Apply icon: `HiOutlinePencilSquare` (not download-style square)
- Strip icon `onMouseDown` + `preventDefault` to avoid focus/scroll side effects

## Tests

```bash
cd ui/mill-ui && npx vitest run src/components/chat/artifactPreview
cd ui/mill-ui && npx vitest run src/components/queries/__tests__/analysisHostState.test.ts
cd ui/mill-ui && npx vitest run src/components/chat/__tests__/chatMessageHelpers.test.ts
```

New/updated:

- `inlineSqlArtifactKey.test.ts`
- `SqlDataInlineArtifactStrip.test.tsx` — identical SQL, only one green dot
- `analysisHostState.test.ts` — per-artifact key tracking
- `chatMessageHelpers.test.ts` — scroll signature ignores facet status

## Manual verify

1. Open Analysis inline chat; get two turns with the same SQL text.
2. Apply one strip — only that strip shows the green dot.
3. Hover pill preview zone — expand arrows appear; click opens popover.
4. Reject a facet proposal — chat should not jump scroll; headline strikethrough.
