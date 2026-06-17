# WI-315 — Flow enumerable join policy (hash over merge on file scans)

## Status: **incomplete**

Partial scaffolding landed (Skymill fixtures, Hep-based plan-shape helpers, unit smoke tests).
Production join policy and flow-connection rule tuning are **not** implemented.

### Findings (review 2026-06-15)

| Area | Expected | Actual |
|------|----------|--------|
| `FlowEnumerableRuleSets` / curated rule set | New class; disable or deprioritise `EnumerableMergeJoinRule` on flow connections | **Missing** — no `mill-data-backends` changes |
| `FlowContextFactory` wiring | Flow-only enumerable rules | **Unchanged** — still default Calcite rule discovery |
| Interim rule registration | Temporary only | `FlowTableScan.register()` adds full `EnumerableRules` bundle (including hash join rules) so Volcano/JDBC execution works until this WI centralises a curated set |
| Skymill WITH `WHERE c2.id = c3.id` | Hash-biased plan via production Volcano/JDBC | **Not validated** — still regresses to merge join + sort on full backend path |
| Unit `shouldPreferHashJoin_whenCorrelatedCitiesFilter` | Proves WI-315 | Uses **Hep** planner in `SkymillCalciteTestFixtures.explainPhysicalPlan`, not JDBC Volcano; passing does **not** prove production behaviour |
| `SkymillTestFixtures` / `SkymillCalciteTestFixtures` | Test harness | **Done** — shared SQL, datasets, `assertHashJoinBiased` |
| `SkymillJdbcTestFixtures` (`testIT`) | JDBC Volcano path | **Done** (WI-311) — `FlowTableSkymillJdbcIT` for execute + JDBC explain |
| Timing budgets | testIT | **Deferred** to WI-316 |
| Table statistics (WI-314) | Preferred before hash build-side tuning | **Not started** — Volcano explore cost remains high on multi-join Skymill |

### Remaining work

1. Introduce `FlowEnumerableRuleSets` (or equivalent) on `FlowContextFactory` — start with **disable merge join** on flow.
2. Move enumerable rule registration out of `FlowTableScan.register()` into connection init; keep only `FlowTableScanToEnumerableRule` on the scan node (or register curated set once at connection level).
3. Enable hash-join assertions on **JDBC** `EXPLAIN PLAN FOR` / `SkymillJdbcTestFixtures` for `FULL_JOIN_SQL` (correlated cities filter).
4. Coordinate with WI-314 (row counts) if hash build-side selection still wrong after rule bias.
5. WI-316: timing IT + design docs.

## Goal

Bias Flow backend enumerable physical plans toward **hash join** instead of **merge join + sort**
on unsorted `FlowTable` scans, fixing plan-shape regressions (e.g. Skymill query with
`WHERE c2.id = c3.id` flipping top join from hash to merge).

## Scope

- Flow-specific enumerable rule set or rule priorities on the flow Calcite connection:
  prefer `EnumerableJoinRule` (hash), deprioritize or disable `EnumerableMergeJoinRule` where
  inputs are not sorted.
- Validate with `EXPLAIN PLAN FOR` on Skymill six-join queries (with and without correlated
  cities filter): top `c3` join should remain hash-friendly when statistics support it.
- No change to JDBC backend join policy.

## Out of scope

- DuckDB / external engine delegation.
- Full cost-model rewrite.
- Logical join reorder (`LoptOptimizeJoinRule`) — optional follow-up if hash bias insufficient.

## Background (Skymill)

| Query | Observed top join | Time (indicative) |
|-------|-------------------|-------------------|
| 6 joins, no `WHERE` | `EnumerableHashJoin` on `c3` | ~0.4–3s |
| Same + `WHERE c2.id = c3.id` | `EnumerableMergeJoin` + composite `Sort` | ~26–30s |

Filter pushdown into join keys (`AND(=($5,$6), =($3,$10))`) encourages merge join. Hash bias +
row counts (WI-314) should restore cheap probe on 14-row `cities`.

## Implementation plan

### 1. Flow enumerable rule set (`mill-data-backends`)

| Step | Action |
|------|--------|
| 1.1 | Introduce `FlowEnumerableRuleSets` (or extend flow connection init) with custom rule list. |
| 1.2 | Include standard enumerable rules **except** tune join rules: remove or lower priority of `EnumerableMergeJoinRule`. |
| 1.3 | Ensure `EnumerableJoinRule` (hash) remains and wins when inputs have `Convention.ENUMERABLE` and no collation. |
| 1.4 | Apply only to connections created by `FlowContextFactory` — JDBC backend unchanged. |

### 2. Configuration surface

| Option | Trade-off |
|--------|-----------|
| Hard disable merge join on flow | Simple; may regress queries that benefit from sorted inputs |
| Lower merge-join cost multiplier | Safer; tune with Skymill IT |
| Connection property `mill.flow.preferHashJoin=true` | Optional escape hatch |

Start with **disable merge join** on flow; re-enable selectively if IT finds regressions.

### 3. Interaction with statistics (WI-314)

Hash build side selection uses row counts — verify planner builds hash on `cities` (14 rows) not
`bookings` (1050). Add EXPLAIN assertions in WI-316.

### 4. Validation (Skymill SQL)

```sql
-- without WHERE: baseline hash at c3 join
-- with WHERE c2.id = c3.id: must not regress to all-merge chain
EXPLAIN PLAN FOR <six-join query>
```

Assert plan contains `EnumerableHashJoin` at top `c3` probe; minimal `EnumerableSort` on large
intermediates.

### 5. Tests

| Test | Location | Assert |
|------|----------|--------|
| Rule registration | unit | Flow connection planner includes custom rule set |
| EXPLAIN shape | testIT (WI-316) | Hash join present for Skymill WITH WHERE |
| Timing | testIT (WI-316) | WITH WHERE within budget of WITHOUT WHERE order-of-magnitude |

### 6. Files (expected touch)

- `FlowContextFactory.java` or new `FlowCalcitePlannerConfig.java`
- `FlowEnumerableRuleSets.java` (new)
- `FlowBackendAutoConfiguration.java` (wire if needed)
- Skymill explain assertions (may land in WI-316)

## Acceptance

- Skymill full join with `WHERE c2.id = c3.id` completes in same order of magnitude as without
  WHERE on Flow backend (target: well under prior ~30s on dev hardware; budget set in WI-316).
- Existing `./gradlew :data:mill-data-backends:test` passes.

## Modules

- `data/mill-data-backends`
- `data/mill-data-autoconfigure` (flow backend wiring if needed)
