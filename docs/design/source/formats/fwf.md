# Fixed-width (FWF) format

**Module:** `data/formats/mill-data-format-text`  
**Reader type:** `fwf`  
**Handler:** `FwfFormatHandler`

Fixed-width text: column boundaries defined by character positions in YAML, not delimiters.

User configuration: [`docs/public/src/sources/formats/fwf.md`](../../../public/src/sources/formats/fwf.md).

## Feature support

| Feature | Support | Notes |
|---------|---------|-------|
| Flow source read | Yes | `FwfRecordSource` |
| Schema inference | Partial | Schema from `format.columns` only; blob not read for inference |
| Native typed columns | No | All columns defined as nullable `STRING` |
| Record statistics | Approx | Physical line count minus optional header row |
| Seekable blob access | No | Sequential stream scan |
| Data export (SPI) | No | No export provider; use CSV/TSV export |
| Record writer | Yes | `FwfRecordWriter` |
| Format configuration | Yes | Required column positions, header flag, line separator |
| Multi-entity selection | — | One table stream per blob |
| Column projection pushdown | No | Not implemented |

## Implementation notes

- Unlike CSV, schema is **descriptor-driven** — misconfigured column ranges fail at read time.
- Line-based statistics treat each physical line as one record (no embedded-field newlines).
