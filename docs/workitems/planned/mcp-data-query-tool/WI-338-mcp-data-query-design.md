# WI-338 — Data-query MCP tool design

Status: `planned`  
Type: `📋 design`  
Area: `ai`  
Depends on: [WI-329](../../completed/20260622-ai-v3-mcp-server-poc/WI-329-mill-ai-mcp-transport-http.md) (HTTP MCP backend)

## Problem Statement

MCP clients can list Skymill schema and validate SQL (`sql-query.validate_sql`) but cannot **execute**
queries and receive rows. Users expect Cursor/LangChain to run analytics after schema discovery.
Wrapping Jet as a one-off MCP resource would bypass the v3 capability model and duplicate security
and limits.

## Goal

Document the **`data-query`** capability and **`execute_sql`** MCP tool contract before implementation:
inputs, tabular output shape, row limits, profile gating, and delegation to the data plane.

## In Scope

1. **Design doc updates** under `docs/design/agentic/`:
   - Extend [`v3-mcp-capability-exposure.md`](../../../design/agentic/v3-mcp-capability-exposure.md):
     - New § or subsection for **data execution** vs **SQL generation**
     - §15 tool inventory row: `data-query` / `execute_sql`
     - Clarify: MCP **resources** remain descriptor metadata; **query results are tool output**
   - Short addendum or section in [`platform/mcp.md`](../../../design/platform/mcp.md) mapping
     `query_sql` → `data-query.execute_sql` (Mill naming)
2. **Tool contract** (normative for WI-339):
   - **Name:** `execute_sql` → MCP `data-query.execute_sql`
   - **Input:** `sql` (required), `max_rows` (optional int, server-capped), `dialect` (optional; default from server)
   - **Output:** JSON aligned with platform `TabularResult` subset:
     - `schema`: `[{ name, type, nullable }]`
     - `rows`: array of row arrays or row objects (pick one; document)
     - `row_count`, `truncated`, optional `has_more` (false for MVP single page)
   - **Errors:** structured pass-through (`QUERY_FAILED`, `UNAUTHORIZED`, row limit exceeded)
3. **Profile:** introduce **`data-execution`** agent profile:
   - `capabilityIds`: `conversation`, `schema`, `metadata`, `sql-dialect`, `sql-query`, **`data-query`**
   - MCP server config: `mill.ai.mcp.profile=data-execution` for clients that need execution
4. **Security / limits table:** default `max_rows` (e.g. 1000), hard max (e.g. 10000), timeout,
   read-only policy, same auth as `/services/**`
5. **Sequence diagram:** MCP client → `tools/call execute_sql` → `CapabilityMcpExecutor` →
   `DataQueryCapability` → `DataOperationDispatcher`

## Out of Scope

- Implementation code (WI-339+)
- BACKLOG.md row
- Query session REST (`/api/v1/query/**`) paging over MCP

## Acceptance Criteria

- [ ] Design docs merged with tool contract and profile definition
- [ ] §15 tool inventory includes `data-query.execute_sql` (marked **planned** until WI-341)
- [ ] Explicit statement: **`validate_sql` does not execute**; **`execute_sql` does not generate**

## Suggested commit

`[docs] WI-338: data-query execute_sql MCP tool design`
