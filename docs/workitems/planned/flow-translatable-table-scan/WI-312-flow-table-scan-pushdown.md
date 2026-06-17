# WI-312 — Filter and project pushdown into FlowTableScan

## Goal

Add planner rules that merge single-table `Filter` and `Project` operators into `FlowTableScan`
so predicates and column masks are available at scan time instead of above a full row decode.

## Scope

- Extend `FlowTableScan` to carry optional pushed filter (`RexNode`) and/or project field indices.
- Hep/Volcano rules: `Filter` → `FlowTableScan`, `Project` → `FlowTableScan` (single-table only).
- Enumerable implementation applies filter/project when iterating `SourceTable` (logical projection
  at minimum: skip unused fields when building `Object[]`).
- Contract tests via `FlowCalciteTestFixtures`: `SELECT "name" FROM "users" WHERE "id" = 2` has
  no `Filter` directly above `FlowTableScan` after optimization.

## Out of scope

- Physical Parquet column read (WI-313).
- Join/filter correlation pushdown across tables.
- Partition blob pruning (future story S-10; requires same pushed-predicate hook).

## Implementation plan

### 1. Extend `FlowTableScan` state

| Field | Type | Purpose |
|-------|------|---------|
| `pushedFilter` | `RexNode?` | Single-table predicates merged from `LogicalFilter` |
| `projects` | `ImmutableIntList?` | Output column indices into table schema (from `LogicalProject`) |

Use immutable copy-on-write in rule `convert()` methods; preserve `table` and row type via
`RelOptTable` / `deriveRowType()`.

### 2. Planner rules (`mill-data-source-calcite`)

| Rule | Pattern | Action |
|------|---------|--------|
| `FlowFilterTableScanRule` | `Filter` → `FlowTableScan` | AND merge into `pushedFilter`; remove filter node |
| `FlowProjectTableScanRule` | `Project` → `FlowTableScan` | Compose project indices; remove project node if trivial |

Register in Hep program (order: project pushdown, then filter pushdown) on flow planner config.
Start with **single-condition, single-table** filters only; reject correlated or non-deterministic
Rex until explicitly supported.

### 3. Enumerable execution

| Step | Action |
|------|--------|
| 3.1 | Update `FlowTableScanToEnumerableRule` (WI-311) to pass filter + project mask into bindable. |
| 3.2 | In bindable / scan loop: evaluate `pushedFilter` per row (or compile to `Predicate`) before emit. |
| 3.3 | When building `Object[]`, include only `projects` indices (logical projection — full row may still be read from source until WI-313). |
| 3.4 | Support `LIMIT` pushdown as follow-up if Calcite exposes `Sort`+`Fetch` on scan (optional stretch). |

### 4. `SourceTable` hook (minimal for this WI)

Add default methods or overload on `SourceTable` / scan entry used by enumerable bindable:

```kotlin
fun records(projection: Set<String>? = null): Iterable<Record>
```

Default implementation ignores projection (delegates to existing `records()`); WI-313 implements
Parquet physical projection.

### 5. Tests

| Test | Assert |
|------|--------|
| Filter pushdown | No `Filter` parent directly above `FlowTableScan` after Hep |
| Project pushdown | `FlowTableScan.projects` equals expected indices |
| End-to-end | Filtered SELECT returns correct subset of rows |
| Regression | Full-table SELECT unchanged |

### 6. Files (expected touch)

- `FlowTableScan.kt` (fields + copy)
- `FlowFilterTableScanRule.kt`, `FlowProjectTableScanRule.kt` (new)
- `FlowEnumerableRules.kt` (bindable applies filter/project)
- `SourceTable.kt` / `MultiFileSourceTable.kt` (optional projection parameter)
- `FlowTableScanPushdownTest.kt` (new)

## Acceptance

- Pushdown tests in `mill-data-source-calcite` pass.
- Skymill single-table filtered query shows fewer decoded columns in tests or logging (if asserted).

## Modules

- `data/mill-data-source-calcite`
- `data/mill-data-backends`
- `data/mill-data-source-core` (optional `SourceTable` API)
