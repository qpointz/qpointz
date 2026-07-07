# AI chart visualizations in SQL artifacts

**Status:** `closed` (archived **2026-07-07**)

## Goal

Extend `sql-query.generated-sql` so it becomes the durable analytical artifact for SQL, human
context, result schema, and visualization configuration.

Chart requests enrich the same `sql.generated` artifact with one or more entries in
`visualizations[]`. The chart-mapping capability still owns chart selection and validation, and it
contributes visualization configs to the SQL artifact.

## Product Intent

Users should be able to ask for data and visualizations naturally:

- "show last result as a pie chart"
- "make this a bar chart"
- "plot revenue by month"
- "use country as categories and client_count as values"

The answer should appear as one SQL/data card. When chart visualizations exist, the same card can
show Chart, Data, and SQL views. The durable payload remains renderer-agnostic; `ui/mill-ui`
compiles semantic visualization configs to ECharts locally.

## Target Artifact Shape

The persisted protocol remains:

| Field | Value |
|-------|-------|
| `protocolId` | `sql-query.generated-sql` |
| `persistKind` | `sql.generated` |
| `wirePartType` | `sql` |
| active pointer | `last-sql` |

Target payload:

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
  "profiling": []
}
```

Section rules:

| Section | Required | Role |
|---------|----------|------|
| `sql` | yes | Machine-readable SQL statement, dialect, statement kind, source, and validation warnings. |
| `info` | yes | Human-readable title and description for cards, follow-ups, and disambiguation. |
| `schema` | no | Trusted result schema from `describe_sql`; required before chart visualization validation. |
| `visualizations[]` | no | Renderer-agnostic visualization configs. This story implements `kind: "chart"` only. |
| `profiling[]` | no | Reserved for non-LLM query analysis data such as explain plans or lineage payloads. |

## Flow

```text
User asks for SQL/data only
  -> model calls validate_sql with completionMode sql-only
  -> runtime opens completion plan + SQL artifact draft
  -> completion coordinator finalizes and persists one sql.generated artifact
  -> ui/mill-ui renders SQL/Data views

User asks for a chart
  -> data-analysis routes SQL needs to sql-query when no suitable SQL exists
  -> model calls validate_sql with completionMode sql-with-chart
  -> runtime opens plan (validate_sql, describe_sql, validate_chart_spec) + draft
  -> sql-query.describe_sql merges schema into draft
  -> chart-mapping lists supported charts when needed
  -> chart-mapping validate_chart_spec merges normalized visualization into draft
  -> completion coordinator finalizes and persists one sql.generated artifact
  -> ui/mill-ui Run loads a bounded full data snapshot
  -> shared ChartRenderer compiles the chart visualization to ECharts and renders it
