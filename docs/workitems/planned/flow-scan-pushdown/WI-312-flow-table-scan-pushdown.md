# WI-312 — Filter and project pushdown into FlowTableScan

**Story:** [`flow-scan-pushdown`](STORY.md) · **Backlog:** D-10  
**Prerequisite:** [`flow-translatable-table-scan`](../../completed/20260618-flow-translatable-table-scan/STORY.md) (WI-311 on `dev`)

## Status: **not started**

No `pushedFilter` / `projects` on `FlowTableScan`; no `FlowFilterTableScanRule` /
`FlowProjectTableScanRule`. `FlowTableScanPlannerTest.shouldKeepFilterAboveFlowTableScan_beforePushdown`
documents that filter stays **above** scan today.

## Goal

Add planner rules that merge single-table `Filter` and `Project` operators into `FlowTableScan`
so predicates and column masks are available at scan time instead of above a full row decode.

## Scope

- Extend `FlowTableScan` to carry optional pushed filter (`RexNode`) and/or project field indices.
- Logical Hep/Volcano rules: `Filter` → `FlowTableScan`, `Project` → `FlowTableScan` (single-table only).
- Enumerable path applies filter/project when iterating rows (logical projection in `SourceTableScan`).
- Contract tests via `FlowCalciteTestFixtures`: `SELECT "name" FROM "users" WHERE "id" = 2` has
  no `Filter` directly above `FlowTableScan` after optimization.

## Out of scope

- Physical Parquet column read (WI-313).
- Join/filter correlation pushdown across tables.
- Partition blob pruning (future story S-10).

## Architecture (read before coding)

