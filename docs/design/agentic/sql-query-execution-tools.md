# sql-query execution and schema-probe tools

**Status:** Shipped with `ai-chart-mapping` Stage 1 (WI-338–WI-341)
**Audience:** Agent and MCP developers extending `sql-query` or consuming result schema for chart mapping
**Modules:** `ai/mill-ai`, `ai/mill-ai-autoconfigure`, `data/mill-data-query`

Related:

| Document | Role |
|----------|------|
| [`chart-artifact-contract.md`](./chart-artifact-contract.md) | Chart orchestration; schema provenance via `sql-query.describe_sql` |
| [`v3-mcp-capability-exposure.md`](./v3-mcp-capability-exposure.md) | MCP tool inventory and exposure rules |
| [`../platform/query-result-execution-service.md`](../platform/query-result-execution-service.md) | Query-result session engine reused by execution tools |

---

## 1. Purpose

`sql-query` already validates SQL and emits `generated-sql` artifacts. Stage 1 extends the same
capability with:

| Tool | MCP name | Purpose |
|------|----------|---------|
| `describe_sql` | `sql-query.describe_sql` | Result schema metadata for chart mapping and clients |
| `execute_sql` | `sql-query.execute_sql` | Bounded row execution on already validated SQL |

There is **no** separate `data-query` capability. Validation, generated SQL, schema probe, and
bounded execution share one `sql-query` contract.

---

## 2. Orchestration rules

```text
reasoning / SQL selection
  -> sql-query.validate_sql (orchestrated flows)
  -> sql-query.describe_sql  (schema probe, maxRows = 1 internally)
  -> chart-mapping.validate_chart_spec (later story stages)
```

External MCP agents on **`data-analysis`** use the same tool chain; see
[`charts/chart-mcp-exposure.md`](./charts/chart-mcp-exposure.md) (Gap 20).

| Rule | Detail |
|------|--------|
| Validation prerequisite | Orchestration must call `validate_sql` before `describe_sql` / `execute_sql` in agent flows. Execution handlers do **not** invoke `validate_sql` internally. |
| No SQL generation | `describe_sql` / `execute_sql` consume SQL; they do not generate, rewrite, or semantically validate SQL. |
| Blank input | Handlers reject blank SQL at the tool boundary. |
| Backend errors | Dispatcher/query-plane failures surface as structured execution errors (`QUERY_FAILED`, etc.). |

---

## 3. Shared base envelope

Both tools return the same base fields:

| Field | Type | Notes |
|-------|------|-------|
| `sql` | string | Echo of input SQL |
| `dialectId` | string | Default `CALCITE` when dialect omitted |
| `schema` | array | `[{ name, type, nullable, nativeType? }]` — Mill `LogicalDataTypeId` names |
| `warnings` | array | Optional strings |
| `source` | object | `{ "kind": "execution", "maxRows"?: number }` |

`describe_sql` uses `artifactType: "sql-description"` and `source.maxRows: 1`.
`execute_sql` uses `artifactType: "sql-result"` and `source.kind: "execution"`.

`nativeType` is optional adapter metadata; omit when unmapped. Validators must not require it.

---

## 4. `describe_sql`

**Input:** `sql` (required), `dialect` (optional).

**Output:** base envelope only — **no `rows`**.

MVP implementation executes through `QueryResultExecutionService` with `firstPageSize = 1`, maps
`columnSchema`, discards row data, deletes the session. Implementation lives behind
`SqlQueryExecutionPort` / replaceable describe strategy.

---

## 5. `execute_sql`

**Input:**

| Field | Required | Default |
|-------|----------|---------|
| `sql` | yes | — |
| `resultMode` | no | **`paged`** |
| `max_rows` | no | server default (capped by hard limit) |
| `dialect` | no | `CALCITE` |
| `pageIndex` / `pageSize` | no | first page when `paged` |

**Output:** base envelope plus:

| Field | Meaning |
|-------|---------|
| `rows` | JSON array of row objects keyed by column name |
| `resultMode` | `paged` or `full` |
| `rowCount` | rows returned in this response |
| `truncated` | capped by caller/server limit |
| `hasMore` | more rows may exist beyond response |
| `totalResult` | total when known |
| `limit` | effective row limit |

`full` mode accumulates bounded pages through the shared query-result plane (never unbounded).

---

## 6. Profiles and MCP exposure

| Profile | `sql-query` tools |
|---------|-------------------|
| `data-analysis` | `validate_sql`, `describe_sql`, `execute_sql` |
| `schema-exploration` | none (`sql-query` not included) |
| `hello-world` | none |

MCP clients needing SQL validation or execution use a profile that includes `sql-query` (typically
`data-analysis`). Default MCP profile `schema-exploration` remains read-only.

Configuration: `mill.ai.sql-query.execution.max-rows-default`, `max-rows-hard`, optional `timeout`.

---

## 7. Backend wiring

`SqlQueryExecutionPort` in `mill-ai` (Spring-free) adapts to `QueryResultExecutionService` in
`mill-ai-autoconfigure`. Caller identity maps to `CallerContext` using the same tenant semantics as
`mill-data-query-service`.

Do not call `/api/v1/query` over HTTP internally; do not bypass the query-result engine with direct
`DataOperationDispatcher` usage.
