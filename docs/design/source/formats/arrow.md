# Arrow format

**Module:** `data/formats/mill-data-format-arrow`  
**Reader type:** `arrow`  
**Handler:** `ArrowFormatHandler`

Apache Arrow IPC (stream and file variants). Columnar layout with explicit schema
metadata; timestamp timezone is column-level.

User configuration: [`docs/public/src/sources/formats/arrow.md`](../../../public/src/sources/formats/arrow.md).  
Phased design (Flight deferred): [`../arrow-format-design.md`](../arrow-format-design.md).

## Feature support

| Feature | Support | Notes |
|---------|---------|-------|
| Flow source read | Yes | IPC stream/file → record batches |
| Schema inference | Yes | Arrow schema from IPC header |
| Native typed columns | Yes | Full Mill ↔ Arrow type contract |
| Record statistics | Yes | Sum of batch `rowCount` across IPC payload |
| Seekable blob access | Yes | IPC file layout; stream uses sequential read |
| Data export (SPI) | No | `ArrowExportFormatProvider` exists; SPI file not registered |
| Record writer | Yes | `ArrowRecordWriter` (IPC stream) |
| Format configuration | No | Self-describing; no `format:` section |
| Multi-entity selection | — | One table stream per blob |
| Column projection pushdown | No | Not implemented |

## Implementation notes

- `RecordStatisticReader` aggregates row counts lazily (same provider wiring as Parquet).
- Timezone: `timestamp(unit, timezone)` normalized to UTC instant; per-row zones not supported.
- Export SPI wiring is a follow-up; read path and writer are in place.
