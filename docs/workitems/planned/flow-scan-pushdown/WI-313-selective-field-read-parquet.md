# WI-313 — Selective field read and Parquet column projection

**Story:** [`flow-scan-pushdown`](STORY.md) · **Prerequisite:**
[`flow-translatable-table-scan`](../../completed/20260618-flow-translatable-table-scan/STORY.md) (WI-311 done).

## Status: **not started**

`SourceTable` has no projection API; `ParquetRecordSource` still reads full rows. Depends on WI-312
for `FlowTableScan.projects` → column names.

## Goal

Extend the source layer so scans request only required columns and Parquet reads skip unused
column chunks at the format level (not just after full-row decode).

## Scope

- `SourceTable` API extension: `records(projection: Set<String>)` and/or
  `vectorBlocks(projection, batchSize)` with default full-schema behaviour for callers that omit
  projection.
- `MultiFileSourceTable` forwards projection to underlying `RecordSource` instances.
- Parquet: replace or supplement row-only `AvroParquetReader` path with projected schema read
  (footer/schema projection; sum row counts unchanged).
- Avro/CSV: logical projection only (skip unmapped fields when building `Record`).
- Tests: wide Parquet fixture, query two columns, assert reader does not load omitted columns
  (spy, column stats, or projected schema on reader).

## Out of scope

- Vectorized execution as primary path (optional follow-up).
- External query engines.
- Partition pruning by blob path (separate story).

## Implementation plan

### 1. Source API (`mill-data-source-core`)

| Step | Action |
|------|--------|
| 1.1 | Define `ColumnProjection` (field names or indices) on `SourceTable`. |
| 1.2 | `MultiFileSourceTable.records(projection)` passes projection to each `RecordSource`. |
| 1.3 | `ConcatenatingRecordIterator` unchanged; each source honours projection independently. |
| 1.4 | Wire from WI-312 enumerable bindable: map `FlowTableScan.projects` → `Set<String>` via schema field names. |

### 2. Parquet physical read (`mill-data-format-parquet`)

| Step | Action |
|------|--------|
| 2.1 | Today: `ParquetRecordSource` uses `AvroParquetReader` → full row `GenericRecord`. |
| 2.2 | Add projected read path: build Parquet schema subset from requested column names (match Mill `RecordSchema` fields to Parquet footer schema). |
| 2.3 | Use Parquet column IO / `ParquetReader` with projected schema (avoid reading omitted column chunks). |
| 2.4 | `ParquetFormatHandler.createRecordSource(..., projection?)` or wrap source when projection non-null. |
| 2.5 | Reuse existing footer read in `inferSchema` — extend with `readRowCount()` helper for WI-314. |

### 3. Row formats (logical only)

| Format | Approach |
|--------|----------|
| Avro | Read full record; map only projected fields into `Record` (cheap for small files). |
| CSV/TSV | Univocity: select column indexes when parser supports it, else skip after parse. |
| Excel | Skip unrequested columns when mapping rows. |

### 4. Flow / Calcite layer

No new RelNodes — WI-312 already passes projection into scan; this WI makes `FlowTable.scan` /
enumerable bindable call `sourceTable.records(projection)`.

### 5. Tests

| Test | Module | Assert |
|------|--------|--------|
| Wide Parquet fixture (≥5 columns) | `mill-data-format-parquet` | Project 2 columns; reader schema has 2 fields |
| Integration | `mill-data-source-calcite` | E2E SELECT two columns via pushed project |
| Spy / metric | optional | Column read count or mock `InputFile` access |

### 6. Files (expected touch)

- `SourceTable.kt`, `MultiFileSourceTable.kt`
- `ParquetRecordSource.kt`, `ParquetFormatHandler.kt`
- `FlowEnumerableRules.kt` (pass projection to source)
- `ParquetSelectiveReadTest.kt` (new)

## Acceptance

- Parquet selective-read tests pass in `mill-data-format-parquet`.
- Flow pushdown from WI-312 passes projection into Parquet sources end-to-end.

## Modules

- `data/mill-data-source-core`
- `data/formats/mill-data-format-parquet`
- `data/mill-data-source-calcite`
