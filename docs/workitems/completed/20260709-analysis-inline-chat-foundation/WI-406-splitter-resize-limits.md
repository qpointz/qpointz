# WI-406 - Splitter resize limits

## Goal

Allow widening the inline chat column beyond the previous ~560px cap so users can allocate up to half
the main content width to the drawer.

## Root cause

`AppBodyWithInlineChat` passed `maxRightPx={560}` and `minLeftPx={320}`, capping drag range ~180px
above the 380px default.

## Implementation

- `horizontalSplitPaneMath.ts`: `clampRightPaneWidth`, `clampStoredRightPx`.
- `HorizontalSplitPane`: `maxRightFraction` (default 0.5) instead of fixed max pixels; removed CSS
  `maxWidth` cap on right pane.
- `AppBodyWithInlineChat`: `maxRightFraction={0.5}`, `minLeftPx={280}`, `minRightPx={260}`.

## Acceptance criteria

- User can drag drawer wider than 560px on a typical desktop viewport (up to 50% of split area).
- Stored width respects current viewport on reload.
- Unit tests for clamp math.

## Status

Complete (Stage 8).
