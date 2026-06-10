# WI-252 — `ExportVectorBlockSource` (`mill-data-backend-core`)

Status: `planned`  
Type: `feature`  
Area: `data`  
Backlog refs: **P-36**

## Goal

Implement **query / table execution that ends at `VectorBlockIterator`** in [`mill-data-backend-core`](../../../../data/mill-data-backend-core) — **no** format or `OutputStream` awareness. This keeps **plan building**, **`DataOperationDispatcher.execute`**, and **SQL parse** on the data plane where [`PlanHelper`](../../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/dispatchers/PlanHelper.java) and [`DataOperationDispatcher`](../../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/dispatchers/DataOperationDispatcher.java) already live.

**Suggested type name:** `ExportVectorBlockSource` (or equivalent; avoid `Mill` prefix per repo rule).

## Scope

1. **Table scan:** build **`QueryRequest`** with **`setPlan(proto)`** only — **no `SQLStatement`** — via `PlanHelper.createNamedScan` + `createPlan` + `SubstraitDispatcher.planToProto`, then `dispatcher.execute(request)`.
2. **Ad-hoc SQL:** build **`QueryRequest`** with **`SQLStatement`** / parse path (same as today’s SQL execution) → `dispatcher.execute(request)`.
3. Apply shared **`QueryExecutionConfig`** (fetch size, limits) suitable for export; surface clear errors for unknown schema/table or parse failures.
4. **Spring-free** implementation in backend-core (constructor-injected `DataOperationDispatcher`, `PlanHelper`, `SubstraitDispatcher` or factories available in that module). Optional **Spring `@Bean`** adapter can live in `mill-data-autoconfigure` in a follow-on if needed.

**Placement:** **`mill-data-backend-core`** is the right module (dispatcher, plan, schema already there). **Not** `mill-data-source-core` — that layer is formats/sources, not Jet execution.

## Acceptance

- Unit or integration test: table path returns an iterator for a fixture **without** constructing `SELECT *` as a SQL string.
- API does **not** depend on export format types or HTTP.

## Depends on

None for **core** types (can parallel **WI-250**); **WI-253** consumes this from **`ExportFacility`**.
