# WI-311 — FlowTableScan and TranslatableTable foundation

## Status: **complete** (2026-06-18)

| Area | Result |
|------|--------|
| `FlowTableScan` + `TranslatableTable` | Done — `FlowTable.toRel()` → `FlowTableScan` |
| Enumerable conversion | Done — `FlowTableScanToEnumerableRule` + interim full `EnumerableRules` in `FlowTableScan.register()` |
| Unit tests | Done — `FlowTableScanPlannerTest`, `FlowTableTest` |
| Skymill JDBC `testIT` | Done — `FlowTableSkymillJdbcIT` (scalar + join + explain smoke) |
| Commits | `5e6ab601`, `589db5e8` on `feat/flow-performace-statistics` |

**Note:** WI-315 replaces the interim full `EnumerableRules` bundle in `FlowTableScan.register()`
with a curated `FlowEnumerableRuleSets` on the same RelNode hook (not connection-level wiring).

## Goal

Introduce `FlowTableScan` (logical table scan RelNode) and make `FlowTable` implement
`TranslatableTable` so SQL and Substrait paths emit the custom scan instead of a generic
`TableScan`. Wire enumerable conversion so queries still execute via existing `scan()`.

## Scope

- `FlowTableScan` extends `TableScan`; created from `FlowTable.toRel()`.
- `FlowTable` implements `TranslatableTable` (retain `ScannableTable` for enumerable fallback).
- Register a converter rule so `prepareStatement` / JDBC execution works on flow connections.
- Unit tests using `FlowCalciteTestFixtures`: `toRel` returns `FlowTableScan`, SQL logical plan
  contains `FlowTableScan`, end-to-end SELECT still returns rows.

## Out of scope

- Filter/project pushdown (WI-312).
- Parquet column pruning (WI-313).
- Statistics and join policy (WI-314, WI-315).

## Implementation plan

### 1. Core RelNode (`mill-data-source-calcite`)

| Step | Action |
|------|--------|
| 1.1 | Add `FlowTableScan` extending `org.apache.calcite.rel.core.TableScan`, convention `NONE`, factory `create(cluster, relOptTable)`. |
| 1.2 | Override `copy(traitSet, inputs)` for planner mutations. |
| 1.3 | Change `FlowTable` to implement `TranslatableTable`; `toRel()` returns `FlowTableScan.create(...)`. Keep `scan(DataContext)` unchanged. |
| 1.4 | Optional: `sourceTable()` accessor for tests and enumerable bindable. |

### 2. Enumerable conversion

| Step | Action |
|------|--------|
| 2.1 | Add `FlowTableRules` (or `FlowEnumerableRules`) with `FlowTableScanToEnumerableRule`: matches `FlowTableScan` → `EnumerableTableScan` or custom `EnumerableFlowTableScan` that delegates to `FlowTable.scan()`. |
| 2.2 | Reuse Calcite's existing `EnumerableTableScan` + `ScannableTable` path if rule converts `FlowTableScan` → standard enumerable scan on same `RelOptTable` (simplest spike). |
| 2.3 | Register rules via `FlowTableScan.register()` so Volcano / `RelRunner.prepareStatement(rel)` discover them through `registerClass` (not `FlowContextFactory` `FrameworkConfig`). **Implemented:** `FlowTableScanToEnumerableRule` plus interim full `EnumerableRules` bundle; WI-315 replaces bundle with curated `FlowEnumerableRuleSets` on the same hook. |

### 3. Pipeline integration points

```
CalciteSqlProvider.parseSql     → Frameworks planner → toRel() on TranslatableTable
CalcitePlanConverter.toRelNode  → SubstraitRelNodeConverter → RelBuilder.scan → toRel()
CalciteExecutionProvider      → RelRunner.prepareStatement(rel) → enumerable rules
```

Both SQL and Substrait execution paths must emit `FlowTableScan` at logical layer before
enumerable conversion.

### 4. Tests (`FlowCalciteTestFixtures`)

| Test | Assert |
|------|--------|
| `FlowTable` implements `TranslatableTable` | `instanceof` |
| `toRel()` | Returns class `FlowTableScan` |
| SQL → `planner.rel()` | Tree contains `FlowTableScan` for `SELECT "id" FROM "users"` |
| JDBC `executeQuery` | Same row results as before |
| Skymill JDBC `testIT` | `SkymillJdbcTestFixtures` + `FlowTableSkymillJdbcIT` — scalar SELECT and JDBC explain on real dataset |
| `FlowTableTest` | Existing scan tests still pass |

### 5. Files (expected touch)

- `FlowTableScan.kt`, `FlowTableScanRules.kt` (new / delivered)
- `FlowTable.kt` (modify)
- `FlowTableScanPlannerTest.kt`, `FlowTableSkymillJdbcIT.kt` (tests)

## Acceptance

- `./gradlew :data:mill-data-source-calcite:test` passes.
- `./gradlew :data:mill-data-source-calcite:testIT` passes (Skymill JDBC smoke).
- `./gradlew :data:mill-data-backends:test` passes (no regression on flow execution).
- `EXPLAIN PLAN FOR` on a simple flow query completes; physical plan may show
  `EnumerableTableScan` until pushdown lands.

## Modules

- `data/mill-data-source-calcite`
- `data/mill-data-backends` (rule registration on flow Calcite connection if required)
