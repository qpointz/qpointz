# Chat Run all — chart targets

Normative for **WI-370** / Gap 25.

## Decision

Extend **Run all** to include **`kind: 'chart'`** artefacts. Use the **same traversal rules as
`collectChatSqlTargets`** — no topological sort, no lineage graph, no cross-target idempotency
registry.

## Traversal (identical to SQL collector)

[`collectChatSqlTargets`](../../../../ui/mill-ui/src/services/chatSqlExecution.ts) rules, applied to
chart artefacts:

| Rule | SQL today | Chart |
|------|-----------|-------|
| Message order | Bottom-up (most recent assistant turn first) | **Same** |
| Role filter | Assistant turns only | **Same** |
| Multi-artefact per turn | `[...sqlArtifacts].reverse()` within turn | **`[...chartArtifacts].reverse()`** |
| Dedupe key | `` `${messageId}:${artifactId ?? sql}` `` | `` `${messageId}:${artifactId ?? normalizedSql}` `` |
| Fallback | `data` artefact SQL when no `sql` artefact | Chart payload embedded `sql` when needed for target identity |

Add `collectChatChartTargets(messages)` returning `ChatChartTarget[]`:

```ts
export interface ChatChartTarget {
  messageId: string;
  chartArtifactId?: string;
  sql: string; // from chart payload or paired sql artefact
}
```

## Run-all phases (two sequential passes)

```
Pass 1 — SQL (unchanged):  for each sqlTarget → executeChatSqlArtifact (paged data tab)
Pass 2 — Chart (new):       for each chartTarget → load chart snapshot + render (Gap 23 full mode)
```

Both passes iterate in **collector order** (bottom-up document order). No interleaving per message.

Rationale: STORY requires SQL/data hydration before chart render; two passes keep the existing SQL
loop untouched and add a chart loop with the same ordering semantics.

## Idempotency — not a separate concern

**Rejected:** explicit idempotency map when chart SQL equals an SQL target executed in pass 1.

**Instead:** pass 2 uses the same reuse path as single-card **Run** ([`chart-snapshot-fetch.md`](./chart-snapshot-fetch.md)):

- If pass 1 attached a matching `data` artefact (same SQL, sufficient rows, not truncated for chart
  limit) → chart pass reuses it.
- Otherwise → `fetchChartSnapshot` executes `resultMode: 'full'`.

No extra dedupe beyond the collector's per-turn `seen` set (same as SQL). Re-running Run all may
re-execute queries — acceptable, same as SQL Run all today.

## Toolbar / hook surface

Extend [`useRunAllChatQueries`](../../../../ui/mill-ui/src/components/chat/useRunAllChatQueries.ts):

- `runAllDisabled` when **no SQL and no chart** targets (not SQL-only).
- `sqlQueryCount` → consider `runAllTargetCount` or keep SQL count + chart count for label.
- After pass 2, bump `runAllTick` so condensed previews switch to Chart tab (mirror Data tab today).

## Out of scope

- Topological sort by `lineage.sourceArtifactIds`.
- Global SQL string dedupe across messages.
- Chart-only turns requiring pass 1 (pass 2 alone is sufficient via embedded SQL + full snapshot).

## Tests (WI-370)

- `collectChatChartTargets` — bottom-up order, multi-chart per turn, chart-only turn, dedupe.
- Run-all integration — pass 1 then pass 2; chart reuses data from pass 1 when SQL matches.
