# WI-259 — mill-ui `realQueryService` + env toggle

Status: `planned`  
Type: `feature`  
Area: `ui`  
Backlog refs: **U-13**

## Goal

Implement **`realQueryService`** in [`queryService.ts`](../../../../ui/mill-ui/src/services/queryService.ts): **`fetch`** with **`credentials: 'include'`**, map wire JSON to [`QueryService`](../../../../ui/mill-ui/src/types/query.ts). Select mock vs real via env (mirror **[`isRestChatBackendActive`](../../../../ui/mill-ui/src/services/chatService.ts)** pattern, e.g. `VITE_QUERY_API=mock`).

## Scope

1. Update [`api.ts`](../../../../ui/mill-ui/src/services/api.ts) export if barrel changes.
2. Vitest: service layer tests with `fetch` mock.
3. Document env vars in mill-ui README or env sample file if present.

**Editor UX:** advanced SQL editing (Monaco, schema completions) is **WI-266** — this WI owns **wire protocol + `queryService` only**.

## Acceptance

- `npm run test` and `npm run build` pass under `ui/mill-ui`.
- Manual smoke against local `mill-service` with **WI-257** deployed.

## Depends on

**WI-257** (live saved-query catalog API).
