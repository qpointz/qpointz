# flow-translatable-table-scan

**Status:** closed **2026-06-18** (branch `feat/flow-performace-statistics`).

## Goal

Improve Mill-native Flow backend query performance on small datasets without external engines
(DuckDB, in-memory JDBC ingest, Spark). Replace the dumb `ScannableTable` full-scan path with a
**TranslatableTable** model: custom `FlowTableScan` RelNode, table statistics for the planner, and
enumerable join policy tuned for unsorted file scans.

## Context

Skymill six-table joins (~1k rows) were ~30s on Flow vs ~3s on JDBC/H2 because Calcite chose
**EnumerableMergeJoin + Sort** over full `Object[]` materialization from Parquet/Avro. Root cause
was plan shape and scan semantics, not data volume. `FlowTable` previously implemented only
`ScannableTable`; Calcite could not push filters, projects, or LIMIT into the scan.

## Delivered pipeline

```
SQL / Substrait plan
  → CalciteSqlProvider / CalcitePlanConverter.toRelNode
  → logical plan with FlowTableScan (TranslatableTable.toRel)
  → enumerable rules: FlowTableScan → EnumerableFlowTableScan (WI-311)
  → join rules biased to hash join on unsorted scans via FlowTableScan.register (WI-315)
  → statistics on FlowTable / FlowTableScan for row estimates (WI-314)
```

Physical read path today still decodes full rows from `SourceTable.records()`; filter/project
pushdown and Parquet column projection are **deferred** to follow-on story
[`flow-scan-pushdown`](../../planned/flow-scan-pushdown/STORY.md) (**WI-312**, **WI-313**).

## Test harness

`FlowCalciteTestFixtures` in `mill-data-source-calcite` provides in-memory `SourceTable` data and
Calcite JDBC wiring for unit/contract tests. Use quoted identifiers in SQL (`"users"`, `"id"`) —
Calcite folds unquoted names to uppercase.

## Related backlog

- **D-9** — `done` (this story).
- **D-10** — filter/project pushdown + Parquet projection (**WI-312**, **WI-313**), story
  [`flow-scan-pushdown`](../../planned/flow-scan-pushdown/STORY.md).
- **S-10** — `HivePartitionTableMapper` + partition **pruning** (separate story after WI-312).

## Design

[`docs/design/source/mill-source-calcite.md`](../../../design/source/mill-source-calcite.md) —
TranslatableTable, statistics, join policy, limitations (pushdown noted as follow-on).

## Work Items

- [x] WI-311 — FlowTableScan and TranslatableTable foundation (`WI-311-flow-table-scan-translatable-table.md`)
- [x] WI-314 — Flow table statistics for Calcite planner (`WI-314-flow-table-statistics.md`)
- [x] WI-315 — Flow enumerable join policy (hash over merge on file scans) (`WI-315-flow-enumerable-join-policy.md`)
- [x] WI-316 — Skymill join performance IT and design docs (`WI-316-skymill-perf-it-and-design-docs.md`)

**Split at closure:** WI-312 and WI-313 moved to [`planned/flow-scan-pushdown`](../../planned/flow-scan-pushdown/STORY.md).
