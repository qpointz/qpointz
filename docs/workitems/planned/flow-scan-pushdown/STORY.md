# flow-scan-pushdown

## Goal

Complete the Flow **TranslatableTable** execution path by pushing single-table **filter** and
**project** operators into `FlowTableScan`, then reading only required columns at the source layer
(physical Parquet column projection where possible). This addresses remaining Skymill performance
gaps on the Substrait execution path where predicates still run above full-row scans.

## Prerequisite

Story **[`flow-translatable-table-scan`](../../completed/20260618-flow-translatable-table-scan/STORY.md)**
(closed **2026-06-18**) delivered:

- **WI-311** — `FlowTableScan`, `TranslatableTable`, enumerable scan execution
- **WI-314** — table statistics and `FlowTableScan.estimateRowCount()`
- **WI-315** — hash-join bias via `FlowEnumerableRuleSets` on `FlowTableScan.register()`
- **WI-316** — Skymill perf IT baseline + `mill-source-calcite.md`

Branch lineage: continue from `feat/flow-performace-statistics` (or a new branch from `dev` after merge).

## Context

`SkymillJoinPerformanceIT` shows JDBC **EXPLAIN** is hash-biased after WI-315, but correlated
filter queries on the **Substrait execution path** can still be ~20× slower than baseline because
filters and projects sit above `FlowTableScan` and Parquet reads full rows. WI-312 merges filter/project
into the scan RelNode; WI-313 passes column masks into `SourceTable` / `ParquetRecordSource`.

## Target execution pipeline

```
SQL / Substrait plan
  → logical plan with FlowTableScan
  → Hep/Volcano: filter + project pushdown into FlowTableScan (WI-312)
  → enumerable bindable applies pushed filter + project mask
  → SourceTable.read / records(projection) (WI-313)
    → Parquet: projected column chunks
    → Avro/CSV: logical field skip
```

## Implementation order

| Phase | WI | Depends on | Delivers |
|-------|-----|------------|----------|
| 1 | WI-312 | WI-311 (done) | `pushedFilter`, `projects` on `FlowTableScan`; planner + enumerable rules |
| 2 | WI-313 | WI-312 | `SourceTable` projection API; Parquet selective read |

## Test harness

Reuse `FlowCalciteTestFixtures` and `SkymillJoinPerformanceIT` from prerequisite story. After WI-312,
assert no `Filter` directly above `FlowTableScan` on single-table queries. After WI-313, assert
Parquet reads omit unreferenced columns.

## Related backlog (out of scope)

- **S-10** — partition **pruning** by blob path (after pushed-predicate hook exists).
- Join reorder / cross-table correlation pushdown.
- Substrait → RelNode migration — orthogonal; both paths use `TranslatableTable.toRel`.

## Design

Update [`docs/design/source/mill-source-calcite.md`](../../../design/source/mill-source-calcite.md)
and [`docs/design/source/formats/parquet.md`](../../../design/source/formats/parquet.md) when each WI
lands.

## Work Items

- [ ] WI-312 — Filter and project pushdown into FlowTableScan (`WI-312-flow-table-scan-pushdown.md`)
- [ ] WI-313 — Selective field read and Parquet column projection (`WI-313-selective-field-read-parquet.md`)
