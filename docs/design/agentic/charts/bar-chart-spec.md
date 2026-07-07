# Bar chart specification

**Status:** Locked design input design input for WI-366
**`chartType`:** `bar`
**ECharts:** `series.type: 'bar'`

## 1. Analytical purpose

Compare a **numeric measure** across **categories**. Supports grouped and stacked bars when a
**series** dimension is present.

**Use when:** comparing counts, sums, or averages per category (e.g. revenue by country, orders by
month).

**Do not use when:** showing trends over continuous time (prefer `line` / `area`) or part-to-whole
with few slices (prefer `pie`).

## 2. Semantic configuration

### 2.1 Encodings

| Role | Required | Maps to | Mill logical types |
|------|----------|---------|-------------------|
| `category` | yes | Category axis (discrete dimension) | `STRING`, `BOOL`, `UUID`, `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ` |
| `value` | yes | Value axis (measure) | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |
| `series` | no | Series grouping (grouped/stacked bars) | `STRING`, `BOOL`, `UUID` |

### 2.2 Options

| Option | Type | Default | Meaning |
|--------|------|---------|---------|
| `orientation` | `"vertical"` \| `"horizontal"` | `"vertical"` | Bar growth direction |
| `stacked` | boolean | `false` | Stack series on the value axis |

### 2.3 Snapshot limits

| | Value |
|---|-------|
| `defaultLimit` | 500 |
| `hardLimit` | 5000 |

## 3. Example artifact

Full artifact (query-level fields + one `charts[]` entry). See
[`multi-chart-artifact-model.md`](./multi-chart-artifact-model.md).

```json
{
  "artifactType": "generated-chart",
  "title": "Clients by country",
  "description": "Client count grouped by country.",
  "sql": "SELECT country, COUNT(*) AS client_count FROM clients GROUP BY country",
  "dialectId": "CALCITE",
  "statementKind": "select",
  "source": "generated",
  "validationWarnings": [],
  "columns": [
    { "name": "country", "type": "STRING" },
    { "name": "client_count", "type": "BIG_INT" }
  ],
  "charts": [
    {
      "chartKey": "default",
      "title": "Clients by country",
      "chartType": "bar",
      "encodings": {
        "category": { "field": "country", "label": "Country" },
        "value": { "field": "client_count", "label": "Clients" }
      },
      "options": {
        "orientation": "vertical",
        "stacked": false
      },
      "presentation": {
        "sort": [{ "field": "client_count", "direction": "desc" }],
        "legend": { "visible": false },
        "labels": { "visible": true }
      }
    }
  ]
}
```

### 3.1 Grouped bar example (with `series`)

`charts[]` entry only:

```json
{
  "chartKey": "revenue-by-product",
  "chartType": "bar",
  "encodings": {
    "category": { "field": "month", "label": "Month" },
    "value": { "field": "revenue", "label": "Revenue" },
    "series": { "field": "product", "label": "Product" }
  },
  "options": { "orientation": "vertical", "stacked": false }
}
```

## 4. Sample data

**Simple bar** (after sort):

```json
[
  { "country": "DE", "client_count": 120 },
  { "country": "US", "client_count": 95 },
  { "country": "FR", "client_count": 80 }
]
```

**Grouped bar:**

```json
[
  { "month": "2025-01", "product": "A", "revenue": 1000 },
  { "month": "2025-01", "product": "B", "revenue": 800 },
  { "month": "2025-02", "product": "A", "revenue": 1100 }
]
```

## 5. Validation rules

| Rule | Error code (suggested) |
|------|------------------------|
| `category` and `value` required | `missing_encoding` |
| `category.field` and `value.field` exist in `columns` | `unknown_field` |
| `value` column is numeric-compatible | `incompatible_type` |
| Reject `x`, `y`, `hierarchy`, `bin` encodings | `invalid_encoding_role` |
| If `series` present, column must be categorical | `incompatible_type` |
| `stacked: true` without `series` → warning only (single series stack is no-op) | `warning_stack_without_series` |
| Row count after snapshot ≤ `hardLimit` | enforced at fetch, not validator |

