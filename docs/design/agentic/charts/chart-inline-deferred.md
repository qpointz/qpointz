# Inline chat chart treatment — deferred

Normative for **WI-370** / Gap 26.

## Decision

**Inline chat hosts are out of scope for chart rendering in this story.** Chart preview, Run,
expand, and Run all chart passes apply to **`general` chat only**.

Inline surfaces (`inline-analysis`, `inline-model`, `inline-knowledge`) need a different UX later
(less space, host-specific affordances). That work is **explicitly deferred** — not a WI-370
acceptance gap.

## WI-370 behaviour (interim)

When a composite includes `chart` on an inline host:

| Case | Behaviour |
|------|-----------|
| Composite has `sql` and/or `data` | Render **existing inline treatment** — **Data \| SQL** tabs only; **no Chart tab**, no `ChartRenderer` |
| Chart-only turn (no separate `sql` artefact) | **`prose-only`** fallback — assistant text if any; no chart card |
| `resolveArtifactTreatment` | Unchanged registry rows for `sql-data-composite`; chart branch gated by `chatType === 'general'` in preview components |

Do **not** add chart rows to `chatArtifactTreatments` for inline hosts in WI-370.

## Future (post–WI-370)

Dedicated design for compact inline chart UX, e.g.:

- thumbnail sparkline vs full `ChartRenderer`
- `inline-analysis` host-apply of chart spec vs open-in-analysis handoff
- whether inline-model / inline-knowledge show charts at all

Track as follow-up story or WI after general-chat chart ship is stable.

## Tests (WI-370)

- `general` — chart tab renders.
- `inline-analysis` / `inline-model` / `inline-knowledge` — composite with chart artefact does **not**
  mount `ChartRenderer`; SQL/data paths unchanged.

## Out of scope (this story)

- Inline chart treatment matrix rows.
- Host-apply chart to Analysis from inline chat.
- Inline Run all chart pass.
