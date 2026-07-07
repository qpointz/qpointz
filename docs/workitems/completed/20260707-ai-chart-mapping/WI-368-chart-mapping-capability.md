# WI-368 - Chart visualization capability

Status: `done`
Type: `feature`, `test`
Area: `ai`

## Problem Statement

Chart requests need model guidance, a supported chart catalog, and validation against trustworthy
result schema. The chart capability must not generate SQL and must not emit a durable artifact of
its own.

## Goal

Implement `chart-mapping` as a capability that returns normalized chart visualization configs for
`sql.generated.visualizations[]`.

## Locked Decisions

- `chart-mapping` never generates, rewrites, validates, or executes SQL.
- Supported chart types, roles, options, constraints, and snapshot limits come from a catalog tool.
- `validate_chart_spec` returns a normalized `kind: "chart"` visualization config.
- Failed validation returns structured diagnostics; **no durable emission** (`emitsOnSuccess` forbidden).
- `data-analysis` composes `sql-query` and `chart-mapping`; SQL runs first for mixed requests.
- Follow-up chart references resolve against the latest SQL artifact and `visualizations[].key`.
- `validate_chart_spec` speaks the SQL artifact `visualizations[]` shape directly; it does not
  return `charts[]` or a chart artifact payload.

## In Scope

1. Add `capabilities/chart-mapping.yaml`.
2. Add capability-local prompts:
   - `chart-mapping.intent`: classify chart visualization requests.
   - `chart-mapping.system`: state that SQL belongs to `sql-query`.
   - `chart-mapping.spec`: define the workflow and semantic chart vocabulary without embedding the
     full chart catalog.
3. Add `list_supported_charts`:
   - supported chart types
   - required and optional encoding roles
   - compatible Mill logical types
   - chart options and defaults
   - chart-level constraints
   - snapshot guidance such as default and hard row limits
4. Add `validate_chart_spec`:
   - accepts SQL context, trusted `schema[]`, requested chart type, encodings, options, and
     presentation hints
   - validates all encoded fields against `schema[]`
   - validates chart roles and options against the chart catalog
   - rejects renderer-specific config
   - returns `passed`, `code`, `message`, and `normalizedVisualization`
   - returns `kind: "chart"` in the normalized visualization
   - uses `key` as the visualization identifier
   - **does not** declare `emitsOnSuccess`; results are merged by the WI-367 completion coordinator
5. Add capability provider and tool handler classes in `ai/mill-ai`.
6. Add `chart-mapping` to the `data-analysis` profile while keeping `sql-query` in that profile.
7. Define routing for mixed requests such as "plot revenue by month":
   - SQL generation/validation first when no suitable SQL exists
   - schema probing before chart validation
   - chart validation contributes to the SQL artifact draft
8. Define follow-up behavior:
   - "show last result as pie chart" uses latest SQL artifact context
   - "change this chart" resolves the latest chart visualization on the selected SQL artifact unless
     the user names a visualization explicitly
   - if multiple SQL artifacts are plausible, the model asks for clarification
   - if the result shape is unsuitable, return `query_refinement_needed`
9. Add MCP exposure for external-agent workflows:
   - chart catalog tool
   - chart validation tool
   - data-analysis profile inventory tests

## Out of Scope

- SQL generation, validation, rewriting, or execution.
- Persisting artifacts or `emitsOnSuccess` on `validate_chart_spec`.
- Emitting a chart-specific protocol final.
- UI rendering.
- Implementing non-chart visualization kinds.

## Acceptance Criteria

- [x] Capability manifest loads through the existing capability registry.
- [x] Prompt text explicitly forbids SQL generation, renderer-specific chart config, and direct
      protocol/artifact emission from validators.
- [x] Prompt text does not duplicate the chart catalog contents.
- [x] `list_supported_charts` exposes the MVP chart catalog.
- [x] Catalog does not advertise unsupported future chart types as available.
- [x] `validate_chart_spec` accepts valid semantic chart configs.
- [x] `validate_chart_spec` rejects invalid fields, invalid role use, unsupported chart types, and
      renderer-specific payloads.
- [x] Validator behavior is driven by catalog constraints.
- [x] Successful validation returns a normalized `kind: "chart"` visualization config for
      `visualizations[]`.
- [x] The validator output contract is documented with concrete input and output JSON shapes using
      `normalizedVisualization`.
- [x] `validate_chart_spec` has no `emitsOnSuccess`; coordinator merges `normalizedVisualization`.
- [x] Failed validation returns `passed`, `code`, and `message` only; no durable artifact.
- [x] `query_refinement_needed` is returned when the requested chart requires a differently shaped
      SQL result.
- [x] `data-analysis` includes both `sql-query` and `chart-mapping`.
- [x] MCP catalog and execution tests cover chart tools in `data-analysis`.

## Suggested Commit

`[feat] WI-368: add chart visualization capability`
