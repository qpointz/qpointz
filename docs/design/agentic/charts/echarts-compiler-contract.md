# ECharts compiler contract (shared)

**Status:** Draft
**Applies to:** all MVP chart types in [`README.md`](./README.md)

## Compiler inputs and outputs

```text
compileChartSpecToECharts(
  spec: GeneratedChartArtifact,   // semantic generated-chart payload
  rows: Record<string, unknown>[], // bounded snapshot from query-service full mode
  theme?: ChartThemeTokens         // Mantine-derived colors, fonts (WI-370)
) => EChartsOption                  // passed to echarts-for-react; never persisted
```

| Input | Source |
|-------|--------|
| `spec` | Chat artifact / GET replay |
| `rows` | `fetchChartSnapshot` — JSON objects keyed by column name |
| `theme` | `chartTheme.ts` — grid, axis, palette |

## Pipeline (normative order)

1. **Validate compiler support** — `spec.chartType` must have a registered compiler; otherwise
   return unsupported-renderer state (no throw in UI).
2. **Apply `presentation.sort`** — sort `rows` in memory (stable sort); at most 3 keys; see
   [`../chart-artifact-contract.md`](../chart-artifact-contract.md) §9.
3. **Resolve effective options** — merge `spec.options` with catalog defaults for `chartType`.
4. **Build `dataset.source`** — use sorted `rows` as-is (array of objects). Column names must match
   `spec.encodings.*.field` and `spec.columns[].name`.
5. **Dispatch by `chartType`** — chart-specific compiler module (`compileBarChart`, …).
6. **Apply shared chrome** — `title`, `tooltip`, `legend`, `grid`, theme colors.
7. **Apply `presentation.labels`** — `series[].label.show` where the chart type supports labels.

## ECharts compatibility principles

Mill targets **ECharts 6.x** `option` objects compatible with `echarts-for-react`.

| Principle | Detail |
|-----------|--------|
| **Prefer `dataset` + `encode`** | Row objects from SQL map directly to `dataset.source`. Avoid hand-built `series.data` arrays unless required (pie is the main exception). |
| **Axis typing from Mill logical types** | `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ` → `xAxis.type: 'time'` (category charts use category axis on the dimension axis). Numeric columns → `'value'`. String/bool/uuid categories → `'category'`. |
| **Single `dataset` index** | MVP uses `dataset: [{ source: rows }]` and `series[].datasetIndex: 0`. |
| **`encode` uses field names** | ECharts 5+ supports encoding by dimension name when `dataset.source` is object rows. |
| **Stacking** | When `options.stacked === true` and multiple series exist, set `series[].stack: 'total'`. |
| **Legend** | `legend.show` from `presentation.legend.visible` (default `true` when `series` has multiple names). |
| **Tooltip** | `tooltip.trigger: 'axis'` for bar/line/area; `'item'` for pie/scatter. |
| **No renderer config in artifact** | Keys like `xAxis`, `series`, `dataset` appear only in compiler output. |

## Shared ECharts option skeleton

All Cartesian charts (bar, line, area, scatter) start from:

```json
{
  "title": { "text": "<spec.title>", "show": true },
  "tooltip": { "trigger": "axis" },
  "legend": { "show": true },
  "grid": { "left": "8%", "right": "4%", "bottom": "12%", "containLabel": true },
  "dataset": [{ "source": [] }],
  "xAxis": {},
  "yAxis": {},
  "series": []
}
```

Pie charts omit `xAxis` / `yAxis` and use a single `series[0].type: 'pie'`.

## Axis label resolution

| Source | ECharts target |
|--------|----------------|
| `encodings.<role>.label` | `xAxis.name`, `yAxis.name`, or `legend` via series `name` |
| `encodings.<role>.field` | `encode.x`, `encode.y`, `encode.itemName`, `encode.value` |
| Missing label | Axis name omitted; ECharts uses field name in tooltip |

## Time axis normalization

When the bound encoding field has Mill type `DATE`, `TIME`, `TIMESTAMP`, or `TIMESTAMP_TZ`:

- Set the corresponding axis `type: 'time'`.
- Compiler converts cell values to JavaScript `Date` or ISO strings ECharts accepts.
- If parsing fails, fall back to `category` and emit a console warning (UI only).

## Truncation and empty data

| Condition | Compiler / UI behavior |
|-----------|------------------------|
| `rows.length === 0` | Return empty-state option (title + “No data”) or let `ChartRenderer` show empty state without mounting ECharts. |
| Snapshot `truncated === true` | UI shows chart-specific banner before render; compiler may still render partial data with `graphic` warning text. |
| Row count > catalog `hardLimit` | Snapshot fetch should fail before compile; compiler does not slice silently. |

## Presentation mapping

| `presentation` field | ECharts mapping |
|---------------------|-----------------|
| `sort[]` | Pre-process rows (not an ECharts option). |
| `legend.visible` | `legend.show` |
| `labels.visible` | `series[].label.show` (bar, line, area, pie); scatter uses `label.show: false` by default |

## Compiler registration (WI-370)

```typescript
type ChartCompiler = (
  spec: GeneratedChartArtifact,
  rows: Record<string, unknown>[],
  theme: ChartThemeTokens
) => EChartsOption;

const compilers: Record<ChartType, ChartCompiler> = {
  bar: compileBarChart,
  line: compileLineChart,
  area: compileAreaChart,
  scatter: compileScatterChart,
  pie: compilePieChart,
};
```

## Test vectors

Each chart spec file includes at least one **compiler test vector**:

- `artifact` — semantic input
- `rows` — snapshot input
- `expectedOption` — golden subset (key paths asserted in Vitest, not full deep equal of theme colors)