See **[STORY.md § Architecture decisions](STORY.md#architecture-decisions-locked)**. Summary:

1. Pushdown state on `FlowTableScan`; register rules in `FlowTableScan.register()`.
2. Generic `EnumerableTableScan` **drops** pushed state — extend `FlowTableScanToEnumerableRule` /
   `SourceTableScan` so execution honours `pushedFilter` and `projects`.
3. Do **not** wire rules on JDBC `FrameworkConfig` / `FlowContextFactory`.

### Current execution chain (on `dev`)

```
FlowTableScan (NONE)
  → FlowTableScanToEnumerableRule  [FlowTableScanRules.kt]
  → EnumerableTableScan
  → FlowTable.scan(DataContext)    [FlowTable.kt]
  → SourceTableScan.scan()         [SourceTableScan.kt]
  → sourceTable.records()          full rows
```

### Target chain (this WI)

```
FlowTableScan (NONE, pushedFilter?, projects?)
  → logical rules merge Filter/Project into scan
  → FlowTableScanToEnumerableRule (preserves pushdown)
  → enumerable scan + bindable
  → SourceTableScan(sourceTable, filter?, projects?)
  → sourceTable.records()          full rows; logical skip in SourceTableScan until WI-313
```

## Implementation plan

### 1. Extend `FlowTableScan` state

| Field | Type | Purpose |
|-------|------|---------|
| `pushedFilter` | `RexNode?` | Single-table predicates merged from `LogicalFilter` |
| `projects` | `ImmutableIntList?` | Output column indices into table schema (from `LogicalProject`) |

Use immutable copy-on-write in rule `convert()` methods; update `copy(traitSet, inputs)` to preserve
fields; preserve `table` and row type via `deriveRowType()` when projects change.

File: `data/mill-data-source-calcite/src/main/kotlin/io/qpointz/mill/source/calcite/FlowTableScan.kt`

### 2. Planner rules (`mill-data-source-calcite`)

| Rule | Pattern | Action |
|------|---------|--------|
| `FlowFilterTableScanRule` | `Filter` → `FlowTableScan` | AND-merge into `pushedFilter`; remove filter node |
| `FlowProjectTableScanRule` | `Project` → `FlowTableScan` | Compose project indices; remove project node if trivial |

**Registration:** add both rules in `FlowTableScan.register()` before `FlowTableScanToEnumerableRule`.

Start with **single-table, non-correlated, deterministic** `RexNode` filters only.

New files (suggested package `io.qpointz.mill.source.calcite`):

- `FlowFilterTableScanRule.kt`
- `FlowProjectTableScanRule.kt`

### 3. Enumerable execution

| Step | Action |
|------|--------|
| 3.1 | Update `FlowTableScanToEnumerableRule` in `FlowTableScanRules.kt` to pass filter + project into execution (custom enumerable scan or equivalent — see STORY). |
| 3.2 | Extend `SourceTableScan` with optional filter + project indices; evaluate filter per row before emit. |
| 3.3 | When building `Object[]`, include only `projects` indices (full row may still be read from source until WI-313). |
| 3.4 | `LIMIT` pushdown optional stretch if Calcite exposes `Sort`+`Fetch` on scan. |

**Not** `FlowEnumerableRules.kt` (does not exist). Touch `FlowTableScanRules.kt` and `SourceTableScan.kt`.

### 4. `SourceTable` hook (minimal for this WI)

Add default on `SourceTable` (`mill-data-source-core`):

```kotlin
fun records(projection: Set<String>? = null): Iterable<Record>
```

Default delegates to `records()` ignoring projection; WI-313 implements Parquet physical projection.
`MultiFileSourceTable` forwards projection when non-null.

### 5. Tests

| Test | Class | Assert |
|------|-------|--------|
| Filter pushdown | `FlowTableScanPlannerTest` or `FlowTableScanPushdownTest` | No `Filter` parent directly above `FlowTableScan` after Hep |
| Project pushdown | `FlowTableScanPushdownTest` (new) | `FlowTableScan.projects` equals expected indices |
| End-to-end | `FlowTableScanPushdownTest` | Filtered SELECT returns correct subset |
| Regression | `FlowTableScanPlannerTest` | Full-table SELECT unchanged |
| Join policy | `SkymillJoinFixturesTest` | Hash-join tests still pass |
| Perf smoke | `SkymillJoinPerformanceIT` | Correlated cities filter faster (see STORY success criteria) |

**Baseline to invert:** `FlowTableScanPlannerTest.shouldKeepFilterAboveFlowTableScan_beforePushdown`

Fixture: `data/mill-data-source-calcite/src/test/kotlin/io/qpointz/mill/source/calcite/FlowCalciteTestFixtures.kt`

### 6. Files (expected touch)

| File | Module |
|------|--------|
| `FlowTableScan.kt` | `mill-data-source-calcite` |
| `FlowFilterTableScanRule.kt`, `FlowProjectTableScanRule.kt` | `mill-data-source-calcite` (new) |
| `FlowTableScanRules.kt` | `mill-data-source-calcite` |
| `SourceTableScan.kt` | `mill-data-source-calcite` |
| `FlowTable.kt` | `mill-data-source-calcite` (if `scan()` threads pushdown) |
| `SourceTable.kt`, `MultiFileSourceTable.kt` | `mill-data-source-core` |
| `FlowTableScanPushdownTest.kt` | `mill-data-source-calcite` (new) |
| `FlowTableScanPlannerTest.kt` | `mill-data-source-calcite` |
| `docs/design/source/mill-source-calcite.md` | design |

## Commands

```bash
./gradlew :data:mill-data-source-calcite:test --tests "io.qpointz.mill.source.calcite.FlowTableScanPlannerTest"
./gradlew :data:mill-data-source-calcite:test --tests "io.qpointz.mill.source.calcite.FlowTableScanPushdownTest"
./gradlew :data:mill-data-source-calcite:test --tests "io.qpointz.mill.source.calcite.SkymillJoinFixturesTest"
./gradlew :data:mill-data-backends:testIT --tests "io.qpointz.mill.data.backend.flow.SkymillJoinPerformanceIT"
```

## Acceptance

- [ ] Pushdown tests in `mill-data-source-calcite` pass.
- [ ] `SkymillJoinPerformanceIT` correlated-filter case shows improvement vs pre-WI-312 baseline (log ratio in IT output).
- [ ] Design doc limitations updated.

## Modules

- `data/mill-data-source-calcite`
- `data/mill-data-backends` (IT validation only)
- `data/mill-data-source-core` (optional `SourceTable` API stub)
