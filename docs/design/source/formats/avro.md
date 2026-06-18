# Avro format

**Module:** `data/formats/mill-data-format-avro`  
**Reader type:** `avro`  
**Handler:** `AvroFormatHandler`

Apache Avro OCF (Object Container File). Self-describing schema in the file header;
row-oriented read through `GenericDatumReader`.

User configuration: [`docs/public/src/sources/formats/avro.md`](../../../public/src/sources/formats/avro.md).

## Feature support

| Feature | Support | Notes |
|---------|---------|-------|
| Flow source read | Yes | `AvroRecordSource` sequential block read |
| Schema inference | Yes | Embedded Avro schema in OCF header |
| Native typed columns | Yes | Avro schema → Mill via `AvroSchemaConverter` |
| Record statistics | No | No `RecordStatisticReader`; full scan would be required |
| Seekable blob access | No | Sequential `DataFileStream` read |
| Data export (SPI) | Yes | `AvroExportFormatProvider` |
| Record writer | Yes | `AvroRecordWriter` (OCF) |
| Format configuration | No | Self-describing; no `format:` section |
| Multi-entity selection | — | One table stream per blob |
| Column projection pushdown | No | Not implemented |

## Implementation notes

- Tables without record statistics omit table-level row estimates in Calcite (strict
  multi-blob rule: all blobs must expose stats or none do).
- Export uses vector batches via `AvroVectorExportSupport`.
- Optional future: block-index row counts if Avro metadata exposes them.
