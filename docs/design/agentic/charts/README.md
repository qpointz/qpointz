# Chart type specifications

**Status:** Locked design input design input for WI-366 — normative for WI-368 / WI-370 implementation
**Parent contract:** [`../chart-artifact-contract.md`](../chart-artifact-contract.md)
**Target compiler:** `ui/mill-ui/src/components/charts/compileChartSpecToECharts.ts` (WI-370)
**Target renderer:** ECharts **6.x** via `echarts-for-react`

## Purpose

These documents are **implementation-facing specs** for each MVP chart type. They extend the
renderer-agnostic **`visualizations[]` chart entry** (inside `sql.generated`) with:

1. Normative semantic configuration (what the agent emits).
2. Validation rules (what the chart-mapping validator enforces).
3. **ECharts compilation mapping** (how `mill-ui` compiles semantic spec + row snapshot → ECharts
   `option` at render time).

Artifacts **never** store ECharts `option` JSON. ECharts config is always produced locally by the
compiler. The semantic config is designed so compilation is **deterministic** and uses standard
ECharts primitives (`dataset`, `encode`, axis types, `series.type`, `stack`, `areaStyle`).

All example artifacts use the **`visualizations[]`** model on `sql.generated` (one SQL artifact; multiple chart views by `key`). See
[`../sql-artifact-visualization-protocol.md`](../sql-artifact-visualization-protocol.md).

## MVP chart types

| Spec | `chartType` | Semantic roles | ECharts `series.type` |
|------|-------------|----------------|------------------------|
| [`bar-chart-spec.md`](./bar-chart-spec.md) | `bar` | `category`, `value`, optional `series` | `bar` |
| [`line-chart-spec.md`](./line-chart-spec.md) | `line` | `x`, `y`, optional `series` | `line` |
| [`area-chart-spec.md`](./area-chart-spec.md) | `area` | `x`, `y`, optional `series` | `line` + `areaStyle` |
| [`scatter-chart-spec.md`](./scatter-chart-spec.md) | `scatter` | `x`, `y`, optional `series`, `color` | `scatter` |
| [`pie-chart-spec.md`](./pie-chart-spec.md) | `pie` | `category`, `value` | `pie` |

Shared compiler rules (data prep, sorting, axis typing, legend/labels, theme):
[`echarts-compiler-contract.md`](./echarts-compiler-contract.md)

**Target architecture:** Chart specs become **`presentations[]`** entries on
[`generated-query`](../query-artifact-presentations.md) — not separate `generated-chart` rows.
Semantic content in this folder remains the source for chart presentation validation and compilation.

## Review checklist (WI-366)

- [x] Semantic roles match Gap 11 (chart-type-specific industry terms).
- [x] Every semantic field maps to a documented ECharts path or `encode` dimension.
- [x] Sample artifact + sample rows + compiled `option` are internally consistent.
- [x] Edge cases (empty data, truncation, series, stacked, time axis) are covered.
- [x] No ECharts-only keys appear in the durable artifact payload.

## Future families (non-MVP)

| Doc | Contents |
|-----|----------|
| [`forward-compatible-families.md`](./forward-compatible-families.md) | Gap 13 verification — treemap, sunburst, histogram + heatmap/bubble stress cases |
| [`multi-chart-artifact-model.md`](./multi-chart-artifact-model.md) | Gap 14 — one query per artifact; `charts[]` for multiple views |
| [`chart-context-resolution.md`](./chart-context-resolution.md) | **Locked (Gap 17):** prompt-based last query/chart resolution; multi-SQL disambiguation via title/description |
| [`chart-routing-intent.md`](./chart-routing-intent.md) | **Locked (Gap 18):** `CHART_MAP` on chart-mapping; profile composes only |
| [`chart-emission-path.md`](./chart-emission-path.md) | **Locked (Gap 19):** `validate_chart_spec` + `emitsOnSuccess` only; no model protocol final |
| [`chart-mcp-exposure.md`](./chart-mcp-exposure.md) | **Locked (Gap 20):** MCP tool surface for external agents (catalog, SQL, data, chart spec) |
| [`chart-test-proof-strategy.md`](./chart-test-proof-strategy.md) | **Locked (Gap 21):** layered mock-LLM proof; scenario export; ArtifactEmitScenariosIT packs |
| [`chart-ui-composite.md`](./chart-ui-composite.md) | **Locked (Gap 22):** extend `sql-data-composite` with optional `chart` tab — no separate artefact kind |
| [`chart-snapshot-fetch.md`](./chart-snapshot-fetch.md) | **Locked (Gap 23):** `queryService` `resultMode: 'full'` + `fetchChartSnapshot` for chart Run |
| [`chart-reply-view.md`](./chart-reply-view.md) | **Locked (Gap 24):** `chart-primary` — same reply chrome as `sql-primary`, section title `"Chart"` |
| [`chart-run-all.md`](./chart-run-all.md) | **Locked (Gap 25):** `collectChatChartTargets`; two-pass Run all; document order; no idempotency registry |
| [`chart-inline-deferred.md`](./chart-inline-deferred.md) | **Locked (Gap 26):** inline chart UX deferred; WI-370 = `general` chat only |
| [`chart-greenfield-implementation.md`](./chart-greenfield-implementation.md) | **Locked (Gap 27):** greenfield `components/charts/`; grinder ChartView inspirational only |

## Related work items

| WI | Uses these specs for |
|----|----------------------|
| WI-366 | Contract cross-links; appendix examples (**done**) |
| WI-368 | Catalog entries + validator rules |
| WI-370 | `compileChartSpecToECharts`, `ChartRenderer`, Vitest compiler vectors |
