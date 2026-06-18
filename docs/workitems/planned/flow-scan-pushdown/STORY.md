# flow-scan-pushdown

**Backlog:** [D-10](../../BACKLOG.md) · **Status:** `planned` (no WI checked yet)

## Goal

Complete the Flow **TranslatableTable** execution path by pushing single-table **filter** and
**project** operators into `FlowTableScan`, then reading only required columns at the source layer
(physical Parquet column projection where possible). This addresses remaining Skymill performance
gaps on the Substrait execution path where predicates still run above full-row scans.

## Branching

```bash
git fetch origin dev
git checkout -b feat/flow-scan-pushdown origin/dev
```

Do **not** continue on `feat/flow-performace-statistics` — that story is merged and archived.

## Prerequisite (on `dev`)

Story **[`flow-translatable-table-scan`](../../completed/20260618-flow-translatable-table-scan/STORY.md)**
(closed **2026-06-18**) delivered:

| WI | Deliverable |
|----|-------------|
| WI-311 | `FlowTableScan`, `TranslatableTable`, `FlowTableScanToEnumerableRule` |
| WI-314 | `FlowTableStatistics`, format-backed row counts, `FlowTableScan.estimateRowCount()` |
| WI-315 | `FlowEnumerableRuleSets` (hash join, no merge join on file scans) |
| WI-316 | `SkymillJoinPerformanceIT`, `mill-source-calcite.md` |

## Problem statement

`SkymillJoinPerformanceIT` on `dev`:

- JDBC **EXPLAIN** on the filtered 6-join query is hash-biased (WI-315).
- **Substrait execution** (`sqlProvider.parseSql` → `executionProvider.execute`) on
  `WHERE c2.id = c3.id` can still be ~20× slower than the same join without the filter, because
  filters/projects sit **above** `FlowTableScan` and every row is fully decoded from Parquet/Avro.

WI-312 merges filter/project into the scan RelNode; WI-313 passes column masks into
`SourceTable` / `ParquetRecordSource`.

## Architecture decisions (locked)

These decisions remove ambiguity for a cold-start implementer. Do not register pushdown on JDBC
`FrameworkConfig` or `FlowContextFactory` — follow the WI-311 / WI-315 **RelNode** hook pattern.

### 1. Pushdown lives on `FlowTableScan`, not `FlowTable`

- Add optional `pushedFilter: RexNode?` and `projects: ImmutableIntList?` on `FlowTableScan`.
- `FlowTable.toRel()` stays unchanged; planner rules merge `Filter` / `Project` **into** the scan.
- `FlowTable.scan(DataContext)` remains the enumerable entry point but must receive pushed state
  (see §3).

### 2. Register logical pushdown rules from `FlowTableScan.register()`

Mirror WI-315: when Volcano or Hep first sees `FlowTableScan`, `register(planner)` adds:

1. `FlowFilterTableScanRule`, `FlowProjectTableScanRule` (logical, `Convention.NONE`)
2. `FlowTableScanToEnumerableRule` (existing)
3. `FlowEnumerableRuleSets.register(planner)` (existing)

`FlowRelPlannerRules.registerRulesFromRelTree` already walks the plan and calls `registerClass`;
Hep explain in tests uses the same rule set — no duplicate rule lists in fixtures.

**Rule order in Hep program:** project pushdown, then filter pushdown, then enumerable conversion
(bottom-up).

### 3. Enumerable conversion must preserve pushed state

**Current behaviour (do not keep for pushed scans):** `FlowTableScanToEnumerableRule` in
`FlowTableScanRules.kt` converts to generic `EnumerableTableScan`, which **drops** custom scan
state.

**Required for WI-312:** extend the converter (or add `FlowEnumerableTableScan`) so enumerable
execution still sees `pushedFilter` and `projects`. Recommended spike order:

1. Add fields + `copy()` on `FlowTableScan`.
2. Implement logical pushdown rules; prove with Hep on logical plan (filter not parent of scan).
3. Thread filter/project into `SourceTableScan` (new constructor or scan parameters) via updated
   converter + `FlowTable.scan()` — generic `EnumerableTableScan` alone is insufficient.

`SourceTableScan` (`mill-data-source-calcite`) is the single place that turns `Record` → `Object[]`;
apply logical projection and row predicate there until WI-313 adds physical Parquet projection.

### 4. Substrait and SQL share the same RelNode path

Both must emit `FlowTableScan` at the logical layer, then the same pushdown + enumerable rules:

| Path | Entry | Plan shape |
|------|-------|------------|
| SQL | `CalciteSqlProvider.parseSql` | Frameworks planner → `TranslatableTable.toRel` |
| Substrait | `CalcitePlanConverter` / `PlanConverter` | `RelBuilder.scan` → `toRel()` |
| Execute | `RelRunner.prepareStatement(rel)` / JDBC | Volcano + `FlowTableScan.register()` |

