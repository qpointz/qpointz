# Chart artifact contract — superseded topology

**Status:** Chart **semantic specs** under [`charts/`](./charts/README.md) remain normative. **Durable emission** target is
[`sql-artifact-visualization-protocol.md`](./sql-artifact-visualization-protocol.md) (`sql.generated.visualizations[]`) — not a separate `generated-chart` artifact.

**Audience:** Agents and developers adding chart-mapping capability, artifact wiring, or chart clients  
**Modules:** `ai/mill-ai`, `ai/mill-ai-service`, `ui/mill-ui`

> **Active protocol:** [`sql-artifact-visualization-protocol.md`](./sql-artifact-visualization-protocol.md)  
> **Story:** [`../../workitems/in-progress/ai-chart-mapping/STORY.md`](../../workitems/in-progress/ai-chart-mapping/STORY.md)

The sections below retain historical `generated-chart` / `charts[]` topology for reference during doc migration. **Do not implement** a separate chart persist kind or wire part.

---

## Legacy document (pre–WI-366 delivery)

**Original status:** Locked design for `ai-chart-mapping` Stage 2 (WI-366)

Related docs:

| Document | Role |
|----------|------|
| [`artifact-foundation.md`](./artifact-foundation.md) | Runtime artifact descriptor, emission, persistence, SSE, and UI integration model |
| [`v3-capability-manifest.md`](./v3-capability-manifest.md) | Capability YAML schema for prompts, tools, protocols, artifacts, and MCP exposure |
| [`v3-mcp-capability-exposure.md`](./v3-mcp-capability-exposure.md) | Capability tool exposure model; update inventory when chart tools ship |
| [`../ai/chat-artefact-architecture.md`](../ai/chat-artefact-architecture.md) | Chat UI treatment and replay architecture; cross-link from there when chart UI wiring lands |
| [`charts/README.md`](./charts/README.md) | **Accepted (WI-366):** per-chart-type specs + ECharts 6 compiler |
| [`charts/forward-compatible-families.md`](./charts/forward-compatible-families.md) | **Verified appendix:** future treemap, sunburst, histogram, heatmap, bubble (Gap 13) |
| [`charts/multi-chart-artifact-model.md`](./charts/multi-chart-artifact-model.md) | **Locked:** one query per artifact; `charts[]` for multiple views (Gap 14) |
| [`charts/chart-context-resolution.md`](./charts/chart-context-resolution.md) | **Locked:** prompt-based last query/chart resolution; multi-SQL via title/description (Gap 17) |
| [`charts/chart-routing-intent.md`](./charts/chart-routing-intent.md) | **Locked:** `CHART_MAP` capability-local intent; `data-analysis` composes only (Gap 18) |
| [`charts/chart-emission-path.md`](./charts/chart-emission-path.md) | **Locked:** OnToolSuccess from `validate_chart_spec` only (Gap 19) |
| [`charts/chart-mcp-exposure.md`](./charts/chart-mcp-exposure.md) | **Locked:** MCP tools for external chart workflow (Gap 20) |
| [`charts/chart-test-proof-strategy.md`](./charts/chart-test-proof-strategy.md) | **Locked:** layered mock proof + scenario export (Gap 21) |
| [`../../workitems/in-progress/ai-chart-mapping/STORY.md`](../../workitems/in-progress/ai-chart-mapping/STORY.md) | Story scope and WI order |
| [`query-artifact-presentations.md`](./query-artifact-presentations.md) | **Target:** one `generated-query` artifact; chart as `presentations[]` |

---

## 1. Purpose

`generated-chart` is a durable, renderer-agnostic artifact that describes how to visualize the result
of an existing generated SQL artifact.

The chart artifact decorates the generated SQL artifact. It does not generate, rewrite, validate, or
execute SQL. SQL remains owned by the `sql-query` capability. Chart mapping consumes an existing SQL
artifact plus trustworthy result metadata and emits a semantic chart specification that clients can
compile to ECharts, Vega, Plotly, a notebook chart, or a table fallback.

This document is the canonical design hub for the chart artifact contract. Runtime mechanics still
belong in [`artifact-foundation.md`](./artifact-foundation.md); chat-specific treatment details still
belong in [`../ai/chat-artefact-architecture.md`](../ai/chat-artefact-architecture.md).

---

## 2. Canonical location decision

The canonical chart artifact design lives in this file:

```text
docs/design/agentic/chart-artifact-contract.md
```

Reasoning:

- The chart artifact is primarily an agentic artifact contract, not only a chat UI treatment.
- `artifact-foundation.md` should stay the shared artifact pipeline reference rather than absorb the
  full chart schema, catalog, validation, and snapshot policy.
