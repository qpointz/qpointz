# WI-266 — mill-ui Analysis: Monaco SQL editor + schema hints

Status: `planned`  
Type: `feature`  
Area: `ui`  
Backlog refs: **U-13**

## Goal

Replace the Analysis **plain `Textarea`** in [`QueryEditor.tsx`](../../../../ui/mill-ui/src/components/queries/QueryEditor.tsx) with a **Monaco**-based SQL surface: syntax highlighting, sensible keyboard behaviour (preserve **Ctrl/Cmd+Enter** to run when **`analysisExecuteQuery`** is enabled), and **schema-aware** completion/hints aligned with the same catalog the Model view uses.

## Scope

1. **Monaco integration** in `ui/mill-ui`: dependency choice (`@monaco-editor/react` + `monaco-editor`), **Vite worker** configuration so the editor loads reliably in dev and production builds (see existing patterns; do not regress `npm run build` / `npm run test`).
2. **Theming** consistent with Mantine light/dark (`useMantineColorScheme`).
3. **Schema catalog for completions**
   - Load tree via [`schemaService.getTree`](../../../../ui/mill-ui/src/services/schemaService.ts) using the same **scope / context** strategy as the Model explorer (e.g. resolve `selectedContext` from [`getContext`](../../../../ui/mill-ui/src/services/schemaService.ts), not a hard-coded `global` unless product rules say otherwise).
   - Offer **schemas**, **`schema.table`** qualified names, and **columns** where practical (optional batched [`getTable`](../../../../ui/mill-ui/src/services/schemaService.ts) to fill column names; cap breadth for large catalogs and document limits).
4. **Dialect / intelligence library (evaluate, pick one path in implementation)**
   - **Baseline:** Monaco built-in `sql` language + **custom** `registerCompletionItemProvider` for catalog + keywords.
   - **Optional upgrade:** [`monaco-sql-languages`](https://github.com/DTStack/monaco-sql-languages) + [`dt-sql-parser`](https://github.com/DTStack/dt-sql-parser) for richer validation/context — **note:** upstream README targets an older **monaco-editor** line; verify compatibility with the version Mill pins before committing; each bundled dialect is **separate** Monarch + parser wiring (not a single runtime “dialect JSON” — see assessment in story **STORY** / design note if captured).
5. **Tests:** Vitest where valuable (e.g. pure helpers for flattening schema tree → completion index); avoid brittle full Monaco E2E unless already patterned in repo.
6. **Docs:** mill-ui README or env sample — document any **`VITE_`** flags introduced for editor behaviour (if any); update [`ARCHITECTURE.md`](../../../../ui/mill-ui/docs/ARCHITECTURE.md) or [`UI-ELEMENT-INVENTORY.md`](../../../../ui/mill-ui/docs/UI-ELEMENT-INVENTORY.md) when the editor component name/layout stabilises.

## Acceptance

- Analysis SQL editing uses Monaco (no regression to mock-only **query execution** — that remains **WI-259** / **`queryService`**).
- **Ctrl/Cmd+Enter** still triggers execute when the feature flag allows it.
- With a reachable schema API, **suggestions** include at least **schemas** and **qualified tables**; column hints **best-effort** per scope note above.
- `npm run test` and `npm run build` pass under `ui/mill-ui`.

## Depends on

**WI-259** (Analysis stack wired to real/mock **query** API; editor ships as part of the same Analysis delivery wave — reorder only if team explicitly splits UI WIs).

## Notes

- **Export** of result sets stays on **`/services/export/**`** ([`completed/20260507-streaming-export-service`](../../completed/20260507-streaming-export-service/STORY.md), optional **WI-255**); **not** in this WI.
- **Paged results / grid** stay on **`/api/v1/query/**`** ([`completed/20260511-query-result-execution-service`](../../completed/20260511-query-result-execution-service/STORY.md)); **not** in this WI.
