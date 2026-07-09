# WI-391 - Inline artifact strips

## Goal

Introduce compact artifact strip presentation for inline chats so generated artifacts fit the
smaller drawer surface and drive host-specific actions.

## Status

**Complete** — `inline-artifact-strip` mode, SQL/facet strip components, typed host action dispatch,
auto-apply removed from renderers.

## Requirements

- Add the specialized inline artifact presentation mode `inline-artifact-strip`.
- For `inline-analysis`, render SQL artifacts as compact proposal strips instead of the full
  SQL/Data/Chart artifact card.
- Replace the current `host-apply` presentation for `inline-analysis` SQL with
  `inline-artifact-strip`; do not leave the artifact invisible in the drawer.
- Use a quiet, recognizable visual shape:
  - framed horizontal strip
  - artifact label on the left
  - low-density action buttons on the right
  - no large tabbed content by default
- Include concise artifact identity:
  - SQL proposals: `<Query title>.sql` or a fallback such as `Generated query.sql`
  - non-SQL artifacts: artifact type plus title when available
  - artifact type indicator for SQL
- Include actions:
  - `apply`
  - `apply-and-run`
  - `copy`
  - future context-specific actions such as `exclude`
- Do not auto-apply SQL when the artifact arrives unless the Analysis host has enabled
  `autoapply.enabled`.
- Keep the artifact strip renderer generic: it should expose artifact actions and state, but the
  Analysis host interprets auto-apply and auto-run behavior.
- Keep user choice understandable when settings are enabled:
  - when auto-apply is off, Apply remains a primary action
  - when auto-apply is on, the artifact strip should indicate the proposal was applied
  - when auto-run is on, the artifact strip should not imply execution happened until the host
    confirms the run was started
- Clicking the strip should open a popover/overlay preview.
- The preview may reuse existing full artifact rendering in read-only mode, but must hide primary
  action buttons owned by the strip/host.
- Render non-SQL artifacts in Analysis copilot as compact strips, not full drawer cards.
- Non-SQL artifact strips should show only minimal identity: type and title when available.
- Non-SQL artifact strips may expose an `open in` action when the artifact has a natural
  destination, such as Model view.
- The `open in` destination is artifact-specific and host-owned.
- Clicking a non-SQL strip opens a popover/overlay with the full artifact view.
- For SQL + data composite artifacts in the Analysis copilot, show SQL identity and optional
  lightweight status/row-count metadata; do not embed a data grid in the drawer strip.
- When a single assistant turn contains multiple SQL proposals:
  - render every SQL proposal as its own inline artifact strip
  - treat the first SQL proposal as the primary/default proposal for automation
  - keep later SQL proposals visible and manually actionable
  - do not mark later proposals as superseded only because they are not first
- Keep General Chat artifact rendering unchanged.
- Keep future inline chat contexts able to use the same artifact strip foundation with different
  actions.

## Acceptance Criteria

- Analysis copilot SQL artifacts render as inline artifact strips.
- Analysis copilot non-SQL artifacts render as compact inline artifact strips with only type and
  title when available.
- The full tabbed artifact preview is not used inside the Analysis copilot drawer.
- `inline-analysis` SQL artifacts no longer use silent `host-apply` rendering.
- Apply and Apply & Run dispatch typed host actions instead of directly mutating the editor from the
  renderer.
- Artifact strip rendering remains usable when Analysis auto-apply hides or deemphasizes the manual
  Apply action.
- Artifact strip state makes auto-applied proposals understandable without adding full artifact
  chrome.
- Clicking the strip opens a read-only preview without duplicating action buttons.
- Non-SQL strip popup/overlay can show the full artifact view while keeping the drawer strip
  compact.
- Non-SQL strips expose `open in` only when an artifact-specific destination exists.
- SQL + data composite artifacts do not render an embedded drawer data grid.
- Multiple SQL proposals in one turn render as separate strips.
- Later SQL proposals remain manually actionable even when the first proposal is the automation
  target.
- Copy writes the SQL text to the clipboard.
- General Chat SQL artifact tests continue to pass unchanged.

## Verification

```bash
cd ui/mill-ui && npm run test -- --run
```