- `chat-artefact-architecture.md` should document how chat renders chart artifacts once implemented,
  but it should not own the artifact payload contract.
- `v3-mcp-capability-exposure.md` should only list chart-mapping tools after the capability exists.

Implementation work should update this document first when changing chart artifact semantics, then
cross-link or summarize the surface-specific impact in the runtime, MCP, or UI documents as needed.

---

## 3. Contract boundaries

| Rule | Detail |
|------|--------|
| SQL is external | The chart capability consumes existing generated SQL and result metadata. It does not create or alter SQL. |
| Payload is self-contained | The chart artifact carries the SQL fields needed to understand the query. Lineage ids are provenance only. |
| Renderer agnostic | The artifact uses Mill semantic roles per chart type (§6). Clients compile to ECharts locally; see [`charts/`](./charts/README.md). |
| Schema-bound | Every encoded field must exist in the known result schema. Mapping without trustworthy schema must fail. |
| Data is not embedded | Row snapshots are render inputs fetched at run time; they are not durable chart artifact payload. |
| Catalog driven | Supported chart types and constraints are exposed through a chart-mapping catalog tool and used by validation. |

---

## 4. Result schema provenance

Chart mapping requires a trustworthy result schema produced by actually running, or otherwise
server-side probing, the validated SQL. The model must not infer chartable columns from SQL text
alone.

Required orchestration:

```text
User asks for chart
  -> reasoning selects or creates SQL intent
  -> sql-query validates SQL and emits generated-sql
  -> runtime calls sql-query.describe_sql for result schema and Mill logical types
  -> chart-mapping receives generated-sql plus the `sql-query.describe_sql` schema
  -> chart-mapping validates encodings and emits generated-chart
```

The execution/probe step belongs to runtime orchestration through the `sql-query` capability, not to
the chart-mapping capability. The chart capability still does not generate, rewrite, validate, or
execute SQL. It consumes the validated SQL artifact and the schema metadata produced by
`sql-query.describe_sql`.

The first `describe_sql` implementation uses the normal query-result execution path with
`maxRows = 1`, discards the sampled row, and returns only schema metadata. That implementation must
live behind a replaceable strategy/port seam so a later backend can switch to SQL wrapping or native
describe/prepare without changing the `describe_sql` tool contract.

The MVP `sql-query` execution implementation should reuse the existing query-result execution plane:
`QueryResultExecutionService` from `data/mill-data-query` supplies execution, paging, row-object
marshalling, caller ownership, and schema extraction. The AI capability uses a Spring-free
`SqlQueryExecutionPort`; autoconfigure supplies a thin adapter over `QueryResultExecutionService`.
It must not call the `/api/v1/query` HTTP controller internally, and it must not bypass the
query-result engine with direct dispatcher calls.

The shared query-result execution plane should expose result-mode semantics reusable by both AI
tools and UI/runtime execution:

| Mode | Purpose |
|------|---------|
| `paged` | Return one bounded page for grid-style inspection. |
| `full` | Return an accumulated bounded result for chart snapshots or compact tool answers. |

`full` is never unbounded. It must report `rowCount`, effective `limit`, `truncated`, `hasMore`, and
`totalResult` when known. Chart rendering should use `full` mode so the chart receives a complete
snapshot within catalog limits; if the snapshot is truncated, the chart must not present it as a
complete result.

If the runtime cannot obtain schema metadata, chart mapping must fail validation and follow
§10 — no `generated-chart` artifact is emitted.

