# WI-257 — REST `/api/v1/queries` (list, get)

Status: `planned`  
Type: `feature`  
Area: `services`  
Backlog refs: **U-13**

## Goal

Implement HTTP handlers per [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md#domain-queries-analysis):

- `GET /api/v1/queries`
- `GET /api/v1/queries/{queryId}`

**Ad-hoc SQL execution** is **not** in this WI — it is **`/api/v1/query/**`** (story **`query-result-execution-service`**, **D-8**). Do **not** add **`POST /api/v1/queries/execute`**.

## Scope

1. **List/get**: read from **WI-256** repository.
2. **Not** AI-gated — conditional on **`DataOperationDispatcher`** bean (see **WI-258**).

**Execution:** clients use **`POST /api/v1/query`** and related session routes (see [`../../in-progress/query-result-execution-service/STORY.md`](../../in-progress/query-result-execution-service/STORY.md)).

## Acceptance

- Contract matches documented JSON (allow additive fields only with doc update).
- Happy path + SQL error path tested.

## Depends on

**WI-258** (module hosts controllers/services).

## Notes

Implementation split with **WI-258** is flexible: either **WI-258** lands an empty module first or **WI-257** ships in the same commit as the first controller — honour **RULES.md** one-commit-per-WI with a sensible slice.