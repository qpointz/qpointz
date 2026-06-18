# WI-313 — Selective field read and Parquet column projection

**Story:** [`flow-scan-pushdown`](STORY.md) · **Backlog:** D-10  
**Prerequisite:** WI-312 complete (`FlowTableScan.projects` → column names at execution)

## Status: **not started**

`SourceTable.records()` has no projection parameter; `ParquetRecordSource` still reads full rows via
`AvroParquetReader`.

## Goal

Extend the source layer so scans request only required columns and Parquet reads skip unused
column chunks at the format level (not just after full-row decode).

## Scope

- `SourceTable` API: `records(projection: Set<String>?)` with default full-schema behaviour.
- `MultiFileSourceTable` forwards projection to underlying `RecordSource` instances.
- Parquet: projected schema read (footer subset; avoid omitted column chunks).
- Avro/CSV/Excel: logical projection only (skip unmapped fields when building `Record`).
- Tests: wide Parquet fixture, query two columns, assert reader schema / IO uses projection.

## Out of scope

- Vectorized execution as primary path.
- External query engines.
- Partition pruning by blob path (story S-10).

## Architecture

WI-312 enumerable bindable maps `FlowTableScan.projects` → `Set<String>` via
`FlowTable.sourceTable().schema.fieldNames`. This WI makes that set reach format handlers:

```
FlowTableScan.projects
  → SourceTableScan / FlowTable.scan
  → SourceTable.records(projection)
  → MultiFileSourceTable → ParquetFormatHandler.createRecordSource(..., projection)
  → ParquetRecordSource (projected ParquetReader schema)
```

No new RelNodes.

## Implementation plan

### 1. Source API (`mill-data-source-core`)

| Step | Action |
|------|--------|
| 1.1 | Add `records(projection: Set<String>? = null)` on `SourceTable`; default calls `records()`. |
| 1.2 | `MultiFileSourceTable.records(projection)` passes projection to each `RecordSource`. |
| 1.3 | `ConcatenatingRecordIterator` unchanged; each source honours projection independently. |
| 1.4 | Wire from WI-312: enumerable path passes `Set<String>` from project indices. |

Files:

- `data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/SourceTable.kt`
- `data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/MultiFileSourceTable.kt`

### 2. Parquet physical read (`mill-data-format-parquet`)

| Step | Action |
|------|--------|
| 2.1 | Today: `ParquetRecordSource` uses `AvroParquetReader` → full row `GenericRecord`. |
| 2.2 | Build Parquet schema subset from requested column names (match Mill `RecordSchema` to footer). |
| 2.3 | Use `ParquetReader` with projected schema — avoid reading omitted column chunks. |
| 2.4 | `ParquetFormatHandler.createRecordSource(..., projection?)` or wrap when projection non-null. |
| 2.5 | Reuse footer read from `inferSchema` / `RecordStatisticReader` (WI-314). |

Files:

- `data/formats/mill-data-format-parquet/src/main/kotlin/io/qpointz/mill/source/format/parquet/ParquetRecordSource.kt`
- `data/formats/mill-data-format-parquet/src/main/kotlin/io/qpointz/mill/source/format/parquet/ParquetFormatHandler.kt`

### 3. Row formats (logical only)

| Format | Handler module | Approach |
|--------|----------------|----------|
| Avro | `mill-data-format-avro` | Read full record; map only projected fields |
| CSV/TSV/FWF | `mill-data-format-text` | Column indexes in parser or skip after parse |
| Excel | `mill-data-format-excel` | Skip unrequested columns when mapping |

### 4. Calcite layer

Update `SourceTableScan` / `FlowTable.scan` path to call `sourceTable.records(projection)` when
`FlowTableScan.projects` is set (WI-312 wiring point).

Files:

- `data/mill-data-source-calcite/src/main/kotlin/io/qpointz/mill/source/calcite/SourceTableScan.kt`
- `data/mill-data-source-calcite/src/main/kotlin/io/qpointz/mill/source/calcite/FlowTable.kt` (if needed)

### 5. Tests

| Test | Module | Assert |
|------|--------|--------|
| Wide Parquet fixture (≥5 columns) | `mill-data-format-parquet` | Project 2 columns; reader / schema has 2 fields |
| Integration | `mill-data-source-calcite` | E2E SELECT two columns via pushed project |
| Spy / metric | optional | Projected schema on reader or column read count |

Suggested new test: `ParquetSelectiveReadTest.kt` under
`data/formats/mill-data-format-parquet/src/test/kotlin/...`

### 6. Docs

- `docs/design/source/formats/parquet.md` — column projection → Yes
- `docs/design/source/mill-source-calcite.md` — row-oriented scan limitation

## Commands

```bash
./gradlew :data:formats:mill-data-format-parquet:test --tests "*ParquetSelectiveRead*"
./gradlew :data:mill-data-source-calcite:test --tests "io.qpointz.mill.source.calcite.FlowTableScanPushdownTest"
./gradlew :data:mill-data-backends:testIT --tests "io.qpointz.mill.data.backend.flow.SkymillJoinPerformanceIT"
```

## Acceptance

- [ ] Parquet selective-read tests pass in `mill-data-format-parquet`.
- [ ] Flow pushdown passes projection into Parquet sources end-to-end.
- [ ] Public/design format docs updated.

## Modules

- `data/mill-data-source-core`
- `data/formats/mill-data-format-parquet`
- `data/mill-data-source-calcite`
