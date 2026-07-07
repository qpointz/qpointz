# Area chart specification

**Status:** Locked design input design input for WI-366
**`chartType`:** `area`
**ECharts:** `series.type: 'line'` with `areaStyle: {}`

## 1. Analytical purpose

Same as **line chart** (measure over ordered `x`) with **filled area** under the line. Supports
**stacked areas** when `series` and `options.stacked` are set.

**Use when:** emphasizing volume under a trend, stacked composition over time.

**Do not use when:** simple trend line without fill (use `line`) or unrelated `(x,y)` pairs (use
`scatter`).

## 2. Semantic configuration

### 2.1 Encodings

Identical to [`line-chart-spec.md`](./line-chart-spec.md) §2.1:

| Role | Required |
|------|----------|
| `x` | yes |
| `y` | yes |
| `series` | no |

### 2.2 Options

| Option | Type | Default | Meaning |
|--------|------|---------|---------|
| `stacked` | boolean | `false` | Stack areas on the value axis when multiple series |

### 2.3 Snapshot limits

Same as line: `defaultLimit` 500, `hardLimit` 5000.

## 3. Example artifact

```json
{
  "artifactType": "generated-chart",
  "title": "Revenue by region over time",
  "columns": [
    { "name": "month", "type": "STRING" },
    { "name": "revenue", "type": "DOUBLE" },
    { "name": "region", "type": "STRING" }
  ],
  "charts": [
    {
      "chartKey": "default",
      "title": "Revenue by region over time",
      "chartType": "area",
      "encodings": {
        "x": { "field": "month", "label": "Month" },
        "y": { "field": "revenue", "label": "Revenue" },
        "series": { "field": "region", "label": "Region" }
      },
      "options": {
        "stacked": true
      },
      "presentation": {
        "sort": [{ "field": "month", "direction": "asc" }],
        "legend": { "visible": true },
        "labels": { "visible": false }
      }
    }
  ]
}
```

## 4. Sample data

```json
[
  { "month": "2025-01", "region": "EMEA", "revenue": 4000 },
  { "month": "2025-01", "region": "APAC", "revenue": 3000 },
  { "month": "2025-02", "region": "EMEA", "revenue": 4200 },
  { "month": "2025-02", "region": "APAC", "revenue": 3100 }
]
```

## 5. Validation rules

Same as line, plus:

| Rule | Error code |
|------|------------|
| `stacked: true` without `series` | `warning_stack_without_series` (warning only) |

## 6. ECharts compilation mapping

### 6.1 Difference from line

Area compilation **extends** line compilation:

| Addition | ECharts path |
|----------|-------------|
| Chart type | `series[].type: 'line'` (unchanged — ECharts has no separate `area` type) |
| Fill | `series[].areaStyle: {}` — empty object enables default theme fill |
| Stack | `series[].stack: 'total'` when `options.stacked === true` |

### 6.2 Semantic → ECharts (delta from line)

| Semantic | ECharts |
|----------|---------|
| `chartType: area` | `areaStyle: {}` on every series |
| `options.stacked` | `stack: 'total'` on every series when true |

All axis, encode, dataset, and series-splitting rules match [`line-chart-spec.md`](./line-chart-spec.md).

### 6.3 Stacked area series example

```json
{
  "series": [
    {
      "type": "line",
      "name": "EMEA",
      "encode": { "x": "month", "y": "revenue" },
      "areaStyle": {},
      "stack": "total"
    },
    {
      "type": "line",
      "name": "APAC",
      "encode": { "x": "month", "y": "revenue" },
      "areaStyle": {},
      "stack": "total"
    }
  ]
}
```

**ECharts compatibility:** stacked area is the standard `line` + `areaStyle` + `stack` pattern
documented in ECharts official examples.

## 7. Compiled option example (single series)

```json
{
  "title": { "text": "Monthly revenue" },
  "tooltip": { "trigger": "axis" },
  "dataset": [{
    "source": [
      { "month": "2025-01", "revenue": 12000 },
      { "month": "2025-02", "revenue": 14500 }
    ]
  }],
  "xAxis": { "type": "category", "name": "Month", "boundaryGap": false },
  "yAxis": { "type": "value", "name": "Revenue" },
  "series": [{
    "type": "line",
    "encode": { "x": "month", "y": "revenue" },
    "areaStyle": {},
    "label": { "show": false }
  }]
}
```

## 8. Compiler test vector

```typescript
expect(option.series[0].type).toBe('line');
expect(option.series[0].areaStyle).toBeDefined();
expect(option.series[0].stack).toBe('total'); // when stacked + multi-series
```

## 9. Open questions for review

- [ ] Opacity / gradient in `areaStyle` — theme concern only for MVP?
- [ ] Overlaid (non-stacked) multi-series area readability — legend required?
