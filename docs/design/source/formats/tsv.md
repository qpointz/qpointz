# TSV format

**Module:** `data/formats/mill-data-format-text`  
**Reader type:** `tsv`  
**Handler:** `TsvFormatHandler`

Tab-separated values with TSV-specific escape sequences (distinct from generic CSV
with `delimiter: "\t"`).

User configuration: [`docs/public/src/sources/formats/tsv.md`](../../../public/src/sources/formats/tsv.md).

## Feature support

| Feature | Support | Notes |
|---------|---------|-------|
| Flow source read | Yes | `TsvRecordSource` |
| Schema inference | Yes | Header row or explicit `headers` list |
| Native typed columns | No | All columns inferred as nullable `STRING` |
| Record statistics | Approx | Same line heuristic as CSV (`TextLineRecordStatisticReader`) |
| Seekable blob access | No | Sequential stream scan |
| Data export (SPI) | Yes | `TsvExportFormatProvider` |
| Record writer | Yes | `TsvRecordWriter` |
| Format configuration | Yes | TSV dialect settings via `TsvSettings` |
| Multi-entity selection | — | One table stream per blob |
| Column projection pushdown | No | Not implemented |

## Implementation notes

- Shares text statistics implementation with CSV/FWF; same quoted-newline over-count risk.
- Export and read paths share Univocity-based text stack with CSV/FWF module.