Column types used for chart validation must come from the execution/probe metadata as Mill
`LogicalDataTypeId` names: `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `BINARY`, `BOOL`, `DATE`,
`FLOAT`, `DOUBLE`, `INTERVAL_DAY`, `INTERVAL_YEAR`, `STRING`, `TIMESTAMP`, `TIMESTAMP_TZ`, `TIME`,
and `UUID`. Chart validators may collapse those names into internal families such as numeric,
temporal, categorical, or unsupported, but the tool contract should not introduce a separate
chart-specific type vocabulary. Precision, scale, and length are intentionally excluded from the
chart-facing schema contract.

Target `describe_sql` output shape:

```json
{
  "artifactType": "sql-description",
  "sql": "SELECT country, COUNT(*) AS client_count FROM clients GROUP BY country",
  "dialectId": "CALCITE",
  "schema": [
    { "name": "country", "type": "STRING", "nullable": true },
    { "name": "client_count", "type": "BIG_INT", "nullable": false }
  ],
  "warnings": [],
  "source": {
    "kind": "execution",
    "maxRows": 1
  }
}
```

`execute_sql` must extend the same base envelope with bounded row data and execution metadata; it
must not use a different schema field or shape. `source.kind` is **`execution`** for both tools;
`describe_sql` may include `maxRows: 1` under `source`. Optional `nativeType` on schema entries may
be populated by the query-result adapter when backend metadata is available; validators must not
require it.

---

## 5. Normative payload shape

Every `generated-chart` artifact is bound to **one SQL query and one result schema**. Visualizations
live in **`charts[]`**. Multiple views of the same query share one artifact; multiple queries produce
**separate chart artifacts** (possibly on the same turn). Full rules:
[`charts/multi-chart-artifact-model.md`](./charts/multi-chart-artifact-model.md).

### 5.1 Artifact root (shared query context)

| Field | Required | Description |
|-------|----------|-------------|
| `artifactType` | yes | Always `"generated-chart"`. |
| `title` | no | Query-level headline; copied from generated SQL when available. |
| `description` | no | Query-level summary. |
| `sql` | yes | Validated SQL from the decorated generated-SQL artifact. |
| `dialectId` | no | SQL dialect id when known. |
| `statementKind` | no | e.g. `select`. |
| `source` | no | SQL artifact source marker (e.g. `generated`). |
| `validationWarnings` | no | Warnings from SQL validation, preserved when present. |
| `columns` | yes | Trusted result schema snapshot from `sql-query.describe_sql`. |
| `charts` | yes | Array of **1–5** chart configs (§5.2). |
| `lineage` | no | Optional provenance; not required to render. |

### 5.2 Chart config entry (`charts[]`)

| Field | Required | Description |
|-------|----------|-------------|
| `chartKey` | yes | Stable id within artifact (e.g. `default`, `bar-by-region`). |
| `title` | no | Per-chart headline. |
| `description` | no | Per-chart summary. |
| `chartType` | yes | One of the MVP chart types (§6). |
| `encodings` | yes | Chart-type-specific semantic field bindings (§6–§8). |
| `options` | no | Chart-type-specific options (§7). |
| `presentation` | no | Sort, legend, labels (§9). |

`columns[]` entries use Mill logical type names (`STRING`, `BIG_INT`, `DATE`, …). Every
`encodings.*.field` in each chart config must reference a `columns[].name`.

Each encoding binding:

```json
{ "field": "<column name>", "label": "<optional axis/legend label>" }
```

Include **only** encoding roles valid for that entry's `chartType`.

### 5.3 Single-chart example

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
      "description": "Bar chart of client count by country.",
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
  ],
  "lineage": {
    "sourceArtifactIds": ["sql-123"]
  }
}
```

### 5.4 Multi-chart example (same query)

```json
{
  "artifactType": "generated-chart",
  "title": "Revenue by region",
  "sql": "SELECT region, SUM(revenue) AS revenue FROM sales GROUP BY region",
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
      "options": { "orientation": "vertical", "stacked": false }
    },
    {
      "chartKey": "pie-share",
      "title": "Revenue share (pie)",
      "chartType": "pie",
      "encodings": {
        "category": { "field": "region", "label": "Region" },
        "value": { "field": "revenue", "label": "Revenue" }
      },
      "presentation": { "legend": { "visible": true }, "labels": { "visible": true } }
    }
  ],
  "lineage": { "sourceArtifactIds": ["sql-456"] }
}
```

### 5.5 Legacy flat shape (ingest only)

Root-level `chartType` / `encodings` / `options` / `presentation` without `charts` may be **parsed**
for backward compatibility and normalized to `charts: [{ chartKey: "default", ... }]`. New
emissions must use `charts[]`.

`lineage` is optional. Clients and replay paths must not require lineage lookup to understand the
chart specification.

---

## 6. Chart-type encoding vocabulary

Chart configuration is **implementation-independent**: artifacts describe *what* to visualize using
**industry-familiar semantic roles** per chart family, not renderer config (`xAxis`, `series.data`,
Vega `mark`, Plotly `layout`, etc.).

Validators, the chart catalog (`list_supported_charts`), prompts, and UI compilers must agree on the
same role names and constraints for each `chartType`.

### 6.1 MVP chart types

| `chartType` | Chart family | Primary semantic roles |
|-------------|--------------|------------------------|
| `bar` | Categorical comparison | `category`, `value` |
| `line` | Trend / time series | `x`, `y` |
| `area` | Trend / composition under curve | `x`, `y` |
| `scatter` | Numeric relationship | `x`, `y` |
| `pie` | Part-to-whole | `category`, `value` |

