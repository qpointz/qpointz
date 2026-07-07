# SQL artifact visualization protocol

**Status:** Locked — WI-366 normative source  
**Protocol:** `sql-query.generated-sql` → persist kind `sql.generated`  
**Wire:** `wirePartType: sql`, pointer `last-sql`

Related:

| Document | Role |
|----------|------|
| [`STORY.md`](../../workitems/completed/20260707-ai-chart-mapping/STORY.md) | Story scope and flow |
| [`GAPS.md`](../../workitems/completed/20260707-ai-chart-mapping/GAPS.md) | Completion plan + validation purity |
| [`generated-sql-artifact-context.md`](./generated-sql-artifact-context.md) | `info.title` / `info.description` on `validate_sql` |
| [`charts/README.md`](./charts/README.md) | Per-chart-type semantic specs |
| [`artifact-foundation.md`](./artifact-foundation.md) | Descriptor, emission, persistence |

---

## 1. Purpose

`sql-query.generated-sql` is the **single durable analytical artifact** for a SQL statement. It holds
machine-readable SQL, human context, optional trusted result schema, optional visualization configs,
and optional future profiling references.

Chart configuration lives in `visualizations[]` — there is **no** separate `generated-chart`
persist kind, protocol, or wire part.

---

## 2. Protocol surface

| Field | Value |
|-------|-------|
| `protocolId` | `sql-query.generated-sql` |
| `persistKind` | `sql.generated` |
| `wirePartType` | `sql` |
| Active pointer | `last-sql` |
| `artifactKind` | `generated-sql` |
| `presentation` | `structured` |

---

## 3. Payload shape

```json
{
  "artifactType": "generated-sql",
  "sql": {
    "text": "SELECT country, COUNT(*) AS client_count FROM clients GROUP BY country",
    "dialectId": "CALCITE",
    "statementKind": "select",
    "source": "generated",
    "validationWarnings": []
  },
  "info": {
    "title": "Clients by country",
    "description": "Counts clients grouped by country."
  },
  "schema": [
    { "name": "country", "type": "STRING", "nullable": true },
    { "name": "client_count", "type": "BIG_INT", "nullable": false }
  ],
  "visualizations": [
    {
      "key": "default",
      "kind": "chart",
      "title": "Clients by country",
      "description": "Client counts grouped by country.",
      "chartType": "bar",
      "encodings": {
        "category": { "field": "country", "label": "Country" },
        "value": { "field": "client_count", "label": "Clients" }
      },
      "options": { "orientation": "vertical", "stacked": false },
      "presentation": {
        "sort": [{ "field": "client_count", "direction": "desc" }],
        "legend": { "visible": false },
        "labels": { "visible": true }
      }
    }
  ],
  "profiling": []
}
```

### Section rules

| Section | Required | Role |
|---------|----------|------|
| `sql` | yes | Machine-readable SQL statement, dialect, statement kind, source, validation warnings |
| `info` | yes | Human-readable title and description for cards, follow-ups, disambiguation |
| `schema` | no | Trusted result schema from `describe_sql`; required before chart validation |
| `visualizations[]` | no | Renderer-agnostic visualization configs (`kind: "chart"` in this story) |
| `profiling[]` | no | Reserved for non-LLM query analysis (explain plans, lineage) |

### Canonical names

Use `visualizations[].key`, `schema[]`, nested `sql`, nested `info`. Do not add legacy alias layers
for new emissions.

---

## 4. Chart visualization entry (`kind: "chart"`)

| Field | Required | Notes |
|-------|----------|-------|
| `key` | yes | Stable id within the artifact; duplicate key replaces prior entry |
| `kind` | yes | Always `"chart"` for this story |
| `title` | no | Chart headline |
| `description` | no | Chart subtitle |
| `chartType` | yes | MVP: `bar`, `line`, `area`, `scatter`, `pie` |
| `encodings` | yes | Semantic roles per chart type (see [`charts/`](./charts/README.md)) |
| `options` | no | Chart-type options from catalog |
| `presentation` | no | Sort, legend, labels — not renderer config |

**Forbidden in durable payload:** ECharts, Vega, Plotly, or any renderer-specific option trees.

Every encoding `field` must reference a column in `schema[]`. Row snapshots are **runtime** inputs
fetched at render time — never embedded in `visualizations[]`.

---

## 5. Completion plan (ephemeral, turn-scoped)

Finalization is **not** driven by validator side effects. The model declares intent; the runtime
follows a recipe.

| `completionMode` | Plan opens via | Required steps (order) | Finalize when |
|------------------|----------------|------------------------|---------------|
| `sql-only` | `validate_sql` input | `validate_sql` | `validate_sql` succeeds |
| `sql-with-chart` | `validate_sql` input | `validate_sql` → `describe_sql` → `validate_chart_spec` (repeatable) | all required steps succeed |
| `enrich-existing` | `validate_chart_spec` + target ref | `describe_sql` if schema missing → `validate_chart_spec` (repeatable) | chart step(s) succeed |

### Terminal rules

| Case | Behavior |
|------|----------|
| `sql-with-chart` `describe_sql` fails | Discard draft; **no new** `sql.generated` persist |
| `sql-with-chart` chart step fails | Discard draft; **no new** `sql.generated` persist |
| `enrich-existing` chart step fails | **No write** to existing row |
| Invalid visualization | Assistant text only; no failed entry in `visualizations[]` |

### Validation purity (G-29)

`validate_sql`, `validate_chart_spec`, and other `validate_*` tools return structured pass/fail only.
They **must not** declare `emitsOnSuccess`. The **completion coordinator** emits
`sql-query.generated-sql` when the plan is satisfied.

---

## 6. Follow-up enrichment (`enrich-existing`)

Follow-up turns ("show last result as pie chart") open a plan via **`validate_chart_spec`** with:

- `targetArtifactId` — preferred when the model resolved a specific `sql.generated` row, or
- resolution via `last-sql` pointer when id is omitted.

Success updates the **same** artifact id in place. Failure leaves the row unchanged.

---

## 7. Multi-chart per SQL result

`validate_chart_spec` is **repeatable** within one plan. Each successful call merges one visualization
by `key`. Multiple views (bar + pie) = multiple calls with distinct `key` values before finalize.

---

## 8. Superseded topology

The prior `generated-chart` dual-artifact model is **not** the active path. Chart semantic specs under
[`charts/`](./charts/README.md) remain normative for encodings and compiler mapping; durable emission
target is `sql.generated.visualizations[]` only.
