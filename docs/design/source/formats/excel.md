# Excel format

**Module:** `data/formats/mill-data-format-excel`  
**Reader type:** `excel`  
**Handler:** `ExcelFormatHandler`

Microsoft Excel workbooks (`.xlsx`, `.xls`) via Apache POI. Multiple sheets can be
selected and concatenated into one logical table stream.

User configuration: [`docs/public/src/sources/formats/excel.md`](../../../public/src/sources/formats/excel.md).

## Feature support

| Feature | Support | Notes |
|---------|---------|-------|
| Flow source read | Yes | `SheetRecordSource` / `WorkbookRecordSource` |
| Schema inference | Yes | Header row on first selected sheet |
| Native typed columns | No | Schema columns are `STRING`; cells retain runtime types |
| Record statistics | No | No `RecordStatisticReader`; row count needs sheet scan |
| Seekable blob access | No | Workbook opened as stream via POI |
| Data export (SPI) | Yes | `XlsxExportFormatProvider` |
| Record writer | No | Read + export only |
| Format configuration | Yes | Header flag, sheet selector (name/index/pattern) |
| Multi-entity selection | Yes | `SheetSelector` — one or many sheets per blob |
| Column projection pushdown | No | Not implemented |

## Implementation notes

- Selected sheets must share compatible column layout when concatenated.
- `ExcelSchemaInferer` drives column names from header or positional columns.
- Without record statistics, multi-blob Excel tables do not contribute Calcite row estimates.