**Role isolation:** a payload must use only roles declared for its `chartType`. Cross-family aliases
are forbidden — e.g. a `bar` chart must not encode its dimension as `x`; a `scatter` chart must not
use `category`/`value` as its primary mapping.

### 6.2 `bar`

Compares a **numeric measure** (`value`) across **categories** (`category`).

| Role | Required | Field semantics | Accepted Mill logical types |
|------|----------|-----------------|----------------------------|
| `category` | yes | Dimension / bucket label | `STRING`, `BOOL`, `UUID`, `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ` |
| `value` | yes | Numeric measure | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |
| `series` | no | Optional grouping for grouped or stacked bars | `STRING`, `BOOL`, `UUID` |

**Industry mapping:** category axis ↔ discrete dimension; value axis ↔ quantitative measure.
Vertical bars: categories on the category axis, values on the value axis. Horizontal bars swap
layout via `options.orientation` (§7.1), not via different encoding role names.

**Validation:**

- Exactly one `category` and one `value` unless `series` introduces multiple measures (catalog:
  `value` cardinality `one_or_many` when series present).
- Reject `x`, `y`, `hierarchy`, `bin` on `bar`.

### 6.3 `line`

Shows how a **numeric measure** (`y`) changes over an **ordered dimension** (`x`).

| Role | Required | Field semantics | Accepted Mill logical types |
|------|----------|-----------------|----------------------------|
| `x` | yes | Ordered dimension (time, date, or category sequence) | `STRING`, `DATE`, `TIME`, `TIMESTAMP`, `TIMESTAMP_TZ`, numeric ordinals |
| `y` | yes | Numeric measure | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |
| `series` | no | Multiple lines | `STRING`, `BOOL`, `UUID` |

**Industry mapping:** `x` ↔ horizontal axis dimension; `y` ↔ vertical measure. Clients compile to
renderer axis encodings locally.

**Validation:**

- Exactly one `x` and one `y` per series group.
- Reject `category`, `value`, `hierarchy`, `bin` as primary line encodings.

### 6.4 `area`

Same encoding contract as **`line`** (§6.3). The chart type selects area fill under the line;
`options.stacked` (§7.4) controls stacked vs overlaid areas when `series` is present.

### 6.5 `scatter`

Plots **numeric `x`** against **numeric `y`** to show correlation or distribution.

| Role | Required | Field semantics | Accepted Mill logical types |
|------|----------|-----------------|----------------------------|
| `x` | yes | Horizontal numeric measure | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |
| `y` | yes | Vertical numeric measure | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |
| `series` | no | Point grouping / color series | `STRING`, `BOOL`, `UUID` |
| `color` | no | Optional categorical color channel | `STRING`, `BOOL`, `UUID` |

**Industry mapping:** classic scatter `(x, y)` with optional series/color for grouped points.

**Validation:**

- `x` and `y` must map to numeric-compatible columns.
- Reject `category`, `value` as primary scatter encodings.

### 6.6 `pie`

Shows **part-to-whole** composition: each **slice label** (`category`) and **slice size** (`value`).

| Role | Required | Field semantics | Accepted Mill logical types |
|------|----------|-----------------|----------------------------|
| `category` | yes | Slice label | `STRING`, `BOOL`, `UUID` |
| `value` | yes | Slice size (single numeric measure) | `TINY_INT`, `SMALL_INT`, `INT`, `BIG_INT`, `FLOAT`, `DOUBLE` |

**Industry mapping:** wedge label ↔ `category`; wedge angle/area ↔ `value`.

**Validation:**

- Exactly one `category` and one `value`.
- Catalog constraints: `maxCategories`, `requiresPositiveValues` (see §7.5).
- Reject `x`, `y`, `series` on MVP `pie`.

### 6.7 Future roles (non-MVP, protocol reserved)

These roles are **not** MVP encodings but are reserved for later chart families. **Verification**
(non-normative examples, ECharts paths, catalog sketches):
[`charts/forward-compatible-families.md`](./charts/forward-compatible-families.md).

| Role | Future chart families |
|------|----------------------|
| `hierarchy` | treemap, sunburst — ordered array of `{ field, label? }` levels (root → leaf) |
| `bin` | histogram — numeric/date field to bin |
| `value` | treemap, sunburst, histogram, heatmap — numeric measure (shared with MVP pie/bar) |
| `size` | bubble — point size measure |
| `facet` | small multiples — facet dimension |

Validators must reject future roles until the matching `chartType` is catalog-supported.

---

## 7. Chart-type options

