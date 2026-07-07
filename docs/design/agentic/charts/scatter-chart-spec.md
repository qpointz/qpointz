# Scatter chart specification

**Status:** Locked design input design input for WI-366
**`chartType`:** `scatter`
**ECharts:** `series.type: 'scatter'`

## 1. Analytical purpose

Plot **numeric x** against **numeric y** to show correlation, distribution, or clusters.

**Use when:** both dimensions are measures (height vs weight, price vs quantity).

**Do not use when:** x is categorical (use `bar`) or x is time with a single trend (use `line`).

## 2. Semantic configuration

### 2.1 Encodings

| Role | Required | Maps to | Mill logical types |
|------|----------|---------|-------------------|
| `x` | yes | Horizontal numeric measure | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |
| `y` | yes | Vertical numeric measure | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |
| `series` | no | Point series grouping | `STRING`, `BOOL`, `UUID` |
| `color` | no | Categorical color grouping | `STRING`, `BOOL`, `UUID` |

**MVP rule:** use **`series` or `color`**, not both. If both provided, validator prefers `series` and
emits `warning_redundant_color_encoding`.

### 2.2 Options

No MVP options.

### 2.3 Snapshot limits

| | Value |
|---|-------|
| `defaultLimit` | 2000 |
| `hardLimit` | 10000 |

## 3. Example artifact

```json
{
  "artifactType": "generated-chart",
  "title": "Price vs quantity",
  "sql": "SELECT unit_price, order_qty FROM order_lines",
  "columns": [
    { "name": "unit_price", "type": "DOUBLE" },
    { "name": "order_qty", "type": "INT" }
  ],
  "charts": [
    {
      "chartKey": "default",
      "title": "Price vs quantity",
      "chartType": "scatter",
      "encodings": {
        "x": { "field": "unit_price", "label": "Unit price" },
        "y": { "field": "order_qty", "label": "Quantity" }
      },
      "presentation": {
        "legend": { "visible": false },
        "labels": { "visible": false }
      }
    }
  ]
}
```

### 3.1 With series grouping

`charts[]` entry:

```json
{
  "chartKey": "by-category",
  "chartType": "scatter",
  "encodings": {
    "x": { "field": "unit_price", "label": "Unit price" },
    "y": { "field": "order_qty", "label": "Quantity" },
    "series": { "field": "category", "label": "Category" }
  }
}
```

## 4. Sample data

```json
[
  { "unit_price": 9.99, "order_qty": 12 },
  { "unit_price": 14.5, "order_qty": 3 },
  { "unit_price": 7.25, "order_qty": 20 }
]
```

## 5. Validation rules

| Rule | Error code |
|------|------------|
| `x` and `y` required | `missing_encoding` |
| Both fields numeric-compatible | `incompatible_type` |
| Reject `category`, `value` | `invalid_encoding_role` |
| `series` and `color` both set | `warning_redundant_color_encoding` |

## 6. ECharts compilation mapping

### 6.1 Semantic → ECharts

| Semantic | ECharts path | Notes |
|----------|-------------|-------|
| `rows` | `dataset[0].source` | |
| `x.field` | `encode.x` | |
| `y.field` | `encode.y` | |
| `x.label` | `xAxis.name` | |
| `y.label` | `yAxis.name` | |
| Both axes | `type: 'value'` | Scatter MVP always value/value |
| `series` | multiple `series[]` with `type: 'scatter'` | Filter rows per series value |
| `color` | split series by color field (same as series pattern) | |
| `presentation.legend.visible` | `legend.show` | |
| `tooltip` | `trigger: 'item'` | Per-point tooltip |

### 6.2 Single-series scatter

```json
{
  "xAxis": { "type": "value", "name": "Unit price", "scale": true },
  "yAxis": { "type": "value", "name": "Quantity", "scale": true },
  "series": [{
    "type": "scatter",
    "datasetIndex": 0,
    "encode": { "x": "unit_price", "y": "order_qty" },
    "symbolSize": 8
  }]
}
```

**ECharts compatibility:** `scale: true` on value axes is recommended for scatter so zero is not
forced when data is far from origin.

### 6.3 Multi-series scatter

One `series` entry per distinct `series.field` value; each uses the same `encode` mapping on
filtered `dataset` or shared source with `transform` filter (same strategy as bar).

```json
{
  "series": [
    { "type": "scatter", "name": "Electronics", "encode": { "x": "unit_price", "y": "order_qty" } },
    { "type": "scatter", "name": "Grocery", "encode": { "x": "unit_price", "y": "order_qty" } }
  ]
}
```

### 6.4 Labels

MVP: `presentation.labels.visible` defaults to `false` for scatter (point labels clutter). When
`true`, set `label.show: true` and `label.position: 'top'`.

## 7. Compiled option example

```json
{
  "title": { "text": "Price vs quantity" },
  "tooltip": { "trigger": "item" },
  "legend": { "show": false },
  "dataset": [{
    "source": [
      { "unit_price": 9.99, "order_qty": 12 },
      { "unit_price": 14.5, "order_qty": 3 },
      { "unit_price": 7.25, "order_qty": 20 }
    ]
  }],
  "xAxis": { "type": "value", "name": "Unit price", "scale": true },
  "yAxis": { "type": "value", "name": "Quantity", "scale": true },
  "series": [{
    "type": "scatter",
    "datasetIndex": 0,
    "encode": { "x": "unit_price", "y": "order_qty" },
    "symbolSize": 8,
    "label": { "show": false }
  }]
}
```

## 8. Compiler test vector

```typescript
expect(option.series[0].type).toBe('scatter');
expect(option.xAxis.type).toBe('value');
expect(option.yAxis.scale).toBe(true);
expect(option.tooltip.trigger).toBe('item');
```

## 9. Open questions for review

- [ ] `symbolSize` from future `options.pointSize`?
- [ ] Large point counts (>2000) — sampling warning in UI?
