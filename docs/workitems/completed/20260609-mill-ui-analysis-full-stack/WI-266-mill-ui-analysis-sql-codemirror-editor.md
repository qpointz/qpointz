# WI-266 — mill-ui Analysis: CodeMirror SQL editor + schema hints

Status: `planned`  
Type: `feature`  
Area: `ui`  
Backlog refs: **U-13**

## Goal

Replace the Analysis **plain `Textarea`** in [`QueryEditor.tsx`](../../../../ui/mill-ui/src/components/queries/QueryEditor.tsx) with a **CodeMirror 6** SQL surface: syntax highlighting, sensible keyboard behaviour (preserve **Ctrl/Cmd+Enter** to run when **`analysisExecuteQuery`** is enabled), and **schema-aware** completion aligned with the Model view catalog.

**Do not** add Monaco, `monaco-editor`, or `@monaco-editor/react`.

## Scope

1. **CodeMirror integration** in `ui/mill-ui`:
   - Reuse [`@uiw/react-codemirror`](../../../../ui/mill-ui/package.json) and patterns from [`SyntaxCodeEditor.tsx`](../../../../ui/mill-ui/src/components/common/SyntaxCodeEditor.tsx).
   - Add **`@codemirror/lang-sql`** for highlighting.
   - Enable **`@codemirror/autocomplete`** for schema-driven suggestions.
   - New **`SqlCodeEditor`** (preferred) or extend `SyntaxCodeEditor` with `language: 'sql'`; keep **`QueryEditor`** as toolbar + execute/format/copy wiring.
   - **No Vite worker** configuration.
2. **Theming:** `useMantineColorScheme` → CodeMirror `light` / `dark` (match `SyntaxCodeEditor`).
3. **Schema completions:**
   - Load tree via [`schemaService.getTree`](../../../../ui/mill-ui/src/services/schemaService.ts) with same context strategy as Model explorer (`getContext` / `selectedContext` — not hard-coded `global` unless product rules require it).
   - Suggest **schemas**, **`schema.table`** qualified names, **columns** best-effort via batched [`getTable`](../../../../ui/mill-ui/src/services/schemaService.ts); cap breadth for large catalogs.
   - Pure helper module (e.g. `schemaCompletionIndex.ts`) for tree → completion entries — **Vitest** without mounting CodeMirror.
4. **Format:** keep existing **`sql-formatter`** toolbar action; no SQL parse linter in this WI.
5. **Docs:** update [`ARCHITECTURE.md`](../../../../ui/mill-ui/docs/ARCHITECTURE.md) / [`UI-ELEMENT-INVENTORY.md`](../../../../ui/mill-ui/docs/UI-ELEMENT-INVENTORY.md) when component layout stabilises.

## Acceptance

- Main SQL editing surface is CodeMirror (not `Textarea`).
- **Ctrl/Cmd+Enter** triggers execute when feature flag allows.
- With reachable schema API, suggestions include **schemas** and **qualified tables**; columns best-effort.
- No regression to **WI-259** / **`queryService`** execution or catalog wiring.
- `npm run test` and `npm run build` pass; no `monaco-editor` in `package.json`.

## Depends on

**WI-259** (Analysis query service wired; editor ships in same delivery wave unless team splits UI WIs).

## Notes

- **Export:** `/services/export/**` — not this WI.
- **Paged results / grid:** `/api/v1/query/**` — not this WI.
