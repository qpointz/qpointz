# WI-316 — Skymill join performance IT and design docs

## Goal

Add a repeatable integration test for Skymill multi-join performance on Flow (Parquet and Avro) and
update design documentation for the TranslatableTable execution model.

## Scope

- `SkymillJoinPerformanceIT` in `mill-data-backends` `testIT`:
  - Phase timing (parse / toRelNode / execute+drain).
  - Compare join with and without `WHERE c2.id = c3.id` (plan + timing).
  - Parquet vs Avro comparison on same row sets.
  - EXPLAIN plan assertions (hash vs merge, table scan counts).
- Test configs: `flow-skymill-parquet.yaml`, `flow-skymill-avro.yaml` under
  `data/mill-data-backends/config/test/`.
- Avro fixture aligned with Parquet row counts (document if using converted tree).
- Update `docs/design/source/mill-source-calcite.md`: TranslatableTable, FlowTableScan, pushdown,
  statistics, limitations table.

## Out of scope

- CI performance gating (informational IT only unless budgets stable).
- Public docs unless behaviour is user-visible.

## Implementation plan

### 1. Test fixtures and config

| Asset | Purpose |
|-------|---------|
| `config/test/flow-skymill-parquet.yaml` | Flow source → `test/datasets/skymill/parquet` |
| `config/test/flow-skymill-avro.yaml` | Avro tree row-aligned with Parquet (converted or native) |
| `testIT` Gradle | `flow.facet.it.root` property; `showStandardStreams` for timing logs |

### 2. `SkymillJoinPerformanceIT` structure

| Test method | Purpose |
|-------------|---------|
| `shouldCompleteFullSixJoinQueryWithinBudget` | End-to-end timing budget (Parquet + Avro parametrized) |
| `shouldCompareJoinWithAndWithoutCorrelatedCitiesFilter` | Plan diff + slowdown ratio (target <3x after WI-315) |
| `shouldBreakDownExecutePhasesForFullJoin` | parse / toRelNode / execute+drain ms |
| `shouldCountPhysicalTableScansInExplainPlan` | `cities` scanned 3×, large tables 1× |
| `shouldCompareParquetVsAvroFullJoinTiming` | Encoding not dominant cost |

SQL constants:

- `JOIN_WITHOUT_WHERE_SQL` — six-table join, no filter.
- `FULL_JOIN_SQL` — same + `WHERE c2.id = c3.id`.

Use `FlowBackendContextRunner.flowContext(yaml)`.

### 3. Time budgets (initial → tighten after WI-311–315)

| Scenario | Initial budget (generous) | Target after optimizations |
|----------|-------------------------|----------------------------|
| Full join, no WHERE | 90s | <5s |
| Full join, with WHERE | 90s | <10s (within ~3× of no-WHERE) |
| Single table scan | 3s | <1s |

Mark `@Disabled` subtests until prior WIs land if needed; enable as optimizations merge.

### 4. EXPLAIN assertions

Parse `EXPLAIN PLAN FOR` output for:

- `EnumerableHashJoin` vs `EnumerableMergeJoin` at top `c3` join
- `EnumerableSort` count (lower after WI-315)
- `EnumerableTableScan` / future `FlowTableScan` table names

### 5. Design documentation updates

| Doc section | Content |
|-------------|---------|
| Architecture diagram | TranslatableTable pipeline (see STORY.md) |
| `FlowTable` | Implements `TranslatableTable`; link to `FlowTableScan` |
| Limitations table | Remove "ScannableTable only"; document pushdown + stats |
| Performance notes | Hash vs merge on unsorted scans; Skymill reference |
| Related | Pointer to future partition pruning story (S-10) |

Optional: `docs/design/source/flow-execution-performance.md` if `mill-source-calcite.md` grows too large.

### 6. Files (expected touch)

- `SkymillJoinPerformanceIT.kt` (new)
- `flow-skymill-parquet.yaml`, `flow-skymill-avro.yaml` (new)
- `mill-data-backends/build.gradle.kts` (testIT config if needed)
- `docs/design/source/mill-source-calcite.md`

## Acceptance

- `./gradlew :data:mill-data-backends:testIT --tests SkymillJoinPerformanceIT` passes with
  documented time budgets (tighten as WI-311–315 complete).
- Design doc reflects new architecture.

## Modules

- `data/mill-data-backends`
- `docs/design/source/mill-source-calcite.md`
- `test/datasets/skymill/` (fixtures as needed)