## 6. ECharts compilation mapping

### 6.1 Semantic → ECharts

| Semantic | ECharts `option` path | Notes |
|----------|----------------------|-------|
| `rows` | `dataset[0].source` | Array of row objects |
| `category.field` | `encode` category axis dimension | See orientation table below |
| `value.field` | `encode` value axis dimension | |
| `category.label` | category axis `name` | vertical: `xAxis.name`; horizontal: `yAxis.name` |
| `value.label` | value axis `name` | vertical: `yAxis.name`; horizontal: `xAxis.name` |
| `options.orientation` | axis `type` swap | See §6.2 |
| `options.stacked` | `series[].stack: 'total'` | When true and multiple series |
| `presentation.legend.visible` | `legend.show` | |
| `presentation.labels.visible` | `series[].label.show` | |
| `title` | `title.text` | |

### 6.2 Orientation and `encode`

**Vertical** (`orientation: vertical`, default):

```json
{
  "xAxis": { "type": "category", "name": "Country" },
  "yAxis": { "type": "value", "name": "Clients" },
  "series": [{
    "type": "bar",
    "encode": {
      "x": "country",
      "y": "client_count"
    }
  }]
}
```

**Horizontal** (`orientation: horizontal`):

```json
{
  "xAxis": { "type": "value", "name": "Clients" },
  "yAxis": { "type": "category", "name": "Country" },
  "series": [{
    "type": "bar",
    "encode": {
      "x": "client_count",
      "y": "country"
    }
  }]
}
```

### 6.3 Series / grouped / stacked bars

When `encodings.series` is set, the compiler:

1. Collects distinct values of `series.field` → one ECharts `series` item per value.
2. For each series name `S`, filters rows where `series.field === S` (or uses
   `dataset.transform` + `filter` — implementation choice; behavior must match).
3. Each series item:

```json
{
  "type": "bar",
  "name": "Product A",
  "encode": { "x": "month", "y": "revenue" },
  "stack": "total"
}
```

`stack: "total"` is set on all series when `options.stacked === true`; omitted when `false`.

**ECharts compatibility note:** grouped bars require **one series per group value**; Mill compiler
owns the pivot/filter step so the artifact stays in long-format SQL result shape.

### 6.4 Time category axis

If `category` column type is `DATE` / `TIMESTAMP` / etc., use `xAxis.type: 'time'` (vertical) or
`yAxis.type: 'time'` (horizontal) instead of `category`.

## 7. Compiled option example (simple vertical bar)

Input: §3 artifact + §4 simple rows.

```json
{
  "title": { "text": "Clients by country" },
  "tooltip": { "trigger": "axis", "axisPointer": { "type": "shadow" } },
  "legend": { "show": false },
  "grid": { "left": "8%", "right": "4%", "bottom": "12%", "containLabel": true },
  "dataset": [{
    "source": [
      { "country": "DE", "client_count": 120 },
      { "country": "US", "client_count": 95 },
      { "country": "FR", "client_count": 80 }
    ]
  }],
  "xAxis": { "type": "category", "name": "Country" },
  "yAxis": { "type": "value", "name": "Clients" },
  "series": [{
    "type": "bar",
    "datasetIndex": 0,
    "encode": { "x": "country", "y": "client_count" },
    "label": { "show": true }
  }]
}
```

## 8. Compiler test vector (Vitest)

```typescript
// assert subset paths
expect(option.series[0].type).toBe('bar');
expect(option.series[0].encode).toEqual({ x: 'country', y: 'client_count' });
expect(option.dataset[0].source).toHaveLength(3);
expect(option.xAxis.type).toBe('category');
```

## 9. Open questions for review

- [ ] Confirm grouped-bar filter-vs-pivot approach for ECharts 6 `dataset.transform`.
- [ ] Maximum distinct `series` values before legend clutter warning (suggested: 12).
