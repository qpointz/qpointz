# WI-338 - sql-query execution tool design

Status: `done`  
Type: `design`  
Area: `ai`

## Problem Statement

MCP clients can list schema and validate SQL, but cannot execute already validated queries and
receive rows. Chart mapping also needs a reliable result-schema interface after SQL validation and
before chart configuration. Inferring schema from SQL text is not reliable enough.

Wrapping Jet as a one-off MCP resource would bypass the v3 capability model and duplicate security
and limits. The execution/schema-probe surface must extend the existing `sql-query` capability so
validation, generated SQL, bounded execution, and chart schema probing share one query contract.

## Goal

Document the `sql-query` capability extensions and tool contracts before implementation:

- `describe_sql` for SQL description and result schema metadata used by chart mapping.
- `execute_sql` for bounded SQL execution as an extension of `describe_sql`.

## Gap Inputs

Resolve these review items from [`GAPS.md`](GAPS.md) before or as part of this WI:

- Gap 1 `sql-query` execution tool split and output contracts
- Gap 2 Schema probe execution strategy
- Gap 3 Validated SQL input boundary
- Gap 4 Type normalization for chart validation
- Gap 6 Profile and runtime access for `describe_sql`
- Gap 7 sql-query execution limits, truncation, and snapshot relationship
- Gap 8a `execute_sql` default `resultMode`
- Gap 8b `source.kind` vocabulary for `describe_sql`
- Gap 8c `nativeType` in chart-facing schema output (MVP)
- Gap 8d `v3-mcp-capability-exposure.md` §2 SQL execution rule

## In Scope

1. Design doc updates under `docs/design/agentic/`:
   - Extend `v3-mcp-capability-exposure.md` with validated SQL execution rules under `sql-query`.
   - Add tool inventory rows for `sql-query.describe_sql` and `sql-query.execute_sql`.
   - Clarify that MCP resources remain descriptor metadata; query results and query schemas are tool
     output.
   - Update `chart-artifact-contract.md` if the schema output contract changes.
2. Schema tool contract:
   - Name: `describe_sql` -> MCP `sql-query.describe_sql`.
   - Input: `sql` (required), `dialect` (optional).
   - Output: base SQL description envelope:
     - `artifactType: "sql-description"`
     - `sql`, `dialectId`
     - `schema`: `[{ name, type, nullable, nativeType? }]`
       - `type` is the Mill `LogicalDataTypeId` name (`STRING`, `BIG_INT`, `TIMESTAMP_TZ`,
         etc.), not a chart-specific family.
       - Precision, scale, and length are intentionally excluded from the chart-facing schema
         contract.
       - `nativeType` is optional; the adapter may map it from backend metadata when available.
         Validators must not require it (gap 8c).
   - `warnings`
   - `source`: `{ "kind": "execution", "maxRows": 1 }` for `describe_sql`; `{ "kind": "execution" }`
     for `execute_sql` unless additional execution metadata is needed at the top level (gap 8b).
   - MVP implementation uses execution-backed describe with `maxRows = 1`; callers consume only
     schema metadata, not rows.
   - The contract must stay strategy-independent so the implementation can later switch to
     zero-row wrapping or native backend describe/prepare.
3. Execution tool contract:
   - Name: `execute_sql` -> MCP `sql-query.execute_sql`.
   - Input: `sql` (required), `resultMode` (`paged` or `full`, default **`paged`**), `max_rows`
     (optional int, server-capped), `dialect` (optional).
   - For `paged` mode, include first-page parameters such as `pageIndex` / `pageSize` or equivalent
     first-page defaults.
   - Output: same base envelope as `describe_sql`, plus:
     - `artifactType: "sql-result"`
     - `rows`: JSON row objects keyed by schema field name
     - `resultMode`, `rowCount`, `truncated`, optional `hasMore`, `totalResult`, `limit`
   - Errors: structured pass-through such as `QUERY_FAILED`, `UNAUTHORIZED`, row limit exceeded.
4. Profile definition:
   - Do not introduce a separate `data-query` capability or profile solely for execution.
   - `data-analysis` already includes `sql-query`, so chart flows can use `describe_sql`.
   - MCP clients that need SQL execution should use a profile that includes `sql-query`; the default
     `schema-exploration` profile still excludes `sql-query` and therefore excludes SQL validation
     and execution tools.
