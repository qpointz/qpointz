# WI-314 — Flow table statistics for Calcite planner

## Goal

Expose table statistics on `FlowTable` so Calcite can estimate row counts, prefer hash join build
sides, and order joins for small file-backed tables.

## Scope

- `FlowTable.getStatistic()` returning `Statistics.of(rowCount, keys, …)`.
- Row count from Parquet footer (`recordCount`) at materialization; sum per logical table.
- Optional primary-key bitsets on `id` columns (descriptor convention or metadata facet bridge
  later).
- Cache counts on `SourceTable` / resolved source; no per-query full scan for stats.
- Unit test: statistic row count matches known Skymill table sizes.

## Out of scope

- Column NDV / histograms (future custom metadata).
- Collations unless files are physically sorted.
- `RelDistribution` from Hive partitions (future story S-10).

## Implementation plan

### 1. Statistics model

| Calcite `Statistic` field | Flow source | Notes |
|---------------------------|-------------|-------|
| `getRowCount()` | Sum of per-file counts | Required for WI-315 |
| `getKeys()` | PK columns (e.g. `id`) | `ImmutableBitSet.of(fieldIndex)` |
| `getCollations()` | empty | Unless file sorted (don't fake) |
| `getDistribution()` | `ANY` | Until partition story |

### 2. Row count collection (`mill-data-source-core` + Parquet)

| Step | Action |
|------|--------|
| 2.1 | Add optional `estimatedRowCount: Long?` on resolved table metadata (e.g. `ResolvedSourceTable` wrapper or field on custom `SourceTable` impl). |
| 2.2 | At `SourceResolver.resolve`: for each blob, read Parquet footer `recordCount` via existing `ParquetFileReader.open` pattern in `ParquetFormatHandler.inferSchema`. |
| 2.3 | Sum counts across blobs in `MultiFileSourceTable`; Avro/CSV fallback: null (unknown) or optional full scan cache at materialization. |
| 2.4 | Store on object reachable from `FlowTable.sourceTable()`. |

### 3. Key inference

| Step | Action |
|------|--------|
| 3.1 | Convention: single column named `id` → unique key bitset. |
| 3.2 | Optional YAML/table descriptor flag later (`primaryKey: [id]`). |
| 3.3 | Metadata facet bridge deferred. |

### 4. Calcite wiring (`mill-data-source-calcite`)

| Step | Action |
|------|--------|
| 4.1 | Override `FlowTable.getStatistic()`. |
| 4.2 | Return `Statistics.UNKNOWN` when row count unavailable (planner behaves as today). |
| 4.3 | Verify `RelMetadataQuery.getRowCount` on `FlowTableScan` picks up table stats. |

### 5. Tests

| Test | Assert |
|------|--------|
| Skymill materialization | `cities` ≈ 14, `passenger` ≈ 210, `bookings` ≈ 1050 |
| `getStatistic().rowCount` | Matches sum of Parquet footers |
| Planner smoke | EXPLAIN row estimate changes vs unknown (optional RelOpt test) |

### 6. Files (expected touch)

- `FlowTable.kt` (`getStatistic()`)
- `ParquetFormatHandler.kt` (row count helper)
- `SourceResolver.kt` or `MultiFileSourceTable` (aggregate count)
- `FlowTableStatisticsTest.kt` (new)

## Acceptance

- Calcite `EXPLAIN` or planner tests show improved row estimates for flow tables.
- `./gradlew :data:mill-data-source-calcite:test` passes.

## Modules

- `data/mill-data-source-calcite`
- `data/formats/mill-data-format-parquet`
- `data/mill-data-source-core`
