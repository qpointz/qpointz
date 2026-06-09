# WI-270 - mill-ui Analysis mode selection and routing plan

Status: `planned`
Type: `feature`, `docs`
Area: `ui`
Backlog refs: **U-14**

## Goal

Plan the Mill UI changes that make SQL Analysis and Visual Analysis available from the same Analysis
area while preserving the current SQL workflow.

## Scope

1. Add create flow:
   - `New SQL Analysis`
   - `New Visual Analysis`
2. Add catalog/list behavior:
   - combined list with type badges, search, tags, updated time.
   - optional filters for SQL / Visual.
3. Define routing:
   - `/analysis` catalog or default landing.
   - `/analysis/sql/:analysisId` for SQL Analysis.
   - `/analysis/visual/:analysisId` for Visual Analysis.
   - Evaluate whether `/analysis/:analysisId` can resolve by `type` after fetch.
4. Preserve current SQL editor:
   - CodeMirror SQL editor.
   - execute/copy/format/clear controls.
   - results grid and export.
   - inline chat context behavior.
5. Define Visual Analysis shell:
   - path list/sidebar.
   - board stack.
   - board configuration panel.
   - preview table panel.
   - top-level parameters panel.
6. Feature flags:
   - `viewAnalysis` continues to gate the whole area.
   - add `analysisVisualMode` or equivalent for staged rollout.
7. Documentation updates:
   - `docs/design/ui/mill-ui/ARCHITECTURE.md`
   - `docs/design/ui/mill-ui/UI-ELEMENT-INVENTORY.md`
   - `docs/public/src/mill-ui.md`

## Acceptance

- UI routing and mode naming are documented.
- SQL Analysis remains a first-class technical mode.
- Visual Analysis is positioned as business-user board/path authoring.
- Feature flag strategy is explicit.
- No mock-only service dependency is introduced for Visual Analysis.

## Dependencies

- Analysis catalog API shape from WI-267 / WI-269.
- Current Analysis full-stack SQL work.

## UX Notes

- Avoid marketing-style landing pages. The first screen should be the Analysis catalog/create
  workflow.
- Visual Analysis should feel like an operational tool: dense, inspectable, and predictable.
- Business users should see column names, filters, groups, measures, joins, and charts; advanced SQL
  should be hidden unless they choose SQL Analysis.