5. Security and limits:
   - default `max_rows`
   - hard `max_rows`
   - timeout
   - validated SQL input prerequisite
   - execution tools require validated SQL input; they must not bypass the `validate_sql`
     prerequisite in orchestrated flows
   - same auth as `/services/**`
6. Sequence diagrams:
   - chart runtime -> `sql-query.validate_sql` -> `sql-query.describe_sql` ->
     `SqlQueryCapability` ->
     SQL description strategy -> query-result execution plane with `maxRows = 1` ->
     `chart-mapping.validate_chart_spec`.
   - MCP client -> `tools/call execute_sql` -> `CapabilityMcpExecutor` ->
     `SqlQueryCapability` -> query-result execution plane.

## Backend reuse decision

`sql-query` execution tools must reuse the existing query-result execution plane. The default
implementation
should adapt to `QueryResultExecutionService` from `data/mill-data-query` through a Spring-free
`SqlQueryExecutionPort` in `mill-ai` and an autoconfigure adapter in WI-340. It should not call the
HTTP `/api/v1/query` controller internally, and it should not bypass the query-result engine by
using `DataOperationDispatcher` directly.

For the MVP:

- `execute_sql` creates a query-result session with `rows-objects`, includes page 0 with bounded
  `firstPageSize = maxRows`, maps the result to the tool envelope, and deletes the session after
  use for one-shot calls.
- `execute_sql.resultMode = "paged"` returns one bounded page.
- `execute_sql.resultMode = "full"` returns an accumulated bounded result. The accumulation should
  be implemented in the shared query-result execution plane or a query-result facade so AI tools,
  chart snapshot loading, and future clients do not duplicate page walking.
- `full` mode must always be bounded by caller limit and server hard cap; it must return
  completeness metadata (`truncated`, `hasMore`, `totalResult` when known, and effective `limit`).
- `describe_sql` uses the same path with `firstPageSize = 1`, maps `firstPage.schema`, discards
  rows, and deletes the session after use.
- `PagedQueryPayload.columnSchema` is the source schema. The tool contract trims query-service
  presentation fields down to the chart-facing shape and omits `idx`, `precision`, `scale`, and
  `length`.
- The strategy remains replaceable so a future backend can provide native describe/prepare without
  changing the tool contract.

## Out of Scope

- Implementation code (WI-339+).
- Backlog row updates.
- Query session REST (`/api/v1/query/**`) paging over MCP.
- Chart artifact schema details beyond the schema-provider interface.

## Acceptance Criteria

- [x] Design docs define `describe_sql` and `execute_sql`.
- [x] Tool inventory includes `sql-query.validate_sql`, `sql-query.describe_sql`, and
      `sql-query.execute_sql`.
- [x] Contract states that SQL validation happens before `describe_sql` / `execute_sql` in
      orchestrated flows.
- [x] Contract states that `describe_sql` / `execute_sql` consume validated SQL and do not create or
      rewrite SQL.
- [x] Contract states that `execute_sql` does not generate SQL.
- [x] Contract states that MVP `describe_sql` uses execution-backed describe with `maxRows = 1`
      and returns schema metadata only.
- [x] Contract states that `describe_sql` implementation is behind a replaceable strategy/port seam.
- [x] Contract defines `execute_sql` as a strict extension of the `describe_sql` base envelope.
- [x] Contract defines `execute_sql.resultMode` with `paged` and bounded `full` semantics.
- [x] Contract defines `schema` as the only schema field name.
- [x] Contract defines Mill logical type names as the schema type vocabulary for chart mapping.
- [x] Contract states that `sql-query` reuses `QueryResultExecutionService` through a thin port
      adapter.
- [x] Contract locks default `execute_sql.resultMode` as **`paged`** (gap 8a).
- [x] Contract locks `source.kind` as **`execution`** for `describe_sql` and `execute_sql` (gap 8b).
- [x] Contract states `nativeType` is optional adapter metadata; omit when unmapped (gap 8c).
- [x] `v3-mcp-capability-exposure.md` §2 states `sql-query` owns validation, generated SQL, bounded
      execution, and schema probing, with execution only on already validated SQL (gap 8d).

## Suggested commit

`[docs] WI-338: design sql-query execution tools`
