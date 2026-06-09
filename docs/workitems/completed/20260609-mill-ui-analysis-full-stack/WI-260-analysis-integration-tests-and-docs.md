# WI-260 — Analysis integration tests + docs sync

Status: `planned`  
Type: `test`, `docs`  
Area: `services`, `ui`  
Backlog refs: **U-13**

## Goal

Add **testIT** (or full-stack slice) covering **queries REST** + persistence, and sync docs if behaviour or fields diverged during implementation. Include **Analysis CodeMirror SQL editor** notes in mill-ui **ARCHITECTURE** / inventory if **WI-266** changed filenames or integration points.

## Scope

1. **Backend testIT** per [`CLAUDE.md`](../../../../CLAUDE.md):
   - Flyway V8 applies; seeded saved queries readable.
   - `GET /api/v1/queries` returns list; `GET /api/v1/queries/{id}` happy path + **`404`**.
   - Auth behaviour when `mill.security.enable=true` (mirror query-result IT pattern).
2. **UI:** confirm `queryService` Vitest coverage from **WI-259** remains green in CI.
3. Sync **[`BACKEND-API-REQUIREMENTS.md`](../../../design/ui/mill-ui/BACKEND-API-REQUIREMENTS.md)** and mill-ui service docs (HTTP-only `queryService`; remove any mock/toggle references) if needed.
4. Story closure per [`RULES.md`](../../RULES.md): **MILESTONE**, **BACKLOG U-13** → `done`, archive story folder.

## Acceptance

- CI green for new integration tests.
- No undocumented breaking changes vs published UI API contract.
- Docs reflect CodeMirror editor (not Monaco) and HTTP-only `queryService` (no query mock env var).

## Depends on

**WI-256**, **WI-257**, **WI-258**, **WI-259**, **WI-266**
