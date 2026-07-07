# WI-339 - sql-query execution tools

Status: `done`  
Type: `feature`  
Area: `ai`  
Depends on: [WI-338](WI-338-sql-query-execution-design.md)

## Problem Statement

`sql-query` already validates SQL and emits generated-SQL artifacts, but it has no tool contract for
describing validated SQL result schema or executing validated SQL through the shared query-result
plane.

## Goal

Extend `sql-query` in `mill-ai`: manifest, provider, and handlers for `describe_sql` and
`execute_sql` that sit beside existing `validate_sql` and delegate to the Mill data plane through a
Spring-free port.

## Gap Inputs

Resolve these review items from [`GAPS.md`](GAPS.md) before or as part of this WI:

- Gap 1 `sql-query` execution tool split and output contracts
- Gap 2 Schema probe execution strategy
- Gap 3 Validated SQL input boundary
- Gap 4 Type normalization for chart validation
- Gap 5 sql-query execution dependency port and backend result shape
- Gap 7 sql-query execution limits, truncation, and snapshot relationship

## In Scope

1. Manifest `ai/mill-ai/src/main/resources/capabilities/sql-query.yaml`:
   - Keep existing `validate_sql`.
   - Add tool `describe_sql` with `kind: query` and JSON schemas per WI-338.
   - Add tool `execute_sql` with `kind: query` and JSON schemas per WI-338.
   - Update `sql-query.system` prompt to cover execution safety, validated-input prerequisite, row
     limits, and chart schema probing.
2. Existing `SqlQueryCapability` provider:
   - Reuse existing ServiceLoader registration.
   - Handlers invoke a `SqlQueryExecutionPort` thin abstraction.
   - `describe_sql` maps execution/probe metadata to the shared base envelope with `schema`.
   - `execute_sql` maps engine result to the same base envelope plus bounded JSON row objects.
3. Dependency port in `mill-ai`:
   - Keeps `mill-ai` free of Spring.
   - Models the minimum query-result operations the capability needs:
     - execute validated SQL with `paged` result mode
     - execute validated SQL with bounded `full` result mode
     - describe validated SQL by executing the same path with one row and discarding data
   - Default implementation is supplied later by autoconfigure and adapts to
     `QueryResultExecutionService`.
   - The port must not expose `DataOperationDispatcher` or Spring Web types.
4. Replaceable SQL description strategy:
   - MVP strategy executes through the SQL query execution port with `maxRows = 1`, backed by the
     query-result execution plane.
   - Strategy discards sampled rows and returns only schema metadata.
   - Strategy boundary is explicit so future implementations can use SQL wrapping or native
     describe/prepare without changing `describe_sql` callers.
5. Guards:
   - Enforce server default and hard row limits.
   - Reject empty SQL.
   - Require validated SQL input for execution/description handlers.
   - Do not generate, rewrite, or validate SQL inside `describe_sql` / `execute_sql`.
   - Surface dispatcher/backend errors as structured tool error content.
6. Unit tests in `mill-ai`:
   - handler happy path
   - schema probe path
   - schema probe uses `maxRows = 1` and does not return rows
   - schema type values are Mill logical type names, e.g. `STRING`, `BIG_INT`, `TIMESTAMP_TZ`
   - precision, scale, length, and query-service `idx` are not part of the chart-facing schema
   - `nativeType` is omitted when the adapter cannot map it; tests must not require it
   - paged mode returns one page and paging/completeness metadata
   - full mode returns accumulated bounded rows and truncation/completeness metadata
   - truncation
   - blank SQL rejection
   - dispatcher/backend failure
   - manifest load and materialized tool registration

## Out of Scope

- MCP transport tests (WI-341).
- `mill-service` profile wiring (WI-340).
- `explain_sql`, Substrait, query sessions.
- Chart-mapping validator implementation.

## Acceptance Criteria

- [x] `./gradlew :ai:mill-ai:test --tests "*SqlQueryExecution*"` passes.
- [x] `CapabilityRegistry` still discovers `sql-query`.
- [x] Existing `sql-query.yaml` manifest declares `validate_sql`, `describe_sql`, and `execute_sql`.
- [x] Materialized capability invokes both handlers without the LangChain4j agent loop.
- [x] Both handlers share one DTO/model for `sql`, `dialectId`, `schema`, `warnings`, and `source`.
- [x] `describe_sql` returns Mill logical type names suitable for chart mapping.
- [x] `describe_sql` MVP implementation delegates through a replaceable strategy using
      `maxRows = 1`.
- [x] `describe_sql` does not return sampled rows.
- [x] `execute_sql` supports `paged` and bounded `full` result modes.
- [x] Full-result accumulation is delegated to the query-result plane/facade, not duplicated in the
      capability handler.
- [x] Execution handlers consume validated SQL and do not generate, rewrite, or validate SQL.
- [x] Capability code depends only on the `SqlQueryExecutionPort`; query-result engine wiring is
      provided outside `mill-ai`.

## Suggested commit

`[feat] WI-339: add sql-query execution tools`
