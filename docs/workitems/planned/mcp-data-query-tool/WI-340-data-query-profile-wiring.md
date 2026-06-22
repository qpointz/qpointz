# WI-340 — Profile, limits, and mill-service wiring

Status: `planned`  
Type: `✨ feature`  
Area: `ai`, `apps`  
Depends on: [WI-339](WI-339-data-query-capability.md)

## Problem Statement

The `data-query` capability requires data-plane beans (`DataOperationDispatcher`, SQL validator,
`CallerContext`) at MCP invoke time. Execution must not appear in default `schema-exploration` MCP
profile.

## Goal

Wire **`data-query`** into Spring Boot mill-service and introduce **`data-execution`** profile for
MCP and in-process agents that need row-returning SQL.

## In Scope

1. **`DataExecutionAgentProfile`** in `mill-ai`:
   - `id = "data-execution"`
   - `capabilityIds`: `conversation`, `schema`, `metadata`, `sql-dialect`, `sql-query`, `data-query`
   - Register in `DefaultProfileRegistry`
2. **`mill-ai-autoconfigure`**:
   - Extend `SpringCapabilityDependencyAssembler` (or equivalent) to supply `DataQueryExecutionPort`
   - Map MCP HTTP request auth → `CallerContext` for tool invocation (align with existing MCP
     `mcpAgentContext` pattern in `AiV3McpHttpAutoConfiguration`)
3. **Configuration properties** (Java + metadata):
   - `mill.ai.data-query.max-rows-default` (e.g. 1000)
   - `mill.ai.data-query.max-rows-hard` (e.g. 10000)
   - `mill.ai.data-query.timeout` (optional)
4. **`apps/mill-service`**:
   - Document profile group or example: `skymill,ai,mcp` + `mill.ai.mcp.profile=data-execution`
   - Optional `application.yml` snippet under `on-profile: mcp` comment block (no default enablement
     of execution on public MCP without ops choice)
5. **Profile tests** (`ProfileRegistryTest`, `ProfileCapabilityMatrixTest`):
   - `data-execution` includes `data-query.execute_sql` tool name when capability materialized

## Out of Scope

- Changing default `mill.ai.mcp.profile` from `schema-exploration` globally
- Production RBAC beyond existing `/services/**` + admission stub
- BACKLOG updates

## Acceptance Criteria

- [ ] mill-service boots with `data-execution` + `mcp` profiles; MCP lists `data-query.execute_sql`
- [ ] `schema-exploration` MCP profile does **not** list `data-query.*`
- [ ] Configuration metadata generated for new `mill.ai.data-query.*` keys

## Suggested commit

`[feat] WI-340: data-execution profile and data-query Spring wiring`
