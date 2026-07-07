# Forward-compatible chart families (verification)

**Status:** Non-normative design appendix for protocol compatibility (Gap 13)
**MVP:** These chart types are **not** implemented, catalog-advertised, or UI-rendered in the initial story.
**Parent contract:** [`../chart-artifact-contract.md`](../chart-artifact-contract.md) §6.7

## Verification conclusion

The current `generated-chart` artifact shape **does not need to change** to represent treemap,
sunburst, histogram, heatmap, or bubble later. The MVP payload fields (`chartType`, `columns`,
`encodings`, `options`, `presentation`) plus reserved roles (`hierarchy`, `bin`, `size`, `facet`,
`color`) are sufficient.

| Check | Result |
|-------|--------|
| Treemap / sunburst via ordered `hierarchy` + `value` | **Pass** — see §2 |
| Histogram via `bin` + `value` + bin `options` | **Pass** — see §3 |
| Heatmap via `x` + `y` + `value` (dense grid) | **Pass** — see §4 |
| Bubble via scatter roles + `size` | **Pass** — see §5 |
| ECharts compilation path exists (future) | **Pass** — standard `series.type` values |
| MVP validators reject these `chartType` values | **Required** — `unsupported_chart_type` until catalog + renderer ship |

Until a future chart type is added to `list_supported_charts` and `compileChartSpecToECharts`,
`validate_chart_spec` must return `passed: false` with `code: unsupported_chart_type` (user sees text
only per §10).

---

## 1. Reserved encoding roles (recap)

| Role | Structure (future) | Chart families |
|------|-------------------|----------------|
| `hierarchy` | Ordered array of `{ field, label? }` — root → leaf | `treemap`, `sunburst` |
| `bin` | `{ field, label? }` — source column to bin | `histogram` |
| `value` | `{ field, label? }` — numeric measure | treemap, sunburst, histogram, heatmap |
| `x`, `y` | `{ field, label? }` | `heatmap` (and MVP cartesian types) |
| `size` | `{ field, label? }` | `bubble` |
| `color` | `{ field, label? }` | `heatmap`, `bubble` (optional) |
| `facet` | `{ field, label? }` | small multiples (future) |

**Hierarchy encoding shape (locked for future use):**

```json
"hierarchy": [
  { "field": "region", "label": "Region" },
  { "field": "country", "label": "Country" },
  { "field": "city", "label": "City" }
]
```

- Order is significant: index `0` = root level, last index = leaf level before `value`.
- Each `field` must exist in `columns` and be categorical-compatible.
- `value` is a sibling encoding (not inside `hierarchy`).

**Bin encoding shape (locked for future use):**

```json
"bin": { "field": "unit_price", "label": "Unit price" },
"value": { "field": "row_count", "label": "Count" }
```

- `bin.field` must be numeric- or temporal-compatible.
- Histogram may use **client-side binning** (compiler bins snapshot rows) or **SQL-pre-binned**
  result (`bin` column + `value` column already aggregated).

---

## 2. Hierarchical part-to-whole — treemap and sunburst

### 2.1 Analytical purpose

Show composition across **nested categories** (e.g. region → country → product) with a numeric
**value** at the leaf grain (or aggregated per path).

### 2.2 SQL result shapes (two supported patterns)

**Pattern A — Multi-level columns (preferred for flat SQL result):**

```sql
SELECT region, country, SUM(revenue) AS revenue
FROM sales
GROUP BY region, country
```

| region | country | revenue |
|--------|---------|---------|
| EMEA | DE | 100 |
| EMEA | FR | 80 |
| APAC | JP | 120 |

**Pattern B — Leaf path already unique:** deeper hierarchies add more categorical columns before
`value`.

### 2.3 Example artifact — treemap (non-normative)

```json
{
  "artifactType": "generated-chart",
  "title": "Revenue by region and country",
  "chartType": "treemap",
  "sql": "SELECT region, country, SUM(revenue) AS revenue FROM sales GROUP BY region, country",
  "columns": [
    { "name": "region", "type": "STRING" },
    { "name": "country", "type": "STRING" },
    { "name": "revenue", "type": "DOUBLE" }
  ],
  "encodings": {
    "hierarchy": [
      { "field": "region", "label": "Region" },
      { "field": "country", "label": "Country" }
    ],
    "value": { "field": "revenue", "label": "Revenue" }
  },
  "options": {},
  "presentation": {
    "labels": { "visible": true }
  }
}
```

