# Parquet format

**Module:** `data/formats/mill-data-format-parquet`  
**Reader type:** `parquet`  
**Handler:** `ParquetFormatHandler`

Columnar binary format with embedded schema in the file footer. Primary analytical
format for Mill flow sources; schema and row counts are read without scanning data pages.

User configuration: [`docs/public/src/sources/formats/parquet.md`](../../../public/src/sources/formats/parquet.md).

## Feature support

| Feature | Support | Notes |
|---------|---------|-------|
| Flow source read | Yes | `ParquetRecordSource` via Avro record bridge |
| Schema inference | Yes | Footer metadata via `ParquetFileReader` + `BlobInputFile` |
| Native typed columns | Yes | Parquet physical + logical annotations → Mill types |
| Record statistics | Yes | Exact row count from footer (`RecordStatisticReader`) |
| Seekable blob access | Yes | Required for footer read and Parquet reader |
| Data export (SPI) | No | No `ExportFormatProvider`; use Arrow/Avro/CSV export |
| Record writer | Yes | `ParquetRecordWriter` (regression/tooling) |
| Format configuration | No | Self-describing; no `format:` section |
| Multi-entity selection | — | One table stream per blob |
| Column projection pushdown | No | Planned — [`flow-scan-pushdown`](../../../workitems/planned/flow-scan-pushdown/STORY.md) / WI-313 |

## Implementation notes

- Storage-agnostic I/O through `BlobInputFile` / `BlobOutputFile` (no Hadoop FS).
- Statistics feed `SourceTable.statisticProviders()` → `FlowTable.getStatistic()` /
  `FlowTableScan.estimateRowCount()` for Calcite join planning.
- Type mapping: `ParquetSchemaConverter` (see public type table).
