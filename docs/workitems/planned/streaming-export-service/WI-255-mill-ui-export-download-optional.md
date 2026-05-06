# WI-255 — Optional: mill-ui export via `/services/export`

Status: `planned`  
Type: `improvement`  
Area: `ui`  
Backlog refs: **P-36**, optional **U-14** if added

## Goal

Replace or augment **client-side** export in [`QueryResults.tsx`](../../../../ui/mill-ui/src/components/queries/QueryResults.tsx) (tray **`HiOutlineArrowDownTray`**) with **`POST /services/export/sql`** using the **current SQL** from Analysis context, or document deferral.

## Scope

1. Pass **credentials** (`fetch` with `credentials: 'include'`) consistent with [`chatService`](../../../../ui/mill-ui/src/services/chatService.ts).
2. Handle **blob download** and filename from **`Content-Disposition`**.
3. Feature flag or env gate if partial rollout is needed.

## Acceptance

- Manual smoke: export CSV/JSON from Analysis after **WI-253** is available.
- **Vitest** for client helper if non-trivial.

## Depends on

**WI-253** (minimum); **WI-254** recommended for stable contract.
