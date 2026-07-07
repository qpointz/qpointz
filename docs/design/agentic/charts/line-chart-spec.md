# Line chart specification

**Status:** Locked design input design input for WI-366
**`chartType`:** `line`
**ECharts:** `series.type: 'line'`

## 1. Analytical purpose

Show how a **numeric measure** (`y`) changes across an **ordered dimension** (`x`) — time series,
date progression, or ordered categories.

**Use when:** trends, sequences, monitoring over time.

**Do not use when:** pure categorical comparison without order meaning (consider `bar`) or
part-to-whole (consider `pie`).

## 2. Semantic configuration

### 2.1 Encodings

| Role | Required | Maps to | Mill logical types |
|------|----------|---------|-------------------|
| `x` | yes | Horizontal axis (ordered dimension) | `STRING`, `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ`, `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT` |
| `y` | yes | Vertical measure | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |
| `series` | no | Multiple lines | `STRING`, `BOOL`, `UUID` |

### 2.2 Options

No MVP options. Catalog reserves future: `interpolate`, `showPoints`.

### 2.3 Snapshot limits

| | Value |
|---|-------|
| `defaultLimit` | 500 |
| `hardLimit` | 5000 |

## 3. Example artifact

```json
{
  "artifactType": "generated-chart",
  "title": "Monthly revenue",
  "sql": "SELECT month, SUM(revenue) AS revenue FROM sales GROUP BY month ORDER BY month",
  "columns": [
    { "name": "month", "type": "STRING" },
    { "name": "revenue", "type": "DOUBLE" }
  ],
  "charts": [
    {
      "chartKey": "default",
      "title": "Monthly revenue",
      "chartType": "line",
      "encodings": {
        "x": { "field": "month", "label": "Month" },
        "y": { "field": "revenue", "label": "Revenue" }
      },
      "presentation": {
        "sort": [{ "field": "month", "direction": "asc" }],
        "legend": { "visible": false },
        "labels": { "visible": false }
      }
    }
  ]
}
```

### 3.1 Multi-line example

`charts[]` entry:

```json
{
  "chartKey": "revenue-by-region",
  "chartType": "line",
  "encodings": {
    "x": { "field": "date", "label": "Date" },
    "y": { "field": "amount", "label": "Amount" },
    "series": { "field": "region", "label": "Region" }
  }
}
```

## 4. Sample data

```json
[
  { "month": "2025-01", "revenue": 12000 },
  { "month": "2025-02", "revenue": 14500 },
  { "month": "2025-03", "revenue": 13800 }
]
```

## 5. Validation rules

| Rule | Error code |
|------|------------|
| `x` and `y` required | `missing_encoding` |
| Fields exist in `columns` | `unknown_field` |
| `y` numeric-compatible | `incompatible_type` |
| Reject `category`, `value` roles | `invalid_encoding_role` |
| Recommend `presentation.sort` on `x` field ascending for time series (warning if missing) | `warning_unsorted_x` |

## 6. ECharts compilation mapping

### 6.1 Semantic → ECharts

| Semantic | ECharts path | Notes |
|----------|-------------|-------|
| `rows` | `dataset[0].source` | |
| `x.field` | `encode.x` | |
| `y.field` | `encode.y` | |
| `x.label` | `xAxis.name` | |
| `y.label` | `yAxis.name` | |
| `x` column temporal type | `xAxis.type: 'time'` | See §6.2 |
| `x` column string/number ordinal | `xAxis.type: 'category'` | Preserve row order after sort |
| `y` column | `yAxis.type: 'value'` | |
| `series` | multiple `series[]` entries | One per distinct series value (same pattern as bar) |
| `presentation.legend.visible` | `legend.show` | Default `true` when multiple series |
| `presentation.labels.visible` | `series[].label.show` | |

### 6.2 X axis type resolution

| `columns[].type` for `x.field` | `xAxis.type` |
|-------------------------------|--------------|
| `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ` | `time` |
| `STRING`, `BOOL`, `UUID` | `category` |
| Integer types | `category` (ordinal labels) or `value` if truly continuous — **MVP: `category`** with row order from sort |

### 6.3 Series without stacking

Line MVP does **not** stack. Multiple `series` → overlaid lines, each:

```json
{
  "type": "line",
  "name": "EMEA",
  "encode": { "x": "date", "y": "amount" },
  "smooth": false
}
```

`smooth` is not exposed in semantic options for MVP (compiler default `false`).

## 7. Compiled option example

```json
{
  "title": { "text": "Monthly revenue" },
  "tooltip": { "trigger": "axis" },
  "legend": { "show": false },
  "dataset": [{
    "source": [
      { "month": "2025-01", "revenue": 12000 },
      { "month": "2025-02", "revenue": 14500 },
      { "month": "2025-03", "revenue": 13800 }
    ]
  }],
  "xAxis": { "type": "category", "name": "Month", "boundaryGap": false },
  "yAxis": { "type": "value", "name": "Revenue" },
  "series": [{
    "type": "line",
    "datasetIndex": 0,
    "encode": { "x": "month", "y": "revenue" },
    "label": { "show": false }
  }]
}
```

**ECharts note:** `boundaryGap: false` on category x-axis connects line to axis ticks (common for
time-like categories).

## 8. Time axis example

When `month` column type is `DATE`:

```json
{
  "xAxis": { "type": "time", "name": "Month" },
  "series": [{
    "type": "line",
    "encode": { "x": "month", "y": "revenue" }
  }]
}
```

Compiler normalizes date cell values to timestamps ECharts accepts.

## 9. Compiler test vector

```typescript
expect(option.series[0].type).toBe('line');
expect(option.series[0].encode).toEqual({ x: 'month', y: 'revenue' });
expect(option.xAxis.boundaryGap).toBe(false);
```

## 10. Open questions for review

- [ ] Integer `x` as `value` axis for true scatter-like line — out of scope; use `scatter` instead.
- [ ] `smooth: true` as future `options.smooth`?
