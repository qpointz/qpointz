# WI-341 — Data-query MCP tests and docs

Status: `planned`  
Type: `🧪 test`  
Area: `ai`  
Depends on: [WI-340](WI-340-data-query-profile-wiring.md)

## Problem Statement

Without automated proof, `execute_sql` could regress on exposure filters, auth, or row limits. Tool
inventory docs would drift from manifests.

## Goal

Add **unit** and **HTTP MCP `testIT`** coverage for `data-query.execute_sql`, and finalize design
doc tool inventory.

## In Scope

1. **`mill-ai-mcp-core` unit tests:**
   - `CapabilityMcpCatalogTest`: `data-execution` profile includes `data-query.execute_sql`;
     `schema-exploration` excludes it
   - `CapabilityMcpExecutorTest`: mock `DataQueryExecutionPort` — happy path + limit exceeded
2. **`mill-ai-mcp-transport-http` `testIT`:**
   - `@SpringBootTest` with `mill.ai.mcp.profile=data-execution`, Skymill or minimal data fixture
   - `shouldListTools_includingDataQueryExecuteSql`
   - `shouldExecuteSql_andReturnRows` — e.g. `SELECT COUNT(*) FROM ...` or `list_schemas` equivalent
     via SQL against Skymill
   - `shouldRequireAuthentication_whenSecurityEnabled` (reuse pattern from `HttpMcpTransportSecurityIT`)
   - `shouldRejectExecuteSql_outsideProfile` when profile is `schema-exploration`
3. **Design doc:** mark `data-query.execute_sql` as **shipped** in §15 inventory; update tool count
4. **Example pointer** (no new example required): note in
   [`misc/examples/ai-mcp-langchain-skymill/README.md`](../../../misc/examples/ai-mcp-langchain-skymill/README.md)
   that row execution requires `data-execution` profile (one paragraph)

## Out of Scope

- LangChain agent demo that calls `execute_sql` (manual follow-up)
- BACKLOG / MILESTONE updates (story closure only)
- stdio bridge `testIT` deferred to backlog A-96 / WI-328 (HTTP fixture reusable when bridge lands)

## Acceptance Criteria

- [ ] `./gradlew :ai:mill-ai-mcp-core:test` and `:ai:mill-ai-mcp-transport-http:testIT` pass
- [ ] §15 tool inventory accurate (24 tools or updated total)
- [ ] README mentions `data-execution` vs `schema-exploration`

## Suggested commit

`[test] WI-341: MCP execute_sql tests and inventory docs`
