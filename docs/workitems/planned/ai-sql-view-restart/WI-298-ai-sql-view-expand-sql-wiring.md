# WI-298 — SQL expand wiring

**Story:** [`ai-sql-view-restart`](STORY.md)

| Field | Value |
|--------|--------|
| **Status** | `planned` |
| **Type** | `✨ feature` |
| **Area** | `ui` |
| **Depends on** | [**WI-296**](WI-296-ai-sql-view-shared-query-data-view.md), [**WI-297**](WI-297-ai-sql-view-expand-shell.md), [**WI-292**](WI-292-ai-sql-view-artifact-preview-ui.md) |
| **Enables** | [**WI-299**](WI-299-ai-sql-view-verification-closure.md) |

## Goal

Wire **Expand** for `general` + `sql-data-composite`: full paging, action parity, chat-native
**SqlDataExpandedView**.

## Deliver

- Register `sql-data-composite` in `expandRegistry` (`general` only).
- **`SqlDataExpandedView`:** ExpandHeader, SqlReadOnlyPanel, `QueryDataView` expanded, shared action bar.
- Expand button on `SqlDataCondensedPreview` when treatment allows.
- Action parity: Run, Copy, Export, Open in Analysis (SQL-only handoff).

### Tests

- Vitest: no Expand on `inline-analysis`; paging; back navigation.

## Acceptance criteria

- [ ] Full expand flow on `general` chat.
- [ ] Condensed ↔ expand visually consistent (chat-native).
- [ ] Re-run in expand without closing pane.
