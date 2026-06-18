# Format capabilities

Quick reference for what each **source reader format** supports when you configure a flow
data source. Configuration examples for each format are on the linked format pages.

| Capability | Parquet | Arrow | Avro | CSV | TSV | FWF | Excel |
|------------|---------|-------|------|-----|-----|-----|-------|
| [Native typed schema](#native-typed-schema) | Yes | Yes | Yes | No | No | No | No |
| [Row count estimates](#row-count-estimates) | Yes | Yes | No | Approx | Approx | Approx | No |
| [Query result export](#query-result-export) | No | No | Yes | Yes | Yes | No | Yes |
| [YAML `format:` options](#yaml-format-options) | No | No | No | Yes | Yes | Yes | Yes |

**Legend:** **Yes** — supported as described · **Approx** — useful but not exact · **No** — not available today

Additional export-only formats (no source reader): **JSON** (`json`) — see [Query result export](#query-result-export).

Per-format configuration: [CSV](formats/csv.md) · [TSV](formats/tsv.md) · [FWF](formats/fwf.md) · [Excel](formats/excel.md) · [Avro](formats/avro.md) · [Parquet](formats/parquet.md) · [Arrow](formats/arrow.md)

---

## Native typed schema

Mill infers column **names and logical types** (integers, dates, timestamps, and so on) directly
from the file for self-describing binary formats.

| Support | Formats |
|---------|---------|
| **Yes** | Parquet, Arrow, Avro — types come from embedded schema metadata |
| **No** | CSV, TSV, FWF, Excel — schema columns are nullable `STRING`; cast in SQL or via [table attributes](configuration.md#table-attributes) if needed |

Excel still reads cell values with natural runtime types during parsing; the declared schema
remains all-string for compatibility across sheets.

---

## Row count estimates

When you query flow sources through Calcite, Mill can supply **estimated row counts** to help
the SQL planner choose join and scan strategies. Estimates are derived from file metadata or
cheap scans — not from running your full query.

| Support | Formats | Notes |
|---------|---------|-------|
| **Yes** | Parquet, Arrow | Exact counts from file footer (Parquet) or IPC batch metadata (Arrow) |
| **Approx** | CSV, TSV, FWF | Physical line count minus header when `hasHeader: true`; can **over-count** when quoted or escaped fields contain embedded newlines |
| **No** | Avro, Excel | No row estimate today; planner treats row counts as unknown |

For tables backed by **multiple files**, every file in the table must support row estimates for
Mill to publish a table-level estimate.

### Controlling statistics (planned)

A follow-up will add a single setting under the reader's `format:` block:

```yaml
format:
  type: csv
  hasHeader: true
  statistics:
    mode: approximate   # none | approximate | exact
```

| Mode | Meaning |
|------|---------|
| `none` | Disabled — no row estimates for this reader |
| `approximate` | Fast estimate (default for CSV/TSV/FWF; line count) |
| `exact` | Full count (Parquet/Arrow metadata; text = full parse, costly) |

Parquet and Arrow allow `exact` or `none` only. Design detail:
[format statistics descriptor design](../../../design/source/format-statistics-descriptor.md).

---

## Query result export

Separate from **reading** source files: Mill can **stream query results** over HTTP in several
formats when the export service is enabled (`mill.data.services.export.enable`). Available
export ids depend on classpath and `mill.data.services.export.formats`.

| Export id | Typical use |
|-----------|-------------|
| `csv` | Delimited download from Analysis or Model export |
| `tsv` | Tab-separated download |
| `avro` | Binary Avro OCF stream |
| `xlsx` | Excel workbook (single sheet) |
| `json` | UTF-8 JSON array (export only — no JSON **source** reader) |

Parquet and Arrow are strong **source** formats but are not offered as HTTP export targets today.
FWF is read-only in Mill (no export provider).

See [Platform runtime — HTTP data export](../reference/platform-runtime.md#http-data-export-servicesexport).

---

## YAML `format:` options

Some readers accept a `format:` block under the reader for dialect and parser behaviour. Binary
formats that carry their own schema generally need no extra options.

| Support | Formats |
|---------|---------|
| **No** | Parquet, Arrow, Avro — specify `type` only |
| **Yes** | CSV, TSV, FWF, Excel — delimiter/quote settings, headers, column layouts, sheet selection, etc. |

FWF **requires** `format.columns` (fixed-width layout); Mill does not infer column positions from
the file alone.

---

## Excel-specific: sheet selection

Only Excel supports choosing **one or many worksheets** per workbook via sheet name, index, or
pattern. Other formats map **one file → one table stream**. Details: [Excel format](formats/excel.md#sheet-selection).
