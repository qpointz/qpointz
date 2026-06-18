# Source format design

Design notes for Mill **flow source** format handlers (`FormatHandler` SPI) and related
export/write paths. User-facing configuration examples live under
[`docs/public/src/sources/formats/`](../../../public/src/sources/formats/).

## Capability matrix

Each format page includes the **same** feature table. Values use:

| Symbol | Meaning |
|--------|---------|
| **Yes** | Supported for typical use |
| **Approx** | Supported with known limitations (see Notes) |
| **Partial** | Incomplete or descriptor-only |
| **No** | Not implemented |
| **—** | Not applicable to this format |

### Feature rows (fixed order)

| Feature | What it measures |
|---------|------------------|
| Flow source read | Blob → `RecordSource` via `SourceDescriptor` reader type |
| Schema inference | Column names/types without an external schema file |
| Native typed columns | Logical types beyond nullable `STRING` |
| Record statistics | Row-count estimates for Calcite planner (`RecordStatisticReader`) |
| Seekable blob access | Random access used for footer/metadata or stats |
| Data export (SPI) | Query result streaming via `ExportFormatProvider` |
| Record writer | Programmatic blob write via `FlowRecordWriter` |
| Format configuration | YAML `format:` dialect and parser options |
| Multi-entity selection | Multiple logical entities per blob (e.g. Excel sheets) |
| Column projection pushdown | Predicate/projection pushed into format reader |

## Format pages

| Format | Module | Design doc |
|--------|--------|------------|
| Parquet | `data/formats/mill-data-format-parquet` | [parquet.md](parquet.md) |
| Arrow | `data/formats/mill-data-format-arrow` | [arrow.md](arrow.md) |
| Avro | `data/formats/mill-data-format-avro` | [avro.md](avro.md) |
| CSV | `data/formats/mill-data-format-text` | [csv.md](csv.md) |
| TSV | `data/formats/mill-data-format-text` | [tsv.md](tsv.md) |
| FWF | `data/formats/mill-data-format-text` | [fwf.md](fwf.md) |
| Excel | `data/formats/mill-data-format-excel` | [excel.md](excel.md) |
| JSON | `data/formats/mill-data-format-json` | [json.md](json.md) (export only) |

Related: [Arrow phased design](../arrow-format-design.md), [mill-source-calcite](../mill-source-calcite.md),
[flow-backend](../flow-backend.md).

## Summary (all formats)

| Feature | Parquet | Arrow | Avro | CSV | TSV | FWF | Excel | JSON |
|---------|---------|-------|------|-----|-----|-----|-------|------|
| Flow source read | Yes | Yes | Yes | Yes | Yes | Yes | Yes | No |
| Schema inference | Yes | Yes | Yes | Yes | Yes | Partial | Yes | No |
| Native typed columns | Yes | Yes | Yes | No | No | No | No | No |
| Record statistics | Yes | Yes | No | Approx | Approx | Approx | No | No |
| Seekable blob access | Yes | Yes | No | No | No | No | No | No |
| Data export (SPI) | No | No | Yes | Yes | Yes | No | Yes | Yes |
| Record writer | Yes | Yes | Yes | Yes | Yes | Yes | No | No |
| Format configuration | No | No | No | Yes | Yes | Yes | Yes | No |
| Multi-entity selection | — | — | — | — | — | — | Yes | — |
| Column projection pushdown | No | No | No | No | No | No | No | No |

Per-format notes (e.g. approximate stats caveats, Arrow export SPI gap) are on each format page.

**Follow-up:** `format.statistics.mode` (`none` | `approximate` | `exact`; `none` = disabled) —
[format-statistics-descriptor.md](../format-statistics-descriptor.md).
