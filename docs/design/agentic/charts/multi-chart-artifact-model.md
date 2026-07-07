# Multi-chart artifact model

**Status:** Locked design input design input (Gap 14)
**Parent contract:** [`../chart-artifact-contract.md`](../chart-artifact-contract.md)

## 1. Core rule

Every `generated-chart` artifact is bound to **exactly one query and one result schema**:

| Shared at artifact root (once) | Per visualization |
|-------------------------------|-------------------|
| `sql`, `dialectId`, `statementKind`, `source`, `validationWarnings` | `charts[]` entries |
| `columns` — snapshot from `sql-query.describe_sql` for **that** SQL | `chartType`, `encodings`, `options`, `presentation` |
| Optional artifact-level `title` / `description` (query headline) | Optional per-chart `title` / `description` |

**One SQL → one chart artifact.** Multiple visualizations of the **same** result share one artifact
with **multiple chart configs** in `charts[]`.

**Multiple SQL queries → multiple chart artifacts** (one per query), each with its own `charts[]`
(musually one entry, but may be several views of that query's result).

```text
User: "bar and pie of revenue by region"
  → one generated-sql
  → one generated-chart { sql, columns, charts: [ { bar... }, { pie... } ] }

User: "plot revenue by month and client count by country"
  → generated-sql A + generated-chart A { charts: [ { line... } ] }
  → generated-sql B + generated-chart B { charts: [ { bar... } ] }
  → two chart artifacts on the same turn (multi-artifact turn)
```

Chart-mapping **never** combines two different SQL statements into one chart artifact.

---

## 2. Canonical payload shape

### 2.1 Multi-chart (same query)

```json
{
  "artifactType": "generated-chart",
  "title": "Revenue by region",
  "description": "Regional revenue as bar and part-to-whole pie.",
  "sql": "SELECT region, SUM(revenue) AS revenue FROM sales GROUP BY region",
  "dialectId": "CALCITE",
  "statementKind": "select",
  "source": "generated",
  "validationWarnings": [],
  "columns": [
    { "name": "region", "type": "STRING" },
    { "name": "revenue", "type": "DOUBLE" }
  ],
  "charts": [
    {
      "chartKey": "bar-by-region",
      "title": "Revenue by region (bar)",
      "chartType": "bar",
      "encodings": {
        "category": { "field": "region", "label": "Region" },
        "value": { "field": "revenue", "label": "Revenue" }
      },
      "options": { "orientation": "vertical", "stacked": false },
      "presentation": {
        "sort": [{ "field": "revenue", "direction": "desc" }],
        "legend": { "visible": false },
        "labels": { "visible": true }
      }
    },
    {
      "chartKey": "pie-share",
      "title": "Revenue share (pie)",
      "chartType": "pie",
      "encodings": {
        "category": { "field": "region", "label": "Region" },
        "value": { "field": "revenue", "label": "Revenue" }
      },
      "presentation": {
        "legend": { "visible": true },
        "labels": { "visible": true }
      }
    }
  ],
  "lineage": {
    "sourceArtifactIds": ["sql-artifact-id-1"]
  }
}
```

### 2.2 Single-chart (same query, one view)

`charts` has **exactly one** element. Root-level artifact `title` may duplicate or generalize the
per-chart title.

```json
{
  "artifactType": "generated-chart",
  "title": "Clients by country",
  "sql": "SELECT country, COUNT(*) AS client_count FROM clients GROUP BY country",
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
      "options": { "orientation": "vertical", "stacked": false },
      "presentation": {
        "sort": [{ "field": "client_count", "direction": "desc" }],
        "labels": { "visible": true }
      }
    }
  ]
}
```

### 2.3 `chartKey`

| Rule | Detail |
|------|--------|
| Required | yes, on every `charts[]` entry |
| Uniqueness | unique within the artifact |
| Format | stable slug: `[a-z0-9][a-z0-9-]*` (e.g. `bar-by-region`, `default`) |
| Purpose | UI tab ids, Run targeting, future intra-artifact pointers |

Validator assigns `chartKey: "default"` when the model omits it and `charts.length === 1`.

---

## 3. Single-chart shorthand (normalization)

Early examples placed `chartType`, `encodings`, `options`, and `presentation` at the **artifact root**.
That remains a **normalization input** only:

- On `validate_chart_spec` success, the validator **always emits canonical** `charts[]` form.
- Wire replay and UI parsers accept legacy flat payloads **only** if `charts` is absent and
  `chartType` is present at root — they normalize to a one-element `charts[]` in memory.
- New emissions must use `charts[]` (preferred from first implementation).

Flat legacy ( ingest only, do not emit ):

```json
{
  "artifactType": "generated-chart",
  "chartType": "bar",
  "encodings": { "category": { "field": "country" }, "value": { "field": "client_count" } },
  "options": { "orientation": "vertical" }
}
```

→ normalized to `charts: [{ chartKey: "default", chartType: "bar", ... }]`.

---

## 4. Turn-level emission patterns

| User intent | SQL artifacts | Chart artifacts | `charts[]` length per artifact |
|-------------|---------------|-----------------|--------------------------------|
| One chart | 1 | 1 | 1 |
| Two charts, same data | 1 | 1 | 2+ |
| Two charts, different queries | 2 | 2 | 1 each (typical) |
| One query, three views | 1 | 1 | 3 |

Platform **multi-artifact turns** (already used for facets) apply: a turn may carry multiple
`generated-chart` records when multiple queries were required.

**Emission order (recommended):**

```text
generated-sql (query A)
generated-chart (query A, charts: [...])
generated-sql (query B)   // if needed
generated-chart (query B, charts: [...])
```

Chart artifacts for query A must not appear after query B's SQL unless lineage explicitly ties a
refinement; default pairing is sequential sql → chart(s) for that sql.

---

## 5. Validation (`validate_chart_spec`)

MVP tool validates **one chart config at a time** against the shared `columns` schema:

| Input | Purpose |
|-------|---------|
| `sql` + `columns` / schema from `describe_sql` | Shared query binding |
| `chartType`, `encodings`, `options`, `presentation` | Single config under validation |
| `chartKey` | Optional; required when merging into existing multi-chart artifact |
| `mergeIntoArtifactId` | Optional future — append to prior chart artifact on same turn |

**Success path:**

- First chart for a SQL → emit new `generated-chart` with `charts: [ config ]`.
- Additional chart for **same** SQL on same turn → runtime merges into existing artifact's
  `charts[]` (append after re-validation) **or** model calls validate with full `charts[]` — WI-368
  picks one merge strategy; contract requires **one artifact per SQL**, not one validate call per
  artifact only.

**Failure:** unchanged — text reply only, no artifact (§10).

All configs in one artifact must validate against the **same** `columns` snapshot.

---

## 6. Pointers and lineage

| Pointer | Cardinality | Meaning |
|---------|-------------|---------|
| `last-sql` | singular (existing) | Most recently emitted generated SQL |
| `last-chart` | singular | Most recently emitted **chart artifact** (whole bundle, not one `chartKey`) |

Intra-artifact addressing uses `chartKey` inside the artifact payload, not a separate pointer store
entry for MVP.

`lineage.sourceArtifactIds` links the chart artifact to its generated-sql (and optional data
artifact). When multiple chart artifacts exist on a turn, each points to its own SQL artifact id.

---

## 7. UI implications (WI-370)

| Case | UX direction |
|------|----------------|
| `charts.length === 1` | Current design: Chart \| Data \| SQL tabs |
| `charts.length > 1` | Chart tab shows **sub-tabs or carousel** per `chartKey`; **one** Data tab and **one** SQL tab (shared query) |
| Run | Loads **one** snapshot; re-renders all chart configs from same rows |
| Run all | One snapshot fetch per chart **artifact** (not per `charts[]` entry) |

---

## 8. Limits (MVP)

| Limit | Value | Rationale |
|-------|-------|-----------|
| Max `charts[]` per artifact | **5** | Readability; validator warning above 3 |
| Max chart artifacts per turn | **5** | Align with multi-query decomposition guard |

---

## 9. ECharts compilation

`compileChartSpecToECharts` compiles **one** `charts[]` entry at a time:

```typescript
compileChartSpecToECharts(
  artifact: GeneratedChartArtifact,
  chartKey: string,
  rows: Record<string, unknown>[],
  theme?: ChartThemeTokens
): EChartsOption
```

Same `rows` input for every config in the artifact.

---

## 10. Non-goals

- One artifact spanning **multiple SQL** statements.
- Nested chart artifacts inside SQL artifacts.
- Failed/partial chart configs in a published artifact (validation fails → no artifact).

---

## 11. Related

| Doc | Role |
|-----|------|
| [`../chart-artifact-contract.md`](../chart-artifact-contract.md) §5 | Normative fields |
| [`../artifact-foundation.md`](../artifact-foundation.md) | Multi-artifact turns |
| Gap 17 | Last-result / pointer follow-ups — [`chart-context-resolution.md`](./chart-context-resolution.md) |