Chart-type-specific options are stored in the artifact **`options`** object. They mirror the chart
catalog entry for that `chartType`. Options are **not** duplicated at the payload root (no top-level
`orientation`).

The validator normalizes omitted options to catalog defaults before emission.

### 7.1 `bar` options

| Option | Type | Default | Meaning |
|--------|------|---------|---------|
| `orientation` | `"vertical"` \| `"horizontal"` | `"vertical"` | Bar growth direction |
| `stacked` | boolean | `false` | Stack values when `series` is used |

### 7.2 `line` options

No MVP options. Future: `interpolate`, `showPoints`.

### 7.3 `scatter` options

No MVP options.

### 7.4 `area` options

| Option | Type | Default | Meaning |
|--------|------|---------|---------|
| `stacked` | boolean | `false` | Stack areas when `series` is used |

### 7.5 `pie` options

No MVP options. Catalog-level **constraints** (not `options` fields) include `maxCategories` and
`requiresPositiveValues`; the validator enforces them and may attach warnings to
`validationWarnings` on the chart artifact when copied from SQL context.

### 7.6 Snapshot guidance (catalog)

Each catalog entry also declares snapshot limits used by chart rendering (not stored on the artifact):

| `chartType` | `defaultLimit` | `hardLimit` | Notes |
|-------------|----------------|-------------|-------|
| `bar` | 500 | 5000 | Category axis readability |
| `line` | 500 | 5000 | Point/segment density |
| `area` | 500 | 5000 | Same as line |
| `scatter` | 2000 | 10000 | Dense point clouds |
| `pie` | 100 | 500 | `maxCategories` ≤ 12 enforced separately |

---

## 8. Catalog alignment

`chart-mapping.list_supported_charts` is the runtime source of truth for:

- allowed `chartType` values
- `requiredEncodings` / `optionalEncodings` per type (role names from §6)
- per-role `dataTypes` and `cardinality`
- `options` schema and defaults (§7)
- `constraints` and `snapshot` limits (§7.6)

The validator must consume the same catalog model — not a hard-coded duplicate list in prompt text
or UI.

Target catalog fragment (normative shape; full YAML ships with WI-368):

```yaml
chartTypes:
  bar:
    label: Bar chart
    description: Compare numeric values across categories.
    requiredEncodings:
      category:
        dataTypes: [STRING, DATE, TIME, TIMESTAMP, TIMESTAMP_TZ, BOOL, UUID]
        cardinality: one
      value:
        dataTypes: [TINY_INT, SMALL_INT, INT, BIG_INT, FLOAT, DOUBLE]
        cardinality: one_or_many
    optionalEncodings:
      series:
        dataTypes: [STRING, BOOL, UUID]
        cardinality: zero_or_one
    options:
      orientation:
        type: string
        enum: [vertical, horizontal]
        default: vertical
      stacked:
        type: boolean
        default: false
    snapshot:
      defaultLimit: 500
      hardLimit: 5000

  line:
    label: Line chart
    description: Show a numeric measure over an ordered dimension.
    requiredEncodings:
      x:
        dataTypes: [STRING, DATE, TIME, TIMESTAMP, TIMESTAMP_TZ, TINY_INT, SMALL_INT, INT, BIG_INT]
        cardinality: one
      y:
        dataTypes: [TINY_INT, SMALL_INT, INT, BIG_INT, FLOAT, DOUBLE]
        cardinality: one
    optionalEncodings:
      series:
        dataTypes: [STRING, BOOL, UUID]
        cardinality: zero_or_one
    snapshot:
      defaultLimit: 500
      hardLimit: 5000

  area:
    label: Area chart
    description: Line chart with filled area under the measure.
    requiredEncodings:
      x:
        dataTypes: [STRING, DATE, TIME, TIMESTAMP, TIMESTAMP_TZ, TINY_INT, SMALL_INT, INT, BIG_INT]
        cardinality: one
      y:
        dataTypes: [TINY_INT, SMALL_INT, INT, BIG_INT, FLOAT, DOUBLE]
        cardinality: one
    optionalEncodings:
      series:
        dataTypes: [STRING, BOOL, UUID]
        cardinality: zero_or_one
    options:
      stacked:
        type: boolean
        default: false
    snapshot:
      defaultLimit: 500
      hardLimit: 5000

  scatter:
    label: Scatter plot
    description: Plot numeric x against numeric y.
    requiredEncodings:
      x:
        dataTypes: [TINY_INT, SMALL_INT, INT, BIG_INT, FLOAT, DOUBLE]
        cardinality: one
      y:
        dataTypes: [TINY_INT, SMALL_INT, INT, BIG_INT, FLOAT, DOUBLE]
        cardinality: one
    optionalEncodings:
      series:
        dataTypes: [STRING, BOOL, UUID]
        cardinality: zero_or_one
      color:
        dataTypes: [STRING, BOOL, UUID]
        cardinality: zero_or_one
    snapshot:
      defaultLimit: 2000
      hardLimit: 10000

  pie:
    label: Pie chart
    description: Show part-to-whole composition for one category and one numeric value.
    requiredEncodings:
      category:
        dataTypes: [STRING, BOOL, UUID]
        cardinality: one
      value:
        dataTypes: [TINY_INT, SMALL_INT, INT, BIG_INT, FLOAT, DOUBLE]
        cardinality: one
    constraints:
      maxCategories: 12
      requiresPositiveValues: true
    snapshot:
      defaultLimit: 100
      hardLimit: 500
```

