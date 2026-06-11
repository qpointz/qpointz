# WI-296 — Shared QueryDataView component

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` / `🔧 refactoring` |
| **Area** | `ui` |
| **Depends on** | [**WI-295**](WI-295-ai-sql-view-expand-design.md) |
| **Enables** | [**WI-297**](WI-297-ai-sql-view-expand-shell.md), [**WI-298**](WI-298-ai-sql-view-expand-sql-wiring.md) |

## Goal

Extract **`QueryDataView`** from [`QueryResults.tsx`](../../../../ui/mill-ui/src/components/queries/QueryResults.tsx)
for reuse in Analysis, in-chat condensed, and expand pane.

Port from old branch `components/data/` as reference.

## Deliver

### `ui/mill-ui/src/components/data/`

- `QueryDataView.tsx`, `DataGrid.tsx`, `DataToolbar.tsx`, `types.ts`

### Modes

- `playground` — Analysis (existing behaviour).
- `condensed` — in-chat (~10 rows, chat toolbar density).
- `expanded` — expand pane (full paging, chat toolbar density).

### Refactors

- [`QueryPlayground`](../../../../ui/mill-ui/src/components/queries/QueryPlayground.tsx) → `playground`.
- `SqlDataCondensedPreview` → `condensed`.

### Tests

- Vitest: mode matrix; export gated on SQL + `executionId`.

## Acceptance criteria

- [ ] Analysis regression: paging, export, sorting unchanged.
- [ ] Condensed bounded height preserved.
