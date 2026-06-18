# WI-315 — Flow enumerable join policy (hash over merge on file scans)

## Status: **complete** (2026-06-15)

### Delivered

| Area | Result |
|------|--------|
| `FlowEnumerableRuleSets` | Curated enumerable rules; excludes `EnumerableMergeJoinRule`; removes pre-registered merge rules and sets exclusion filter on JDBC Volcano planners |
| `FlowTableScan.register()` | `FlowTableScanToEnumerableRule` + `FlowEnumerableRuleSets.register()` |
| `FlowRelPlannerRules` | Hep explain from `registerClass` on logical tree (no duplicated rule list in fixtures) |
| `SkymillJoinFixturesTest` | `shouldPreferHashJoin_whenCorrelatedCitiesFilter` enabled; `RelRunner.prepareStatement` on logical 6-join plan |
| Module | `mill-data-source-calcite` only — no `FlowContextFactory` rule wiring |

### Architecture decision

Rule registration on **Mill RelNodes** (`FlowTableScan.register`), not JDBC `FrameworkConfig`. Programmatic
`RelRunner.prepareStatement` and JDBC `EXPLAIN` both use Volcano; JDBC planners may preload full
`EnumerableRules` — `FlowEnumerableRuleSets.register` removes merge join and sets
`setRuleDescExclusionFilter` when `FlowTableScan` is first seen.

## Goal

Bias Flow enumerable physical plans toward **hash join** instead of **merge join + sort** on
unsorted `FlowTable` scans.

## Acceptance

- [x] `./gradlew :data:mill-data-source-calcite:test` passes
- [x] Hash-biased plan on `FULL_JOIN_WITH_CITIES_FILTER` (Hep + unit assert)
- [x] `RelRunner.prepareStatement(logical)` succeeds on Skymill 6-join SQL

## Modules

- `data/mill-data-source-calcite`