```

For "show last result as pie chart", the runtime/model uses the latest SQL artifact context. If the
existing result shape cannot support the requested chart, chart-mapping returns a diagnostic such as
`query_refinement_needed`; it does not rewrite SQL itself.

## Architectural Principles

| Rule | Detail |
|------|--------|
| **One SQL artifact** | SQL, schema, visualization configs, and future profiling references live under one `sql.generated` artifact. |
| **Completion plan** | Turn-scoped plan declares required steps (`completionMode`); runtime finalizes when the plan is satisfied — not when individual validators run. |
| **Validation is pure** | `validate_sql`, `validate_chart_spec`, and other `validate_*` tools return structured pass/fail only; they do not emit or persist durable artifacts (`emitsOnSuccess` is removed from validators). |
| **Final artifact only** | Only the completion coordinator emits `sql.generated` after the plan completes (or terminal failure rules apply). |
| **SQL stays in `sql-query`** | Chart mapping never generates, rewrites, validates, or executes SQL. |
| **Schema-bound mappings** | Every encoded chart field must exist in trusted `schema[]` from `describe_sql`. |
| **Renderer-agnostic visualization** | Chart configs use semantic roles and options, never ECharts/Vega/Plotly config. |
| **Catalog-driven chart support** | Supported chart types, role constraints, options, and snapshot limits come from the chart catalog tool. |
| **Row snapshots are runtime data** | Durable SQL artifacts do not embed chart row data. Chart rendering fetches bounded full snapshots at run time. |
| **Reusable renderer** | Chart rendering lives in shared UI chart components, not chat-specific code. |

## Chart Catalog Tool

The chart-mapping capability exposes supported chart types through a structured catalog tool such as
`list_supported_charts`. Prompts should instruct the model to call the catalog when support,
encodings, options, or limits are needed; prompts should not duplicate the catalog contents.

MVP chart types:

- `bar`
- `line`
- `area`
- `scatter`
- `pie`

The catalog defines required/optional encoding roles, compatible Mill logical types, chart options,
constraints, and snapshot limits. Future visualization kinds can be added later under
`visualizations[]`, but this story implements chart visualizations only.

## Chart Data Snapshot Policy

Chart visualizations describe how to render a SQL result; they do not contain result rows.

- Chart rendering uses shared query execution in bounded `full` result mode.
- Grid/data inspection may use paged result mode independently.
- Snapshot limits come from the chart catalog where available.
- If a required snapshot is truncated or exceeds chart limits, the UI must not present it as a
  complete chart.
- If an existing matching data result is sufficient for the chart snapshot, the UI may reuse it.

## Scope

1. Keep Stage 1 `sql-query.describe_sql` / `execute_sql` foundation as the completed prerequisite.
2. Redesign the generated SQL artifact payload around `sql`, `info`, optional `schema`,
   `visualizations[]`, and optional `profiling[]`.
3. Change runtime artifact emission so only final completed SQL artifacts are persisted.
4. Add `chart-mapping` catalog and validation tools that return normalized chart visualization
   configs.
5. Wire persistence, SSE, REST replay, and scenarios around one enriched `sql.generated` artifact.
6. Add `ui/mill-ui` chart rendering inside the existing SQL/data composite experience.

## Non-Goals

- Only SQL artifacts are in scope for durable chart configuration and UI chrome.
- No SQL generation, SQL rewriting, SQL validation, or SQL execution inside chart-mapping.
- No raw ECharts/Vega/Plotly configuration in durable artifacts.
- No implementation of explain-plan or lineage profiling producers in this story.
- No full Visual Analysis board or dashboard builder.
- No server-side chart image rendering.

## Reusable UI Boundary

Chart rendering must be shared infrastructure:

```text
SQL artifact visualizations[]
        -> chart visualization selector
        -> chart data adapter
        -> semantic chart config to ECharts compiler
        -> shared ChartRenderer component
```

Expected reusable modules:

```text
ui/mill-ui/src/components/charts/
  types.ts
  compileChartSpecToECharts.ts
  ChartRenderer.tsx
  chartTheme.ts
  chartData.ts
