# WI-257 — REST `/api/v1/queries` (list, get, execute)

Status: `planned`  
Type: `feature`  
Area: `services`  
Backlog refs: **U-13**

## Goal

Implement HTTP handlers per [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md#domain-queries-analysis):

- `GET /api/v1/queries`
- `GET /api/v1/queries/{queryId}`
- `POST /api/v1/queries/execute` (JSON body with `sql`)

## Scope

1. **`execute`**: build **`QueryRequest`** from SQL, **[`DataOperationDispatcher.execute`](../../../../data/mill-data-backend-core/src/main/java/io/qpointz/mill/data/backend/dispatchers/DataOperationDispatcher.java)**, materialize columns/rows for JSON grid (limits, error mapping with `error` + `code` where feasible).
2. **List/get**: read from **WI-256** repository.
3. **Not** AI-gated — conditional on **`DataOperationDispatcher`** bean (see **WI-258**).

## Acceptance

- Contract matches documented JSON (allow additive fields only with doc update).
- Happy path + SQL error path tested.

## Depends on

**WI-258** (module hosts controllers/services).

## Notes

Implementation split with **WI-258** is flexible: either **WI-258** lands an empty module first or **WI-257** ships in the same commit as the first controller — honour **RULES.md** one-commit-per-WI with a sensible slice.