### 2.4 Example artifact — sunburst (non-normative)

Same encodings as treemap; only `chartType` changes:

```json
{
  "chartType": "sunburst",
  "encodings": {
    "hierarchy": [
      { "field": "region", "label": "Region" },
      { "field": "country", "label": "Country" }
    ],
    "value": { "field": "revenue", "label": "Revenue" }
  }
}
```

Sunburst and treemap share one **hierarchy + value** contract; the catalog distinguishes layout
options later (e.g. `options.roam`, `options.leafDepth`).

### 2.5 Future catalog sketch

```yaml
treemap:
  requiredEncodings:
    hierarchy:
      minLevels: 1
      maxLevels: 8
      levelRole: ordered_field_array
    value:
      dataTypes: [TINY_INT, SMALL_INT, INT, BIG_INT, FLOAT, DOUBLE]
      cardinality: one
  snapshot:
    defaultLimit: 500
    hardLimit: 2000

sunburst:
  # same encoding contract as treemap
```

### 2.6 ECharts compatibility (future compiler)

| Semantic | ECharts |
|----------|---------|
| `chartType: treemap` | `series[0].type: 'treemap'` |
| `chartType: sunburst` | `series[0].type: 'sunburst'` |
| `hierarchy[]` + rows | Compiler builds nested `children` tree from flat rows, or uses `series.data` paths |
| `value.field` | Node `value` in tree data |

ECharts treemap/sunburst expect hierarchical `{ name, value, children? }` data. The compiler
**derives** that tree from flat `dataset.source` + `hierarchy` field order — no tree in the artifact.

**Example compiled tree node (conceptual):**

```json
{
  "name": "EMEA",
  "value": 180,
  "children": [
    { "name": "DE", "value": 100 },
    { "name": "FR", "value": 80 }
  ]
}
```

---

## 3. Binned distributions — histogram

### 3.1 Analytical purpose

Show **frequency distribution** of a numeric or temporal column using bins.

### 3.2 SQL result shapes

**Pattern A — Raw values (client bins at compile/render):**

```sql
SELECT unit_price FROM order_lines
```

Encodings: `bin` → `unit_price`, `value` implicit as row count per bin (compiler aggregates).

**Pattern B — Pre-binned SQL (preferred when SQL already aggregates):**

```sql
SELECT width_bucket(unit_price, 0, 100, 10) AS price_bin, COUNT(*) AS cnt
FROM order_lines
GROUP BY 1
```

Encodings: `bin` → `price_bin`, `value` → `cnt`.

### 3.3 Example artifact — histogram (non-normative)

Pre-binned pattern:

```json
{
  "artifactType": "generated-chart",
  "title": "Order quantity distribution",
  "chartType": "histogram",
  "sql": "SELECT width_bucket(order_qty, 0, 50, 10) AS qty_bin, COUNT(*) AS cnt FROM orders GROUP BY 1",
  "columns": [
    { "name": "qty_bin", "type": "INT" },
    { "name": "cnt", "type": "BIG_INT" }
  ],
  "encodings": {
    "bin": { "field": "qty_bin", "label": "Quantity bin" },
    "value": { "field": "cnt", "label": "Orders" }
  },
  "options": {
    "binCount": 10,
    "binAlign": "center"
  },
  "presentation": {
    "labels": { "visible": false }
  }
}
```

Raw-value pattern (compiler bins snapshot):

```json
{
  "chartType": "histogram",
  "encodings": {
    "bin": { "field": "order_qty", "label": "Quantity" },
    "value": { "field": "order_qty", "label": "Count" }
  },
  "options": {
    "binCount": 20,
    "binWidth": null
  }
}
```

When SQL returns raw rows, `value` may duplicate `bin.field` semantically — compiler counts rows
per bin; validator allows this with catalog flag `valueFromBinCount: true`.

