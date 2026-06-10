# WI-271 - Visual Analysis board MVP implementation plan

Status: `planned`
Type: `feature`, `docs`
Area: `ui`, `services`, `data`
Backlog refs: **U-14**

## Goal

Define the first shippable Visual Analysis board workflow: a business user can select a source,
filter/select/derive/aggregate data, preview output after each step, and save the analysis.

## MVP Workflow

1. Create `New Visual Analysis`.
2. Pick a source table from the schema catalog.
3. See a `Table` preview board for the source.
4. Add a `Filter` board with visual predicate controls.
5. Add a `Select` board to keep/rename/reorder columns.
6. Add an `Expression` board to derive one new column.
7. Add an `Aggregate` board with group-by and measures.
8. Inspect the result table.
9. Save the Visual Analysis spec.

## Scope

1. Board stack UI:
   - ordered board list.
   - add board menu.
   - duplicate/delete board.
   - board status: valid, invalid, preview running, preview failed.
2. Board config panels:
   - source picker.
   - filter predicates with `AND` first; `OR` can be follow-up.
   - select/rename/reorder columns.
   - expression editor with function hints.
   - aggregate group-by and measure selector.
3. Preview execution:
   - compile boards up to selected board.
   - call query session API.
   - show paged rows and schema.
   - show compiler warnings/errors inline.
4. Validation:
   - missing source.
   - unknown column.
   - type mismatch.
   - invalid expression.
   - aggregate without measures/grouping.
5. Tests:
   - pure board-spec validation tests.
   - compiler fixtures.
   - UI smoke tests for add/configure/save flows where repo patterns allow.

## Acceptance

- MVP board workflow is implementable in small follow-up WIs.
- Every MVP board has validation and preview semantics.
- Preview uses existing query sessions, not ad hoc full-result fetches.
- Generated SQL is inspectable in developer/debug UI or logs for troubleshooting.
- Known unsupported board types are deferred explicitly.

## Deferred Board Types

- Map/geospatial board.
- Time-series board.
- Pivot table board beyond basic aggregate output.
- Histogram/distribution selection filtering.
- Chart-to-chart filtering.
- Union board.
- Writeback/action boards.
- Advanced window functions.

## Performance Rules

- Cap preview page size.
- Push filters before joins where possible.
- Warn when joining before filtering large sources.
- Avoid loading distinct-value suggestions without caps.
- Cache only with explicit invalidation/version rules.
