# Chart assistant reply view (`chart-primary`)

Normative for **WI-370** / Gap 24.

## Decision

Add **`chart-primary`** to [`AssistantReplyView`](../../../../ui/mill-ui/src/types/chat.ts). Chart turns use
the **same reply chrome as `sql-primary`** — only the section label and derivation inputs differ.
No new layout component, router branch, or bubble styling.

## Layout chrome (same as SQL)

| Concern | `sql-primary` today | `chart-primary` |
|---------|---------------------|-----------------|
| Router pattern | `StructuredReplyLayout` (section label + artefact stack + optional prose) | **Identical** |
| General chat (`MessageBubble` → `ArtifactPreviewRouter`) | `MessageArtifactComposer` + grouped cards | **Identical** — composite card from Gap 22 |
| Interleaved SSE layout | `InterleavedAssistantReply` + `ArtifactGroupRenderer` | **Identical** |
| Section title | `"SQL"` via `structuredReplySectionTitle` | **`"Chart"`** |
| Prose below artefacts | Optional `MessageContent` when `message.content` non-empty | **Identical** |

`AssistantReplyRouter` (grinder-style hosts) and `ArtifactPreviewRouter` (general chat) both
resolve view from `deriveAssistantReplyView`; chart does not fork either router.

## Derivation ([`deriveAssistantReplyView`](../../../../ui/mill-ui/src/utils/assistantReplyView.ts))

Updated precedence:

1. `facet-primary` — any `facet-proposal` artefact (unchanged)
2. **`chart-primary`** — any `kind: 'chart'` wire artefact
3. `sql-primary` — `sql` or `data` without chart
4. `artifact-primary` — `unknown` structured artefact
5. `conversation` — default

`item.completed` hints (live SSE):

| Hint | View |
|------|------|
| `partType: 'chart'` | `chart-primary` |
| `partType: 'multi'` containing `chart` | `chart-primary` if any `chart` in `partTypes` and no facet types win precedence |
| `partType: 'sql'` | `sql-primary` (only when no chart artefact on turn) |

Mixed **SQL + chart** on one turn → **`chart-primary`** (chart is the user-visible deliverable;
artefacts still group into one `sql-data-composite` per Gap 22).

## Wire / GET replay

- Add `chart-primary` to `assistantReplyViewFromWire` allow-list.
- GET transcript may omit `assistantReplyView` (current behaviour); client derives from wire
  `kind: 'chart'` artefacts.
- Optional future: server sets `assistantReplyView` on `TurnResponse` when persisting chart turns.

## Commentary lead-ins ([`commentaryForArtifactGroup`](../../../../ui/mill-ui/src/utils/replySegments.ts))

When composite has `chart` and no streamed text segment:

| Composite | Lead-in |
|-----------|---------|
| `chart` only | `Chart:` or title from chart payload when present |
| `chart` + sql/data | `Chart:` (same pattern as `Generated SQL:` / `Query results:`) |

## Out of scope

- New `chart-primary` layout component or CSS variant.
- `artifact-primary` fallback for chart turns when chart wire parse succeeds.
- Separate expand/bubble chrome — handled by `sql-data-composite` (Gap 22).

## Tests (WI-370)

- `assistantReplyView.test.ts` — derive `chart-primary` from chart artefact; wire parse;
  `structuredReplySectionTitle` → `"Chart"`; SQL+chart → `chart-primary`; facet beats chart.
- `replySegments.test.ts` — chart composite commentary when applicable.
