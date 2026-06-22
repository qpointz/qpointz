# MCP data-query tool (`execute_sql`)

**Status:** `planned`  
**Depends on:** [`../completed/20260622-ai-v3-mcp-server-poc/STORY.md`](../completed/20260622-ai-v3-mcp-server-poc/STORY.md) (HTTP MCP transport + core catalog/executor — WI-327, WI-329)

## Goal

Expose **read-only SQL execution** to external MCP clients (Cursor, LangChain, other agents) as a
**capability tool** — not a bespoke Jet/MCP adapter. A new **`data-query`** capability provides
**`execute_sql`**, delegating to the existing Mill **data plane** (`DataOperationDispatcher` / Jet
execution path). The tool appears automatically on **`/services/mcp`** via the v3 MCP projection stack.

This closes the gap where MCP today stops at **schema exploration** + **`sql-query.validate_sql`**
(generate/validate only) and cannot return query rows.

**Design alignment:**

- [P-10 / platform MCP](../../../design/platform/mcp.md) — `query_sql` as a **tool** (tabular result), not a live-data **resource**
- [v3 MCP capability exposure](../../../design/agentic/v3-mcp-capability-exposure.md) — capabilities-first; no parallel MCP catalog
- [`sql-query`](../../../ai/mill-ai/src/main/resources/capabilities/sql-query.yaml) — remains **validate + generated-SQL artefacts**; execution is a **separate** capability

## Architectural principle (locked)

| Rule | Detail |
|------|--------|
| **Capability, not adapter** | Implement `DataQueryCapability` + `capabilities/data-query.yaml` in `mill-ai`; MCP inherits via `CapabilityMcpCatalog` |
| **Tool, not resource** | `data-query.execute_sql` — parameterized action returning `TabularResult`-shaped JSON |
| **No Jet MCP servlet** | Reuse in-process `DataOperationDispatcher` (same backend as `/services/jet`) |
| **Read-only POC** | SELECT-only enforcement via existing SQL validation / engine policy; no DML/DDL over MCP |
| **Limits** | `max_rows` cap (server default + hard max); truncate + `has_more` in response |
| **Profile-gated** | New agent profile **`data-execution`** (or extend server MCP profile) — not enabled in default `schema-exploration` |
| **Security** | Same `/services/**` auth as MCP + Jet; `CallerContext` from MCP transport; admission gate hook (A-79 alignment) |
| **Separate from `validate_sql`** | Agents may use schema → validate → execute as three steps; `data-analysis` profile unchanged (validate only) |

## MCP surface (target)

| MCP tool | Kind | Input (summary) | Output (summary) |
|----------|------|-------------------|------------------|
| `data-query.execute_sql` | QUERY | `sql`, optional `max_rows`, optional `dialect` | Columns + rows (bounded), `truncated`, `row_count` |

Optional follow-up (out of scope for this story): `data-query.explain_sql`, session paging via
`/api/v1/query/**` (`executionId` + `pageIndex`).

## Module touchpoints

| Area | Change |
|------|--------|
| `ai/mill-ai` | `data-query` capability provider, manifest, tool handlers |
| `ai/mill-ai-autoconfigure` | `DataOperationDispatcher` / validator beans wired into capability dependencies |
| `ai/mill-ai-mcp-core` | Catalog + executor tests for new tool (no core API change expected) |
| `ai/mill-ai-mcp-transport-http` | `testIT` smoke: `execute_sql` returns rows against Skymill fixture |
| `apps/mill-service` | Profile `data-execution` + `mill.ai.mcp.profile` documentation |
| `docs/design/agentic/` | Extend MCP exposure doc §15 tool inventory |

## Prerequisites

- MCP HTTP backend on mill-service ([WI-329](../completed/20260622-ai-v3-mcp-server-poc/WI-329-mill-ai-mcp-transport-http.md))
- `mill-ai-mcp-core` catalog-scoped executor ([WI-327](../completed/20260622-ai-v3-mcp-server-poc/WI-327-mill-ai-mcp-core.md))
- Skymill (or equivalent) data backend on mill-service for `testIT`

## Out of scope

- Wrapping `/services/jet` or `/api/v1/query/**` as a **standalone MCP resource** URI scheme
- SQL **generation** (stays in `sql-query` + chat runtime)
- DML/DDL, mutations, or CAPTURE tools
- Full platform MCP spec (`res://samples`, profiling, Substrait) — only `execute_sql` MVP
- stdio bridge changes (transparent once HTTP backend exposes the tool)
- BACKLOG row creation (story is **planned** only until user requests backlog promotion)

## Work item order

| Seq | WI | Rationale |
|-----|-----|-----------|
| 1 | WI-338 | Design contract + profile/limits before code |
| 2 | WI-339 | `data-query` capability + manifest + handler |
| 3 | WI-340 | Spring wiring, `data-execution` profile, mill-service MCP config |
| 4 | WI-341 | Unit + MCP `testIT` coverage |

## Work Items

- [ ] WI-338 — Data-query MCP tool design (`WI-338-mcp-data-query-design.md`)
- [ ] WI-339 — `data-query` capability + `execute_sql` tool (`WI-339-data-query-capability.md`)
- [ ] WI-340 — Profile, limits, and mill-service wiring (`WI-340-data-query-profile-wiring.md`)
- [ ] WI-341 — Tests and tool inventory docs (`WI-341-data-query-mcp-tests.md`)

## Verify (full story — before MR)

```bash
./gradlew :ai:mill-ai:test --tests "*DataQuery*"
./gradlew :ai:mill-ai-mcp-core:test
./gradlew :ai:mill-ai-mcp-transport-http:testIT --tests "*DataQuery*"
```

## Related

- MCP POC story: [`../completed/20260622-ai-v3-mcp-server-poc/STORY.md`](../completed/20260622-ai-v3-mcp-server-poc/STORY.md)
- Platform data MCP spec: [`docs/design/platform/mcp.md`](../../../design/platform/mcp.md)
- Query result service (optional future paging): [`docs/design/platform/query-result-execution-service.md`](../../../design/platform/query-result-execution-service.md)
