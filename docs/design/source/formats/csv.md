# CSV format

**Module:** `data/formats/mill-data-format-text`  
**Reader type:** `csv`  
**Handler:** `CsvFormatHandler`

Delimited text (default comma). Parsing via Univocity; values are read as nullable
`STRING` unless cast downstream.

User configuration: [`docs/public/src/sources/formats/csv.md`](../../../public/src/sources/formats/csv.md).

## Feature support

| Feature | Support | Notes |
|---------|---------|-------|
| Flow source read | Yes | `CsvRecordSource` streaming parser |
| Schema inference | Yes | Header row or explicit `headers` list |
| Native typed columns | No | All columns inferred as nullable `STRING` |
| Record statistics | Approx | Physical line count minus header (`TextLineRecordStatisticReader`) |
| Seekable blob access | No | Sequential stream scan for stats and data |
| Data export (SPI) | Yes | `CsvExportFormatProvider` |
| Record writer | Yes | `CsvRecordWriter` |
| Format configuration | Yes | Delimiter, quotes, header, whitespace, limits, auto-detect |
| Multi-entity selection | — | One table stream per blob |
| Column projection pushdown | No | Not implemented |

## Implementation notes

- **Record statistics caveat:** line-based count ignores CSV quoting rules; embedded
  newlines inside quoted fields inflate the estimate (e.g. Skymill `passenger.csv`).
- Stats use `lineSeparator` from format descriptor (default `\n`); header row subtracted
  when `hasHeader=true`.
- TSV is a separate reader type with native escape handling; CSV can use `delimiter: "\t"`.