Catalog `dataTypes` use Mill logical type names (Gap 4), not informal `number`/`string` labels.

---

## 9. Presentation

`presentation` holds **cross-chart** display hints that are not chart-type options:

```json
{
  "sort": [{ "field": "client_count", "direction": "desc" }],
  "legend": { "visible": false },
  "labels": { "visible": true }
}
```

| Field | Rules |
|-------|-------|
| `sort` | Optional. At most **3** entries. Each entry: `{ "field": "<column name>", "direction": "asc" \| "desc" }`. `field` must exist in `columns`. |
| `legend` | Optional. `{ "visible": boolean }`. |
| `labels` | Optional. `{ "visible": boolean }` for data labels on marks. |

Clients may ignore unsupported presentation hints for a chart family; validators should not reject
unknown presentation keys but must validate `sort` field references.

---

## 9.1 ECharts compilation (client-side)

Semantic artifacts are **ECharts-compatible by design** but do not embed ECharts JSON. `ui/mill-ui`
compiles `generated-chart` + row snapshot → ECharts 6 `option` using:

- `dataset.source` — row objects from SQL snapshot
- `encode` — dimension names for bar, line, area, scatter; `itemName`/`value` for pie
- Standard `series.type` values: `bar`, `line`, `scatter`, `pie` (area uses `line` + `areaStyle`)

Per-chart draft specs (artifact examples, validation, compiled `option` golden paths):

| Chart | Spec |
|-------|------|
| Bar | [`charts/bar-chart-spec.md`](./charts/bar-chart-spec.md) |
| Line | [`charts/line-chart-spec.md`](./charts/line-chart-spec.md) |
| Area | [`charts/area-chart-spec.md`](./charts/area-chart-spec.md) |
| Scatter | [`charts/scatter-chart-spec.md`](./charts/scatter-chart-spec.md) |
| Pie | [`charts/pie-chart-spec.md`](./charts/pie-chart-spec.md) |

Shared compiler pipeline: [`charts/echarts-compiler-contract.md`](./charts/echarts-compiler-contract.md).

---

## 10. Validation failures and user-visible outcomes

Chart mapping validates through **`chart-mapping.validate_chart_spec`**, mirroring
**`sql-query.validate_sql`**: structured tool result, **`emitsOnSuccess` only when
`passed === true`**, and **no failed chart artifact** in chat or GET replay.

### 10.1 UX rule (locked)

| Outcome | Chat / GET replay | User sees |
|---------|-------------------|-----------|
| Validation **passes** | `generated-chart` artifact emitted (`emitsOnSuccess`) | Chart card (after Run / snapshot) |
| Validation **fails** | **No** chart artifact, **no** failed-chart wire part, **no** empty chart card | **Assistant text reply only** — model explains why and what to do next |

Failed validation is **not** a first-class UI artifact type. The model consumes the tool result
(including `code` and `message`) and responds in prose. Optional `item.tool.result` SSE may carry
the structured wrapper for debugging, but mill-ui does **not** render a chart shell for failures.

Runtime / UI errors after a **valid** chart was emitted (snapshot truncated, compiler unsupported)
are separate — handled at Run time in WI-370, not as validation failures.

### 10.2 Tool result shape (`validate_chart_spec`)

Follows the `sql-validation` pattern from [`sql-query.yaml`](../../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml).

#### 10.2.1 Tool input shape (`validate_chart_spec`)

The validator consumes **trusted result schema** produced by `sql-query.describe_sql`. The model must
not infer columns from SQL text. Runtime orchestration (chat service / agent loop) is responsible for
calling `describe_sql` before chart validation when schema is not already available from the current
turn.

