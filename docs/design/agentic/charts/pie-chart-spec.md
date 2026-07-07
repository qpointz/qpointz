# Pie chart specification

**Status:** Locked design input design input for WI-366
**`chartType`:** `pie`
**ECharts:** `series.type: 'pie'`

## 1. Analytical purpose

Show **part-to-whole** composition: one **category** (slice label) and one **value** (slice size).

**Use when:** share of total across few categories (≤12 slices recommended).

**Do not use when:** comparing many categories (use `bar`) or showing trends (use `line`).

## 2. Semantic configuration

### 2.1 Encodings

| Role | Required | Maps to | Mill logical types |
|------|----------|---------|-------------------|
| `category` | yes | Slice label (`name`) | `STRING`, `BOOL`, `UUID` |
| `value` | yes | Slice size | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |

No `series`, `x`, or `y` in MVP pie.

### 2.2 Options

No MVP options. Constraints live in catalog:

| Constraint | Value |
|------------|-------|
| `maxCategories` | 12 |
| `requiresPositiveValues` | true |

### 2.3 Snapshot limits

| | Value |
|---|-------|
| `defaultLimit` | 100 |
| `hardLimit` | 500 |

## 3. Example artifact

```json
{
  "artifactType": "generated-chart",
  "title": "Revenue share by region",
  "sql": "SELECT region, SUM(revenue) AS revenue FROM sales GROUP BY region",
  "columns": [
    { "name": "region", "type": "STRING" },
    { "name": "revenue", "type": "DOUBLE" }
  ],
  "charts": [
    {
      "chartKey": "default",
      "title": "Revenue share by region",
      "chartType": "pie",
      "encodings": {
        "category": { "field": "region", "label": "Region" },
        "value": { "field": "revenue", "label": "Revenue" }
      },
      "presentation": {
        "sort": [{ "field": "revenue", "direction": "desc" }],
        "legend": { "visible": true },
        "labels": { "visible": true }
      }
    }
  ]
}
```

## 4. Sample data

```json
[
  { "region": "EMEA", "revenue": 45000 },
  { "region": "APAC", "revenue": 32000 },
  { "region": "Americas", "revenue": 28000 }
]
```

## 5. Validation rules

| Rule | Error code |
|------|------------|
| `category` and `value` required | `missing_encoding` |
| Fields exist in `columns` | `unknown_field` |
| `value` numeric-compatible | `incompatible_type` |
| Distinct categories ≤ `maxCategories` (12) | `too_many_categories` |
| All values > 0 when `requiresPositiveValues` | `non_positive_value` |
| Sum of values = 0 | `zero_total` |
| Reject `x`, `y`, `series` | `invalid_encoding_role` |

## 6. ECharts compilation mapping

Pie is the main chart type where **`series.data` as `{ name, value }[]`** is acceptable because
ECharts pie does not use Cartesian `encode` as cleanly as bar/line. Mill still uses semantic
`category`/`value` in the artifact; the compiler **projects** rows to pie data.

### 6.1 Semantic → ECharts

| Semantic | ECharts path | Notes |
|----------|-------------|-------|
| `rows` | compiled to `series[0].data` | See §6.2 |
| `category.field` | `data[].name` | Stringified |
| `value.field` | `data[].value` | Coerced to number |
| `presentation.legend.visible` | `legend.show` | |
| `presentation.labels.visible` | `series[0].label.show` | |
| `title` | `title.text` | |
| Tooltip | `tooltip.trigger: 'item'` | |

### 6.2 Row projection algorithm

```text
for each row in sorted rows:
  name  = String(row[category.field])
  value = Number(row[value.field])
  if value is NaN → skip row + warning
  append { name, value } to series[0].data
```

Optional ECharts 5+ alternative (if preferred in implementation):

```json
{
  "dataset": { "source": [ ...rows ] },
  "series": [{
    "type": "pie",
    "encode": { "itemName": "region", "value": "revenue" }
  }]
}
```

**Draft decision:** MVP compiler **must support `encode.itemName` + `encode.value`** with
`dataset.source` (ECharts 6 compatible) so pie stays consistent with the shared dataset pipeline.
Use `series.data` only as fallback if encode proves unreliable in Vitest.

### 6.3 Recommended compiled option (dataset + encode)

```json
{
  "title": { "text": "Revenue share by region" },
  "tooltip": { "trigger": "item", "formatter": "{b}: {c} ({d}%)" },
  "legend": { "show": true, "orient": "vertical", "left": "left" },
  "dataset": [{
    "source": [
      { "region": "EMEA", "revenue": 45000 },
      { "region": "APAC", "revenue": 32000 },
      { "region": "Americas", "revenue": 28000 }
    ]
  }],
  "series": [{
    "type": "pie",
    "radius": "65%",
    "center": ["50%", "55%"],
    "datasetIndex": 0,
    "encode": {
      "itemName": "region",
      "value": "revenue"
    },
    "label": {
      "show": true,
      "formatter": "{b}: {d}%"
    },
    "emphasis": {
      "itemStyle": { "shadowBlur": 10, "shadowOffsetX": 0 }
    }
  }]
}
```

**ECharts compatibility notes:**

- `encode.itemName` / `encode.value` map directly from semantic `category` / `value` field names.
- `radius` / `center` are **compiler/theme defaults**, not artifact fields.
- Percent formatter `{d}%` is standard ECharts pie label pattern.

### 6.4 Negative and zero values

When `requiresPositiveValues` and any row has `value ≤ 0`:

- Validator rejects at mapping time (preferred).
- If only compiler sees bad data, filter slices with `value ≤ 0` and surface UI warning.

## 7. Compiler test vector

```typescript
expect(option.series[0].type).toBe('pie');
expect(option.series[0].encode).toEqual({ itemName: 'region', value: 'revenue' });
expect(option.tooltip.trigger).toBe('item');
expect(option.dataset[0].source).toHaveLength(3);
```

## 8. Open questions for review

- [ ] Donut (`radius: ['40%', '70%']`) — future `options.innerRadius`?
- [ ] "Other" bucket when categories > 12 — validator reject vs compiler merge?
