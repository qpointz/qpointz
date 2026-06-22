# WI-339 — `data-query` capability + `execute_sql` tool

Status: `planned`  
Type: `✨ feature`  
Area: `ai`  
Depends on: [WI-338](WI-338-mcp-data-query-design.md)

## Problem Statement

There is no `CapabilityProvider` that executes SQL and returns tabular data for tool invocation.
`sql-query` only validates and emits generated-SQL artefacts for host-side execution.

## Goal

Add **`data-query`** to `mill-ai`: manifest, provider, and handler for **`execute_sql`** that
delegates to the Mill data plane.

## In Scope

1. **Manifest** `ai/mill-ai/src/main/resources/capabilities/data-query.yaml`:
   - `mcp.enabled: true` (default)
   - Tool `execute_sql` with `kind: query` and JSON schemas per WI-338
   - System prompt optional (execution safety, read-only, row limits)
2. **`DataQueryCapability`** (`CapabilityProvider`):
   - `ServiceLoader` registration in `META-INF/services/...CapabilityProvider`
   - Handler invokes `DataOperationDispatcher` (or thin port abstraction) with SQL from tool args
   - Maps engine result to tabular JSON (reuse patterns from `SqlQueryToolHandlers.SqlResultReferenceArtifact`
     where applicable; **include inline rows** for MCP MVP, not only `resultId`)
3. **Dependency port** in `mill-ai` (if needed):
   - e.g. `DataQueryExecutionPort` implemented in autoconfigure using existing dispatcher
   - Keeps `mill-ai` free of Spring; implementation in `mill-ai-autoconfigure` or `mill-ai-data`
4. **Guards:**
   - Enforce server `max_rows` / hard cap
   - Reject empty SQL; surface validator/dispatcher errors as tool error content
5. **Unit tests** (`mill-ai`):
   - Handler with mock port: happy path, truncation, validation failure
   - Manifest loads; tool registered on materialized capability

## Out of Scope

- MCP transport tests (WI-341)
- `mill-service` profile YAML (WI-340)
- `explain_sql`, Substrait, query sessions

## Acceptance Criteria

- [ ] `./gradlew :ai:mill-ai:test --tests "*DataQuery*"` passes
- [ ] `CapabilityRegistry` discovers `data-query`; manifest declares `execute_sql`
- [ ] Materialized capability invokes handler without LangChain4j agent loop

## Suggested commit

`[feat] WI-339: data-query capability with execute_sql tool`