| Field | Required | Description |
|-------|----------|-------------|
| `columns` | yes | Result schema array from `describe_sql` (`name`, `type`, `nullable`, optional `nativeType`). Same shape as artifact `columns`. |
| `charts` | yes | Proposed chart configs to validate (1–5 entries). Each entry matches §5.2 (`chartKey`, `chartType`, `encodings`, optional `options` / `presentation`). |
| `sql` | no | Echo of decorated SQL for diagnostics; copied into `normalizedChart` on success. |
| `title` | no | Query-level title for `normalizedChart`. |
| `description` | no | Query-level description for `normalizedChart`. |
| `dialectId` | no | SQL dialect id when known. |
| `statementKind` | no | e.g. `select`. |
| `source` | no | SQL artifact source marker. |
| `validationWarnings` | no | Warnings from SQL validation to preserve on emission. |

Rules:

- Handlers **must not** read `last-sql-result` pointers or execute SQL to obtain schema. If
  `columns` is missing or empty, return `schema_unavailable` (§10.4.A).
- Single-chart proposals use `charts: [{ "chartKey": "default", ... }]`.
- Multi-view proposals (bar + pie of same query) pass multiple `charts[]` entries in **one** tool
  call; the validator checks each entry against the shared `columns`.
- Chart-mapping does **not** rewrite SQL. Unsuitable grain → `query_refinement_needed` (§10.4.C).

**Failure** (no artifact emission):

```json
{
  "artifactType": "chart-validation",
  "passed": false,
  "attempt": 1,
  "code": "query_refinement_needed",
  "message": "Pie chart needs one category column and one numeric value column. The current result has three numeric columns and no categorical field. Refine the SQL to GROUP BY region and SUM(revenue).",
  "chartType": "pie"
}
```

**Success** (triggers `generated-chart` via `emitsOnSuccess`):

```json
{
  "artifactType": "chart-validation",
  "passed": true,
  "attempt": 1,
  "message": null,
  "normalizedChart": {
    "artifactType": "generated-chart",
    "sql": "...",
    "columns": [],
    "charts": [
      {
        "chartKey": "default",
        "chartType": "bar",
        "encodings": {},
        "options": {}
      }
    ]
  }
}
```

| Field | Failure | Success |
|-------|---------|---------|
| `passed` | `false` | `true` |
| `code` | Machine-readable category (§10.4) | omitted or `null` |
| `message` | Human- and model-readable explanation | `null` |
| `attempt` | Correction loop attempt (1–3) | same |
| `normalizedChart` | omitted | Full payload ready for emission |
| `chartType` | Requested type when known | echoed in `normalizedChart` |

Descriptor direction: `chart-validation` artifact kind with **`persist: false`**, same as
`sql-validation` — tool-loop metadata only, not a chat card.

### 10.3 Emission path (locked)

See [`charts/chart-emission-path.md`](./charts/chart-emission-path.md) (Gap 19).

- **`validate_chart_spec` + `emitsOnSuccess`** when `passed === true` → `generated-chart`.
- **`emissionStrategy: OnToolSuccess`** — coordinator emits one final per successful validator call
  (supports Gap 14 multi-artifact turns).
- Model must **not** call a `chart-mapping.generated-chart` protocol directly.
- On failure, prompts instruct the model to **revise the chart spec** or **route SQL refinement to
  `sql-query`** — never emit a partial or “failed” chart artifact.

### 10.4 Why validation fails — failure taxonomy

These are the expected non-pass outcomes. Each maps to a `code` for scenarios and prompt loops; the
**user always sees prose**, not a chart card.

#### A. Prerequisites missing (orchestration / context)

| Situation | Typical `code` | What happened |
|-----------|----------------|---------------|
| No generated SQL artifact to decorate | `missing_sql_context` | User asked for a chart before any query exists (“pie chart” with no prior SQL). |
| `describe_sql` not run or failed | `schema_unavailable` | No trustworthy result schema (execution error, blank SQL, backend timeout). |
| Schema empty (`schema.length === 0`) | `schema_unavailable` | Query returned no columns (invalid or empty result shape). |

#### B. Spec / encoding mistakes (model or handler input)

| Situation | Typical `code` | What happened |
|-----------|----------------|---------------|
| Encoding role invalid for `chartType` (e.g. `x`/`y` on `bar`) | `invalid_encoding_role` | Wrong chart vocabulary for the selected type (Gap 11). |
| Field not in `describe_sql` schema | `unknown_field` | Typo or hallucinated column name. |
| Required encoding missing | `missing_encoding` | e.g. `pie` without `value`. |
| Column type incompatible with role (string on scatter `y`) | `incompatible_type` | Schema says `STRING`; role requires numeric. |
| Payload contains renderer keys (`xAxis`, `series.data`, …) | `renderer_config_rejected` | Model tried to pass ECharts/Vega config instead of semantic encodings. |
| Unknown or unsupported `chartType` | `unsupported_chart_type` | Not in catalog / no compiler. |