### 3.4 Future catalog sketch

```yaml
histogram:
  requiredEncodings:
    bin:
      dataTypes: [TINY_INT, SMALL_INT, INT, BIG_INT, FLOAT, DOUBLE, DATE, TIMESTAMP]
      cardinality: one
    value:
      dataTypes: [TINY_INT, SMALL_INT, INT, BIG_INT, FLOAT, DOUBLE]
      cardinality: one
  options:
    binCount:
      type: integer
      default: 20
    binWidth:
      type: number
      default: null
    binAlign:
      type: string
      enum: [left, center, right]
      default: center
  modes:
    - pre_binned_sql
    - client_bin_raw_snapshot
```

### 3.5 ECharts compatibility (future compiler)

ECharts has no `histogram` series type. Standard approach:

| Semantic | ECharts |
|----------|---------|
| `chartType: histogram` | `series[0].type: 'bar'` |
| Pre-binned | `encode: { x: '<bin.field>', y: '<value.field>' }`, `xAxis.type: 'category'` or `'value'` |
| Client-side binning | Compiler aggregates rows into bin edges, then same bar mapping |
| `options.binCount` / `binWidth` | Used only in client-bin mode |

Histogram is **semantically distinct** (`chartType: histogram`) but compiles to **bar** series —
same pattern as area compiling to `line` + `areaStyle`.

---

## 4. Stress case — heatmap

Dense two-dimensional grid with cell intensity.

### 4.1 Example artifact (non-normative)

```json
{
  "chartType": "heatmap",
  "encodings": {
    "x": { "field": "day_of_week", "label": "Day" },
    "y": { "field": "hour", "label": "Hour" },
    "value": { "field": "order_count", "label": "Orders" }
  },
  "options": {
    "cellGap": 1
  }
}
```

### 4.2 ECharts compatibility

| Semantic | ECharts |
|----------|---------|
| `series.type` | `'heatmap'` |
| `x`, `y`, `value` | `encode: { x, y, value }` on `dataset` or `visualMap` for intensity |

**Verification:** Existing cartesian roles plus `value` suffice; no new encoding role required.

---

## 5. Stress case — bubble

Scatter with sized points.

### 5.1 Example artifact (non-normative)

```json
{
  "chartType": "bubble",
  "encodings": {
    "x": { "field": "gdp", "label": "GDP" },
    "y": { "field": "life_expectancy", "label": "Life expectancy" },
    "size": { "field": "population", "label": "Population" },
    "series": { "field": "continent", "label": "Continent" }
  }
}
```

### 5.2 ECharts compatibility

| Semantic | ECharts |
|----------|---------|
| Base | `series.type: 'scatter'` |
| `size.field` | `symbolSize` callback from normalized `size` column |
| `series` | Multiple series or `itemStyle` by group |

**Verification:** Reserved `size` role from §6.7 is sufficient; extends scatter compiler.

---

## 6. Protocol compatibility matrix

| Future `chartType` | New roles needed? | New top-level fields? | MVP artifact change? |
|--------------------|-------------------|----------------------|---------------------|
| `treemap` | `hierarchy` (array) | no | **no** |
| `sunburst` | same as treemap | no | **no** |
| `histogram` | `bin` | no | **no** |
| `heatmap` | `x`, `y`, `value` (existing) | no | **no** |
| `bubble` | `size` (+ optional `series`) | no | **no** |

---

## 7. Validator behavior until implemented

| Request | Result |
|---------|--------|
| `chartType: treemap` in MVP | `unsupported_chart_type` — text reply only |
| `hierarchy` encoding on `bar` | `invalid_encoding_role` |
| `bin` on `line` | `invalid_encoding_role` |

Future WIs add catalog entries, validator branches, and compiler modules per
[`README.md`](./README.md) extension process.

---

## 8. Related documents

| Doc | Role |
|-----|------|
| [`../chart-artifact-contract.md`](../chart-artifact-contract.md) §6.7 | Reserved roles |
| [`echarts-compiler-contract.md`](./echarts-compiler-contract.md) | Shared compile pipeline |
| MVP specs (`bar-chart-spec.md`, …) | Shipped chart types |
