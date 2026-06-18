# JSON format (export)

**Module:** `data/formats/mill-data-format-json`  
**Reader type:** — (export only)  
**Provider:** `JsonExportFormatProvider`

JSON array streaming for query **export** only. There is no `FormatHandler` for
ingesting JSON files as flow sources.

## Feature support

| Feature | Support | Notes |
|---------|---------|-------|
| Flow source read | No | Export-only module |
| Schema inference | No | — |
| Native typed columns | No | — |
| Record statistics | No | — |
| Seekable blob access | No | — |
| Data export (SPI) | Yes | `JsonArrayStreamingEncoder` |
| Record writer | No | — |
| Format configuration | No | Export MIME/type fixed by provider |
| Multi-entity selection | — | — |
| Column projection pushdown | No | — |

## Implementation notes

- Included in the capability matrix for completeness alongside binary/text source formats.
- For JSON **ingest**, use a dedicated source or convert upstream; not in scope today.