```

The shared renderer accepts plain chart config and row data props. It must not depend on chat
messages, turns, artifact cards, or chat expand state.

## Chat UI Behaviour

- SQL-only artifacts keep the existing SQL/Data experience.
- SQL artifacts with chart visualizations show Chart, Data, and SQL views.
- The Chart view is primary for chart requests and renders from a bounded full snapshot.
- The Data view may use existing paged/grid UI.
- Run on a chart-enabled SQL card loads the chart snapshot and renders the chart.
- Run all executes/hydrates SQL/data first, then renders chart visualizations that depend on those
  results.
- Expand opens the chart view first when chart visualizations are present.
- If no snapshot is available, the chart view shows a chart-specific empty state.

## Dependencies

- Completed Stage 1 SQL execution/schema tools:
  - `sql-query.describe_sql`
  - `sql-query.execute_sql`
- `data-analysis` profile includes `sql-query` and will include `chart-mapping`.
- Current artifact foundation:
  - `ArtifactDescriptor`
  - `ArtifactWireMapper`
  - SSE structured parts
  - `ui/mill-ui` artifact treatment registry

The former standalone data-query story remains absorbed into Stage 1. There is no separate
data-query capability.

## Work Item Stages

### Stage 1 - sql-query execution foundation

| Seq | WI | Rationale |
|-----|----|-----------|
| 1 | WI-338 | Lock `sql-query` execution/schema-probe tool contracts, limits, and MCP inventory changes. |
| 2 | WI-339 | Extend `sql-query` with `describe_sql` / `execute_sql` tools. |
| 3 | WI-340 | Wire `sql-query` execution ports, limits, Spring/autoconfigure, and mill-service. |
| 4 | WI-341 | Prove `sql-query` exposure and execution with unit/MCP tests and docs. |

### Stage 2 - SQL artifact protocol

| Seq | WI | Rationale |
|-----|----|-----------|
| 5 | WI-366 | Define the SQL artifact visualization protocol and chart visualization config shape. |
| 6 | WI-367 | Implement SQL artifact completion coordinator and required `info` context (`WI-367-generated-sql-title-description.md`) |

### Stage 3 - Chart visualization capability

| Seq | WI | Rationale |
|-----|----|-----------|
| 7 | WI-368 | Implement chart-mapping catalog, prompts, routing, and visualization validation. |

### Stage 4 - Runtime, wire, and scenario proof

| Seq | WI | Rationale |
|-----|----|-----------|
| 8 | WI-369 | Persist, stream, replay, and test enriched SQL artifacts with `visualizations[]`. |

### Stage 5 - mill-ui rendering and execution UX

| Seq | WI | Rationale |
|-----|----|-----------|
| 9 | WI-370 | Render chart visualizations from SQL artifacts in the current UI. |

## Work Items

### Stage 1 - sql-query execution foundation

- [x] WI-338 - sql-query execution tool design (`WI-338-sql-query-execution-design.md`)
- [x] WI-339 - sql-query execution tools (`WI-339-sql-query-execution-tools.md`)
- [x] WI-340 - sql-query execution wiring (`WI-340-sql-query-execution-wiring.md`)
- [x] WI-341 - sql-query MCP tests and docs (`WI-341-sql-query-mcp-tests.md`)

### Stage 2 - SQL artifact protocol

- [x] WI-366 - SQL artifact visualization protocol (`WI-366-chart-artifact-contract.md`)
- [x] WI-367 - SQL artifact completion coordinator (`WI-367-generated-sql-title-description.md`)

### Stage 3 - Chart visualization capability

- [x] WI-368 - Chart visualization capability (`WI-368-chart-mapping-capability.md`)

### Stage 4 - Runtime, wire, and scenario proof

- [x] WI-369 - SQL artifact wire and scenario proof (`WI-369-chart-artifact-runtime-and-wire.md`)

### Stage 5 - mill-ui rendering and execution UX

- [x] WI-370 - mill-ui SQL visualizations (`WI-370-mill-ui-chart-artifact-preview.md`)

## Verify (full story - before MR)

```bash
./gradlew :ai:mill-ai:test --tests "*SqlQueryExecution*"
./gradlew :ai:mill-ai-mcp-core:test
./gradlew :ai:mill-ai-mcp-transport-http:testIT --tests "*SqlQueryExecution*"
./gradlew :ai:mill-ai:test --tests "*Chart*"
./gradlew :ai:mill-ai-test:testIT --tests "*Chart*"
./gradlew :ai:mill-ai-service:test --tests "*ArtifactWireMapper*"
cd ui/mill-ui && npm run test -- --run
cd ui/mill-ui && npm run build
```

## Closure

Story closed **2026-07-07**. Archive:
[`docs/workitems/completed/20260707-ai-chart-mapping/`](../completed/20260707-ai-chart-mapping/STORY.md).

MR-ready branch history: five commits above `origin/dev` (docs → AI runtime → service wiring → mill-ui → dataset note).

## Related

- [`GAPS.md`](GAPS.md)
- [`COLDSTART.md`](COLDSTART.md)
- [`sql-query` capability](../../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml)
- [`artifact-foundation.md`](../../../design/agentic/artifact-foundation.md)
- [`chat-artefact-architecture.md`](../../../design/ai/chat-artefact-architecture.md)
