# WI-341 - sql-query MCP tests and docs

Status: `done`  
Type: `test`  
Area: `ai`  
Depends on: [WI-340](WI-340-sql-query-execution-wiring.md)

## Problem Statement

Without automated proof, `describe_sql` and `execute_sql` could regress on exposure filters,
auth, row limits, or schema metadata shape. Tool inventory docs would drift from manifests.

## Goal

Add unit and HTTP MCP `testIT` coverage for `sql-query.describe_sql` and
`sql-query.execute_sql`, then finalize design doc tool inventory.

## Gap Inputs

Resolve these review items from [`GAPS.md`](GAPS.md) before or as part of this WI:

- Gap 3 Validated SQL input boundary
- Gap 8 sql-query MCP proof strategy and fixtures

## In Scope

1. `mill-ai-mcp-core` unit tests:
   - `CapabilityMcpCatalogTest`: `data-analysis` profile includes `validate_sql`, `describe_sql`,
     and `execute_sql`; `schema-exploration` excludes `sql-query.*`.
   - `CapabilityMcpExecutorTest`: mock `SqlQueryExecutionPort` for schema probe, execute happy
     path, and limit exceeded.
2. `mill-ai-mcp-transport-http` `testIT`:
   - `@SpringBootTest` with `mill.ai.mcp.profile=data-analysis` and the standard Skymill fixture.
   - `shouldListTools_includingSqlQueryExecuteSql`.
   - `shouldListTools_includingSqlQueryDescribeSql`.
   - `shouldExecuteSql_andReturnStructuralResult`.
   - `shouldDescribeSql_andReturnSchemaShape`.
   - `shouldRequireAuthentication_whenSecurityEnabled`.
   - `shouldRejectExecuteSql_outsideProfile` when profile is `schema-exploration`.
   - Assertions must not depend on exact Skymill row counts, concrete row values, or population
     totals.
   - Assert stable contracts instead: artifact type, base fields, schema array shape, Mill logical
     type names, row-array/object shape when rows are present, and truncation/completeness metadata.
   - For `describe_sql`, assert rows are absent.
3. Design docs:
   - mark `sql-query.describe_sql` and `sql-query.execute_sql` as shipped in MCP
     inventory.
   - update tool count.
4. Example pointer:
   - note in `misc/examples/ai-mcp-langchain-skymill/README.md` that row execution requires the
     `data-analysis` profile or another profile that includes `sql-query`.

## Out of Scope

- LangChain agent demo that calls `execute_sql`.
- Backlog / milestone updates.
- stdio bridge `testIT` deferred to backlog A-96 / WI-328.
- Chart-mapping scenario tests; those belong to WI-369.
- Exact Skymill fixture row values or row-count assertions.

## Acceptance Criteria

- [x] `./gradlew :ai:mill-ai-mcp-core:test` passes.
- [x] `./gradlew :ai:mill-ai-mcp-transport-http:testIT --tests "*SqlQueryExecution*"` passes.
- [x] MCP tool inventory is accurate.
- [x] `describe_sql` returns schema metadata without row payload.
- [x] `execute_sql` returns the same base fields as `describe_sql` plus row payload.
- [x] HTTP MCP `testIT` uses Skymill and avoids assertions against exact row counts or concrete
      values.
- [x] Docs explain `data-analysis` / `sql-query` exposure vs `schema-exploration`.

## Suggested commit

`[test] WI-341: verify sql-query MCP tools`
