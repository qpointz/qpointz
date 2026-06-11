# WI-295 — Shared QueryDataView component

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🔧 refactoring` |
| **Area** | `ui` |
| **Depends on** | [**WI-294**](WI-294-ai-sql-view-expand-design.md) |
| **Enables** | [**WI-296**](WI-296-ai-sql-view-expand-implementation.md) |

## Goal

Extract **`QueryDataView`** from [`QueryResults.tsx`](../../../../ui/mill-ui/src/components/queries/QueryResults.tsx)
for reuse in Analysis, in-chat condensed, and expand pane.

Use old branch `components/data/` as **UX reference**; implement fresh on artefacts foundation.

## Deliver

### `ui/mill-ui/src/components/data/`

- `QueryDataView.tsx`, `DataGrid.tsx`, `DataToolbar.tsx`, `types.ts`

### Modes

- `playground` — Analysis (existing behaviour).
- `condensed` — in-chat (~10 rows, chat toolbar density).
- `expanded` — expand pane (full paging, chat toolbar density).

### Refactors

- [`QueryPlayground`](../../../../ui/mill-ui/src/components/queries/QueryPlayground.tsx) → `playground` mode.
- `SqlDataCondensedPreview` → `condensed` mode.

### Tests

- Vitest: mode matrix; export gated on SQL + `executionId`.

## Acceptance criteria

- [ ] Analysis regression: paging, export, sorting unchanged.
- [ ] Condensed bounded height preserved.
