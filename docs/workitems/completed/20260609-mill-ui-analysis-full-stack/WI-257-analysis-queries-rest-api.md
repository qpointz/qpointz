# WI-257 — REST `/api/v1/queries` (list, get)

Status: `planned`  
Type: `feature`  
Area: `services`  
Backlog refs: **U-13**

## Goal

Implement HTTP handlers per [`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md#domain-queries-analysis):

- `GET /api/v1/queries`
- `GET /api/v1/queries/{queryId}`

**Ad-hoc SQL execution** is **not** in this WI — it is **`/api/v1/query/**`** (story **`query-result-execution-service`**). Do **not** add **`POST /api/v1/queries/execute`**.

## Scope

1. **List/get:** read from **WI-256** `SavedQueryCatalog` port; map to wire JSON (`createdAt` / `updatedAt` as epoch ms; optional `tags` as string array).
2. **Activation:** conditional on **`DataOperationDispatcher`** bean (see **WI-258**). **Not** AI-gated.
3. **Errors:** `404` for unknown `queryId`; empty catalog → `{ "queries": [] }` (not an error).

**Execution:** clients use **`POST /api/v1/query`** and related session routes (see [`../../completed/20260511-query-result-execution-service/STORY.md`](../../completed/20260511-query-result-execution-service/STORY.md)).

## Acceptance

- Response JSON matches documented `SavedQuery` shape (additive fields only with doc update).
- **Happy path:** list returns seeded queries; get-by-id returns one object.
- **Catalog error paths (no SQL execution tests in this WI):**
  - Unknown id → **`404`**
  - Empty catalog (if tested without seeds) → `{ "queries": [] }`
  - When `mill.security.enable=true`, unauthenticated access follows same **`/api/**`** rules as other REST surfaces (mirror query-result IT auth pattern).
- Controller/service unit tests cover mapping and `404`.

## Depends on

**WI-258** (module hosts controllers/services).

## Notes

Implementation split with **WI-258** is flexible: either **WI-258** lands module shell first or **WI-257** ships controllers in the same commit — honour **RULES.md** one-commit-per-WI with a sensible slice.
