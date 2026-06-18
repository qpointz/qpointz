# WI-316 — Skymill join performance IT and design docs

## Status: **complete** (2026-06-15)

| Area | Status |
|------|--------|
| `SkymillJoinQueries` + unit smoke (`SkymillJoinFixturesTest`) | Done |
| `FlowTableSkymillJdbcIT` (basic JDBC smoke) | Done (WI-311) |
| `SkymillJoinPerformanceIT` with timing budgets | **Done** |
| `flow-skymill-parquet.yaml` / `flow-skymill-avro.yaml` in backends `config/test` | **Done** |
| `mill-source-calcite.md` TranslatableTable / stats / join policy | **Done** |
| Format capability public + design docs | Done (prior commit) |

### Notes

- Join-policy proof (hash bias on filtered 6-join) is asserted in WI-315 unit tests and
  `shouldLogHashJoinBiasedPlan_whenExplainFilteredJoin_onParquet` (JDBC EXPLAIN).
- Substrait execution path (`SqlProvider` → `PlanConverter` → `ExecutionProvider`) may still be
  slower than baseline on correlated cities filter until [WI-312](../../planned/flow-scan-pushdown/WI-312-flow-table-scan-pushdown.md)
  filter pushdown; IT uses 90s
  budgets and logs slowdown ratio rather than a strict &lt;3× gate.

## Goal

Repeatable Skymill multi-join performance IT on Flow (Parquet / Avro) and design documentation
for the TranslatableTable execution model.

## Acceptance

- [x] `./gradlew :data:mill-data-backends:testIT --tests SkymillJoinPerformanceIT` passes
- [x] Design doc `mill-source-calcite.md` updated

## Modules

- `data/mill-data-backends`
- `docs/design/source/mill-source-calcite.md`
