# WI-370 - mill-ui SQL visualizations

Status: `done`  
Type: `feature`, `test`  
Area: `ui`

## Problem Statement

`ui/mill-ui` already renders SQL and data artifacts as one composite card. Charting should extend
that existing SQL experience. The UI should read chart visualization configs from the SQL artifact's
`visualizations[]` section and render them inside the SQL/data composite.

## Goal

Render chart visualizations from enriched SQL artifacts in `ui/mill-ui` using shared chart
infrastructure and bounded full data snapshots.

## Locked Decisions

- The UI reuses `sql-data-composite` for SQL artifacts with visualization data.
- Chart rendering uses bounded `full` snapshots, not paged grid slices.
- Shared chart renderer infrastructure lives under `ui/mill-ui/src/components/charts/`.
- The UI compiles semantic visualization config to ECharts locally.
- Run all hydrates SQL/data first, then loads chart snapshots.
- Chart-first reply layout is derived from `sql.visualizations[]`, not from a separate wire kind.

## In Scope

1. Extend the SQL artifact client type to support the nested payload:
   - `sql`
   - `info`
   - optional `schema`
   - optional `visualizations[]`
   - optional `profiling[]`
2. Extend live SSE and REST wire parsers so `kind: "sql"` artifacts preserve visualization data.
3. Keep artifact grouping as `sql-data-composite`.
4. Build reusable chart modules under `ui/mill-ui/src/components/charts/`:
   - `types.ts`
   - `compileChartSpecToECharts.ts`
   - `ChartRenderer.tsx`
   - `chartTheme.ts`
   - `chartData.ts`
5. Build a chart visualization compiler:
   - input: one `kind: "chart"` visualization config plus bounded row snapshot
   - output: ECharts options
   - no model-provided renderer config passthrough
   - unsupported chart types render a clear unsupported state
6. Build `ChartRenderer` as reusable infrastructure:
   - accepts plain chart config and data props
   - does not depend on chat message, artifact card, or expand types
   - handles resize in Mantine tabs/modals/split panes
7. Extend `SqlDataCondensedPreview` and `SqlDataExpandedView`:
   - SQL-only artifacts keep existing Data/SQL behavior
   - chart-enabled SQL artifacts show Chart, Data, and SQL views
   - multiple chart visualizations use chart sub-tabs or a selector inside the Chart view
8. Wire run semantics:
   - Run on chart-enabled SQL card fetches a bounded full snapshot
   - Data tab can continue to use paged results
   - Run all executes/hydrates SQL/data first, then loads chart snapshots
   - matching data from SQL execution may be reused when it satisfies chart snapshot needs
9. Add empty/error states:
   - no snapshot loaded
   - unsupported chart type
   - invalid visualization mapping
   - snapshot over limit or truncated
10. Add assistant reply routing so chart-enabled SQL artifacts can open with chart-first layout in
    general chat.
11. Update UI design references that still describe a separate chart wire part so they derive chart
    presence from SQL artifact visualization data.

## Out of Scope

- Backend chart rendering.
- Visual Analysis board workflow.
- Arbitrary chart builder UI.
- Inline chat chart UX beyond preserving the SQL artifact safely.
- Non-chart visualization kinds.

## Acceptance Criteria

- [x] `kind: "sql"` SSE and REST artifacts parse with nested visualization data intact.
- [x] SQL-only artifacts still render with existing SQL/data behavior.
- [x] SQL artifacts with chart visualizations render a Chart view.
- [x] Semantic chart configs compile to ECharts locally.
- [x] Chart rendering lives in reusable `components/charts` infrastructure.
- [x] `ChartRenderer` accepts plain chart/data props and has no dependency on chat artifact types.
- [x] Renderer-specific config from artifact payload is ignored or rejected.
- [x] Chart preview uses bounded full data snapshots, not paged grid slices.
- [x] Run loads data and renders chart visualizations.
- [x] Run all handles chart-enabled SQL artifacts after SQL/data hydration.
- [x] Over-limit or truncated snapshots do not render as silently complete charts.
- [x] Expanded view shows Chart, Data, and SQL without breaking existing SQL previews.
- [x] `assistantReplyView` derives chart-first chrome from non-empty chart visualizations on a SQL
      artifact.
- [x] UI tests cover parsing, grouping, compiler behavior, valid rendering, missing-data state, and
      invalid-mapping state.

## Suggested Commit

`[feat] WI-370: render SQL visualizations in mill-ui`