Proof IT for end-to-end perf: `SkymillJoinPerformanceIT` in `mill-data-backends` (uses
`FlowBackendContextRunner.sqlProvider` + `executionProvider`, **not** JDBC `EXPLAIN` alone).

## Target execution pipeline

```
SQL / Substrait plan
  → logical plan with FlowTableScan
  → logical rules: filter + project pushdown into FlowTableScan (WI-312)
  → FlowTableScanToEnumerableRule (+ preserved pushdown state)
  → SourceTableScan applies filter + project mask (WI-312 logical; WI-313 physical Parquet)
  → join rules (FlowEnumerableRuleSets, WI-315)
```

## Code map (`dev`)

| Area | Path |
|------|------|
| Logical scan RelNode | `data/mill-data-source-calcite/.../FlowTableScan.kt` |
| Enumerable converter | `data/mill-data-source-calcite/.../FlowTableScanRules.kt` |
| Enumerable rule bundle | `data/mill-data-source-calcite/.../FlowEnumerableRuleSets.kt` |
| Rule discovery / Hep explain | `data/mill-data-source-calcite/.../FlowRelPlannerRules.kt` |
| Row materialization | `data/mill-data-source-calcite/.../SourceTableScan.kt` |
| Calcite table | `data/mill-data-source-calcite/.../FlowTable.kt` |
| Unit fixtures | `data/mill-data-source-calcite/src/test/.../FlowCalciteTestFixtures.kt` |
| Pushdown baseline test | `FlowTableScanPlannerTest.shouldKeepFilterAboveFlowTableScan_beforePushdown` |
| Skymill perf IT | `data/mill-data-backends/src/testIT/.../SkymillJoinPerformanceIT.kt` |
| YAML configs | `data/mill-data-backends/config/test/flow-skymill-{parquet,avro}.yaml` |
| Source API | `data/mill-data-source-core/.../SourceTable.kt` |
| Parquet read | `data/formats/mill-data-format-parquet/.../ParquetRecordSource.kt` |
| Design (update per WI) | `docs/design/source/mill-source-calcite.md`, `formats/parquet.md` |

## Implementation order

| Phase | WI | Depends on | Delivers |
|-------|-----|------------|----------|
| 1 | WI-312 | WI-311 (on `dev`) | `pushedFilter`, `projects`; logical rules; enumerable path applies them |
| 2 | WI-313 | WI-312 | `SourceTable.records(projection)`; Parquet column chunks |

## Test commands

```bash
# WI-312 — unit / contract (mill-data-source-calcite)
./gradlew :data:mill-data-source-calcite:test --tests "io.qpointz.mill.source.calcite.FlowTableScanPlannerTest"
./gradlew :data:mill-data-source-calcite:test --tests "io.qpointz.mill.source.calcite.FlowTableScanPushdownTest"

# WI-312 — join fixtures (hash join still required)
./gradlew :data:mill-data-source-calcite:test --tests "io.qpointz.mill.source.calcite.SkymillJoinFixturesTest"

# WI-313 — Parquet format
./gradlew :data:formats:mill-data-format-parquet:test --tests "*SelectiveRead*"

# End-to-end perf + Substrait path (after WI-312; tighten budgets when stable)
./gradlew :data:mill-data-backends:testIT --tests "io.qpointz.mill.data.backend.flow.SkymillJoinPerformanceIT"
```

Use quoted identifiers in SQL (`"users"`, `"id"`) — Calcite folds unquoted names to uppercase.

## Success criteria

### WI-312 done when

- [ ] `FlowTableScanPlannerTest`: filter **not** directly above `FlowTableScan` after optimization
  (invert `shouldKeepFilterAboveFlowTableScan_beforePushdown`).
- [ ] New `FlowTableScanPushdownTest`: project indices on scan; filtered SELECT correct rows.
- [ ] `SkymillJoinFixturesTest` still passes (hash-join policy unchanged).
- [ ] `SkymillJoinPerformanceIT.shouldCompareJoinWithAndWithoutCorrelatedCitiesFilter_onParquet`:
  filtered timing materially improved vs baseline at merge (log ratio; optional strict &lt;3× once stable).
- [ ] `mill-source-calcite.md` limitations row for filter/project pushdown updated.

### WI-313 done when

- [ ] `SourceTable.records(projection)` plumbed from enumerable scan.
- [ ] `ParquetRecordSource` reads projected schema (not full `AvroParquetReader` row for omitted columns).
- [ ] Parquet unit test proves ≤ requested columns read.
- [ ] `formats/parquet.md` column projection row → Yes.

## Related backlog (out of scope)

- **S-10** — partition **pruning** by blob path (after pushed-predicate hook exists).
- Join reorder / cross-table correlation pushdown.
- Substrait → RelNode migration — orthogonal; both paths use `TranslatableTable.toRel`.

## Work Items

- [ ] WI-312 — Filter and project pushdown into FlowTableScan (`WI-312-flow-table-scan-pushdown.md`)
- [ ] WI-313 — Selective field read and Parquet column projection (`WI-313-selective-field-read-parquet.md`)
