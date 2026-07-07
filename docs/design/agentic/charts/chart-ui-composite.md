# Chart UI composite model (mill-ui)

Normative for **WI-370** / Gap 22. Chart artefacts reuse the existing **`sql-data-composite`**
render group — no separate `ArtefactKind` or expand-registry entry.

## Principle

Avoid preemptive decomposition. Chart cards share the same shell, actions, Run semantics, and expand
flow as SQL/data cards. The only meaningful difference is an optional **Chart** tab when a
`generated-chart` wire part is present.

## Render group

Extend [`SqlDataCompositeGroup`](../../../../ui/mill-ui/src/components/chat/artifactPreview/types.ts):

```ts
export type SqlDataCompositeGroup = {
  kind: 'sql-data-composite';
  sql?: Extract<ChatMessageArtifact, { kind: 'sql' }>;
  data?: Extract<ChatMessageArtifact, { kind: 'data' }>;
  chart?: Extract<ChatMessageArtifact, { kind: 'chart' }>;
};
```

- **`ArtefactKind` stays** `sql-data-composite` | `facet-proposal` — no `chart` kind at the group layer.
- Wire/SSE still parse flat `kind: "chart"` on [`ChatMessageArtifact`](../../../../ui/mill-ui/src/types/chat.ts);
  grouping folds them into the composite.

## Grouping rules ([`groupMessageArtifacts`](../../../../ui/mill-ui/src/components/chat/artifactPreview/artifactGroups.ts))

| Incoming | Action |
|----------|--------|
| `chart` + matching `sql` (lineage `sourceArtifactIds` or normalized SQL text) | Attach `chart` to that composite |
| `chart` + matching `data` composite (same SQL) | Attach `chart` to composite |
| `chart` alone (self-contained payload embeds SQL) | New composite with `chart` only; SQL/Data tabs read embedded fields from chart payload |
| `sql` / `data` without chart | Unchanged |

Chart-only turns are still one card — not a second artefact block beside SQL.

## Tabs and default selection

| Composite contents | Tab order | Default tab (live) | Default tab (REST replay) |
|------------------|-----------|--------------------|---------------------------|
| sql and/or data only | Data \| SQL | Data | SQL |
| chart present | **Chart** \| Data \| SQL | Chart | Chart |

Multi-chart (`charts.length > 1`): Chart tab uses sub-tabs per `chartKey`
([`multi-chart-artifact-model.md`](multi-chart-artifact-model.md) §7).

## Shared vs chat-specific

| Layer | Location | Notes |
|-------|----------|-------|
| Semantic → ECharts compiler, `ChartRenderer` | `ui/mill-ui/src/components/charts/` | Plain props; no chat types |
| Tabs, Run, expand, treatments | Existing `SqlDataCondensedPreview`, `SqlDataExpandedView` | Branch on `group.chart` |
| Expand registry | `sql-data-composite` only | No `chart` expand entry |

## Run / Run all

- **Run** on a composite with `chart`: load bounded chart snapshot (Gap 23), then render Chart tab.
- **Data tab** keeps paged grid semantics (`resultMode: paged`).
- **Run all** walks composites; for each with `chart`, hydrate SQL/data if needed, then chart snapshot.

## Out of scope

- Standalone `chart` `ArtefactKind` or `sql-data-chart-composite` type name.
- Separate chart preview component tree duplicating `ChatArtifactCard` / action bar.
