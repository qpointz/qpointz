# WI-340 - sql-query execution wiring

Status: `done`  
Type: `feature`  
Area: `ai`, `apps`  
Depends on: [WI-339](WI-339-sql-query-execution-tools.md)

## Problem Statement

The `sql-query` capability requires data-plane beans, execution infrastructure, and caller context
for `describe_sql` / `execute_sql`. Execution must not appear in the default `schema-exploration`
MCP profile, but `data-analysis` already includes `sql-query` and needs schema probing for charting.

## Goal

Wire `sql-query` execution ports, limits, and caller context into Spring Boot mill-service. Reuse
existing profiles: `data-analysis` includes `sql-query`; `schema-exploration` does not.

## Gap Inputs

Resolve these review items from [`GAPS.md`](GAPS.md) before or as part of this WI:

- Gap 5 sql-query execution dependency port and backend result shape
- Gap 6 Profile and runtime access for `describe_sql`
- Gap 7 sql-query execution limits, truncation, and snapshot relationship

## In Scope

1. Profile planning:
   - keep `sql-query` in `data-analysis`; no separate `data-query` capability or `data-execution`
     profile is introduced for this story
   - ensure `chart-mapping` can access `describe_sql` before WI-368
   - keep `schema-exploration` free of `sql-query.*`
2. `mill-ai-autoconfigure`:
   - supply `SqlQueryExecutionPort` as a thin adapter over `QueryResultExecutionService` from
     `data/mill-data-query`
   - wire or introduce the shared query-result facade/helper that supports `paged` and bounded
     `full` result modes
   - supply the default execution-backed SQL description strategy using `maxRows = 1`
   - map MCP HTTP request auth to `CallerContext` for tool invocation
   - align with existing MCP `mcpAgentContext` patterns
   - do not call `/api/v1/query` over HTTP internally
   - do not bypass the query-result engine with direct `DataOperationDispatcher` calls
3. Configuration properties:
   - `mill.ai.sql-query.execution.max-rows-default`
   - `mill.ai.sql-query.execution.max-rows-hard`
   - optional `mill.ai.sql-query.execution.timeout`
4. `apps/mill-service`:
   - document profile group or example for `skymill,ai,mcp` plus
     `mill.ai.mcp.profile=data-analysis` when MCP clients need SQL validation/execution tools
   - do not enable execution on public MCP by default without ops choice
5. Profile tests:
   - `data-analysis` includes `sql-query`
   - `data-analysis` lists `sql-query.describe_sql` and `sql-query.execute_sql`
   - `schema-exploration` excludes `sql-query.*`

## Out of Scope

- Changing default `mill.ai.mcp.profile` from `schema-exploration` globally.
- Production RBAC beyond existing `/services/**` plus admission stub.
- Backlog updates.

## Acceptance Criteria

- [x] mill-service boots with `data-analysis` and MCP profiles.
- [x] MCP lists `sql-query.describe_sql` and `sql-query.execute_sql` for `data-analysis`.
- [x] `schema-exploration` MCP profile does not list `sql-query.*`.
- [x] Configuration metadata generated for new `mill.ai.sql-query.execution.*` keys.
- [x] Chart story docs state how chart runtime obtains access to `describe_sql`.
- [x] Service wiring keeps the SQL description strategy replaceable.
- [x] Default `SqlQueryExecutionPort` implementation reuses `QueryResultExecutionService`.
- [x] Default execution adapter supports `paged` and bounded `full` modes through shared
      query-result service semantics.
- [x] Tool execution maps principal/caller identity to query-result `CallerContext`.

## Suggested commit

`[feat] WI-340: wire sql-query execution dependencies`
