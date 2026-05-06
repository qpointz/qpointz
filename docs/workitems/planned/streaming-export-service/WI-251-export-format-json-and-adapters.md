# WI-251 — `mill-data-format-json` + export adapters

Status: `planned`  
Type: `feature`  
Area: `data`, `formats`  
Backlog refs: **P-36**

## Goal

Add **`data/formats/mill-data-format-json`** (export-only for MVP) implementing the **WI-250** SPI: encoder accepts **`VectorBlockIterator`** + target **`OutputStream`** only — **no** dispatcher / `QueryRequest`. First flavour **`json`** = **JSON array of objects** (one object per row). Bridge **`RecordReaders.recordReader(iterator)`** inside the encoder if needed to feed existing **[`FlowRecordWriter`](../../../../data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/FlowRecordWriter.kt)** implementations for **CSV, TSV** ([`mill-data-format-text`](../../../../data/formats/mill-data-format-text)), **XLSX** ([`mill-data-format-excel`](../../../../data/formats/mill-data-format-excel)), and **Avro** ([`mill-data-format-avro`](../../../../data/formats/mill-data-format-avro)).

## Scope

1. New Gradle module `mill-data-format-json`; register in [`settings.gradle.kts`](../../../../settings.gradle.kts).
2. Implement **WI-250** `ExportFormatProvider` (or agreed SPI name) and register it in **`META-INF/services/…`** so the **`mill-data-autoconfigure` registry bean** picks it up with **no** static format list in `mill-export-service`. Apply the same pattern to CSV/TSV/XLSX/Avro adapter providers (**one or more JARs**, each with its own `META-INF` entry as appropriate).
3. **NDJSON / JSONL** as optional second id — backlog note if not in this WI (often **more** streaming-friendly than a wrapped JSON array).
4. **JSON array flavour:** if implemented, stream rows (e.g. write `[`, then rows with commas, then `]`) without building a full in-memory `JSONArray` / string for the file — same **bounded memory** rule as **WI-250**.
5. Verify **Excel** / **Avro** / text writers are wired so records are **piped to `OutputStream`** without accumulating all rows (SXSSF-style or equivalent for XLSX; document any minimal footer flush).
6. Parquet: **not** implemented here.

## Acceptance

- Unit tests for JSON writer shape (array of objects).
- At least one integration-style test per adapted format or documented manual verification checklist in WI-254.

**Depends on:** **WI-250** only (SPI + bounded-memory rules). Does **not** depend on **WI-252** — formats never call the vector source.
