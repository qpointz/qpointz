# WI-259 — mill-ui HTTP-only `queryService`

Status: `done`  
Type: `feature`  
Area: `ui`  
Backlog refs: **U-13**

## Goal

Refactor [`queryService.ts`](../../../../ui/mill-ui/src/services/queryService.ts) to **HTTP only** — no in-memory mock branch, no **`VITE_MILL_QUERY_TRANSPORT`** (or similar) env toggle.

| Method | Endpoint |
|--------|----------|
| `executeQuery(sql)` | `POST /api/v1/query` (+ first page / `DELETE` session — existing flow) |
| `getSavedQueries()` | `GET /api/v1/analysis/queries` |
| `getSavedQueryById(id)` | `GET /api/v1/analysis/queries/{id}` |

All calls use **`fetch`** with **`credentials: 'include'`** and map wire JSON to [`QueryService`](../../../../ui/mill-ui/src/types/query.ts) types.

## Scope

1. **Remove** from `queryService.ts`:
   - `mockQueryService` and imports from [`mockQueries.ts`](../../../../ui/mill-ui/src/data/mockQueries.ts) used only for query execution/catalog.
   - `useHttpExecution` / **`VITE_MILL_QUERY_TRANSPORT`** branching.
2. **Keep** HTTP helpers (`httpExecuteQuery` or equivalent); add `httpGetSavedQueries` / `httpGetSavedQueryById`.
3. Export a single **`queryService`** object implementing all three methods via HTTP.
4. **Vitest:** mock **`global.fetch`** (or `vi.stubGlobal('fetch', …)`) in [`queryService.test.ts`](../../../../ui/mill-ui/src/services/__tests__/queryService.test.ts) — test mapping, error handling, and `404` → `null` for get-by-id; **do not** rely on in-memory mock data in the service module.
5. **Cleanup (same WI or follow-up note in commit):**
   - Delete or shrink [`mockQueries.ts`](../../../../ui/mill-ui/src/data/mockQueries.ts) if nothing else needs execution mocks (`getResultForQuery` can go).
   - **`searchService`** / **`statsService`** still import `mockSavedQueries` today — either switch those to **`GET /api/v1/analysis/queries`** in this WI or leave a short WI note; prefer aligning search/stats query counts with the live catalog when backend is up.
6. Remove **`VITE_MILL_QUERY_TRANSPORT`** from any docs/comments if present; no new query env var.

**Editor UX:** CodeMirror SQL + schema completions is **WI-266** — this WI owns **wire protocol + `queryService` only**.

## Acceptance

- `queryService` has **no mock code path**; Analysis requires reachable **`/api/v1/analysis/**`** and **`/api/v1/query/**`** (dev: Vite proxy to `mill-service`).
- `npm run test` and `npm run build` pass under `ui/mill-ui` (tests use **fetch mocks**, not service mocks).
- Manual smoke against local `mill-service` with **WI-257** + seeded data.

## Depends on

**WI-257** (live saved-query catalog API).

## Notes

- Offline UI-only dev without `mill-service` is **no longer supported** for Analysis — same posture as other real-backed surfaces once wired.
- Chat retains its own **`VITE_CHAT_API=mock`** toggle; query does **not** mirror that pattern.