#### C. Result shape unsuitable for the requested chart (SQL grain / aggregation)

These are the **query-refinement** cases: the SQL runs, schema is known, but the **result shape**
does not support the requested visualization without **`sql-query` changing the query**. Chart
mapping **must not rewrite SQL**.

| Situation | Typical `code` | Example |
|-----------|----------------|---------|
| Not enough columns for encodings | `query_refinement_needed` | Scatter requested but result has only one numeric column. |
| Wrong grain — Multiply numeric measures without category | `query_refinement_needed` | Pie requested; result is `SELECT a, b, c` with three numeric columns and no slice label column. |
| Detail rows instead of aggregated categories | `query_refinement_needed` | Bar by country requested; result is row-level `client_id, country, …` with thousands of rows and no `GROUP BY`. |
| High cardinality for part-to-whole | `query_refinement_needed` or `too_many_categories` | Pie with 50 distinct categories (> `maxCategories` 12). Validator may suggest bar instead in `message`. |
| Non-positive slice values | `non_positive_value` | Pie with zero or negative `value` when `requiresPositiveValues`. |
| Zero total | `zero_total` | All pie values sum to 0. |
| Binary / unsupported types in encoded roles | `incompatible_type` | `BINARY` column chosen as `category`. |

#### D. Catalog constraint violations (chart rules on otherwise plausible data)

| Situation | Typical `code` | What happened |
|-----------|----------------|---------------|
| Too many pie categories | `too_many_categories` | Distinct `category` count > 12. |
| Snapshot would exceed hard limit | `snapshot_limit_exceeded` | Rare at validation if only schema is probed; more common at UI Run — see §10.1 distinction. |

#### E. Correction loop exhausted

| Situation | Typical `code` | What happened |
|-----------|----------------|---------------|
| Three failed `validate_chart_spec` attempts | `validation_exhausted` | Same pattern as SQL validation loop; model reports last `message` in prose. |

### 10.5 Model behavior after failure

Prompts (`chart-mapping.system`) should mirror `sql-query.system`:

1. Call `list_supported_charts` when chart type or constraints are unclear.
2. Call `validate_chart_spec` with proposed encodings + schema from `describe_sql`.
3. If `passed === false` and `code === query_refinement_needed` (or `missing_sql_context` /
   `schema_unavailable`): **do not** emit a chart; explain in text and either revise the chart spec
   or invoke **`sql-query`** to refine/regenerate SQL, then re-run `describe_sql`.
4. If `code` is an encoding mistake (`unknown_field`, `invalid_encoding_role`, …): revise spec
   and retry `validate_chart_spec` (up to 3 attempts).
5. Never expose a failed chart artifact to the user.

### 10.6 Scenario assertions (WI-369)

Tests assert **`validate_chart_spec` tool result** (`passed`, `code`, `message`) and absence of
`generated-chart` on failure — not a UI failed-card state.

---

## 11. Ownership by WI

| Area | Owning WI |
|------|-----------|
| Canonical schema, semantic roles, validation rules, diagnostics, snapshot contract | WI-366 (**done**) |
| `title` / `description` on generated SQL artifacts | WI-367 |
| `chart-mapping` capability, catalog tool, validator tool, profile routing | WI-368 |
| `generated-chart` artifact descriptor, persistence, SSE, GET replay, scenario tests | WI-369 |
| `ui/mill-ui` chart parsing, grouping, snapshot fetch, renderer, expanded views | WI-370 |

WI-366 locked the normative schema and validation sections in this document. Later WIs should update
this document only when they lock behavior that changes the artifact contract itself.

---

## 12. Extension guidance

Adding a chart family requires all of the following:

1. Add or update the chart catalog entry exposed by `chart-mapping.list_supported_charts`.
2. Extend validator support so semantic encodings and constraints are checked from the catalog.
3. Add client compiler support for at least `ui/mill-ui` or explicitly document an unsupported
   renderer state.
4. Add scenario and UI tests proving both accepted and rejected mappings.

Future chart families such as treemap, sunburst, histogram, heatmap, or bubble should extend the
semantic encoding vocabulary. They must not introduce renderer-specific payloads into
`generated-chart`. Verified mappings: [`charts/forward-compatible-families.md`](./charts/forward-compatible-families.md).
