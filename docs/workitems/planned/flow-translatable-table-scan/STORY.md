# flow-translatable-table-scan

## Goal

Improve Mill-native Flow backend query performance on small datasets without external engines
(DuckDB, in-memory JDBC ingest, Spark). Replace the dumb `ScannableTable` full-scan path with a
**TranslatableTable** model: custom `FlowTableScan` RelNode, filter/project pushdown, selective
Parquet column reads, table statistics, and enumerable join policy tuned for unsorted file scans.

## Context

Skymill six-table joins (~1k rows) take ~30s on Flow vs ~3s on JDBC/H2 because Calcite chooses
**EnumerableMergeJoin + Sort** over full `Object[]` materialization from Parquet/Avro. Root cause
is plan shape and scan semantics, not data volume. `FlowTable` today implements only
`ScannableTable`; Calcite cannot push filters, projects, or LIMIT into the scan.

## Target execution pipeline

```
SQL / Substrait plan
  → CalciteSqlProvider / CalcitePlanConverter.toRelNode
  → logical plan with FlowTableScan (TranslatableTable.toRel)
  → Hep/Volcano: filter + project pushdown into FlowTableScan (WI-312)
  → enumerable rules: FlowTableScan → EnumerableFlowTableScan (WI-311)
  → scan with projection + optional filter (WI-313)
  → join rules biased to hash join on unsorted scans (WI-315)
```

Physical read path (Mill-native, no external engine):

```
FlowTableScan (required columns, optional Rex filter)
  → SourceTable.read(projection, predicate)
    → Parquet: projected column chunks (WI-313)
    → Avro/CSV: logical field skip
  → Object[] or vector blocks → enumerable join/hash (WI-315)
```

## Implementation order

| Phase | WI | Depends on | Delivers |
|-------|-----|------------|----------|
| 1 | WI-311 | — | `FlowTableScan`, `TranslatableTable`, enumerable execution |
| 2 | WI-312 | WI-311 | Pushed filter/project on scan RelNode |
| 3 | WI-313 | WI-312 | Physical column read in source/Parquet |
| 4 | WI-314 | WI-311 | Row counts + keys for planner (can parallel WI-312) |
| 5 | WI-315 | WI-311, WI-314 preferred | Hash-join bias on flow connection |
| 6 | WI-316 | WI-311–315 | Skymill IT budgets + design docs |

WI-314 can start once WI-311 lands (statistics attach to `FlowTable`, not pushdown). WI-315 should
follow WI-314 so row-count estimates inform hash build side; can ship a rule-only spike before stats.

## Test harness

`FlowCalciteTestFixtures` in `mill-data-source-calcite` provides in-memory `SourceTable` data and
Calcite JDBC wiring for unit/contract tests added per WI. Use quoted identifiers in SQL
(`"users"`, `"id"`) — Calcite folds unquoted names to uppercase.

## Related backlog (out of scope for this story)

- **S-10** — `HivePartitionTableMapper` + partition **pruning** (separate story after WI-312;
  `table.attributes` today only injects partition columns per row, does not skip blobs).
- Substrait → RelNode migration (`docs/design/platform/substrait-to-relnode-migration.md`) —
  orthogonal; both paths call `TranslatableTable.toRel` via `RelBuilder.scan`.

## Work Items

- [ ] WI-311 — FlowTableScan and TranslatableTable foundation (`WI-311-flow-table-scan-translatable-table.md`)
- [ ] WI-312 — Filter and project pushdown into FlowTableScan (`WI-312-flow-table-scan-pushdown.md`)
- [ ] WI-313 — Selective field read and Parquet column projection (`WI-313-selective-field-read-parquet.md`)
- [ ] WI-314 — Flow table statistics for Calcite planner (`WI-314-flow-table-statistics.md`)
- [ ] WI-315 — Flow enumerable join policy (hash over merge on file scans) (`WI-315-flow-enumerable-join-policy.md`)
- [ ] WI-316 — Skymill join performance IT and design docs (`WI-316-skymill-perf-it-and-design-docs.md`)
