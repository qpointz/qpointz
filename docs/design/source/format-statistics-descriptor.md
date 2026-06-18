# Format descriptor control for record statistics

Follow-up to [WI-314 flow table statistics](../../workitems/completed/20260618-flow-translatable-table-scan/WI-314-flow-table-statistics.md)
and the [format capability matrices](formats/README.md).

## Problem

Record statistics are wired **implicitly** today:

1. If a `FormatHandler` implements [RecordStatisticReader](../../../data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/statistics/RecordStatisticReader.kt),
   [SourceStatisticWiring](../../../data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/statistics/SourceStatisticWiring.kt)
   always attaches a blob provider (when **all** blobs in a table are stats-capable).
2. Text formats ([CSV](formats/csv.md), [TSV](formats/tsv.md), [FWF](formats/fwf.md)) always use the
   **physical line heuristic** — fast but **approximate** when quoted fields contain embedded newlines.
3. Operators cannot **disable** misleading estimates or **opt in** to an accurate (but costly) text count.

There is no YAML knob; behaviour is entirely code-defined per format handler.

## Goal

Expose a single **`statistics.mode`** on format descriptors (`format:` in `SourceDescriptor`):

| Mode | Meaning |
|------|---------|
| **`none`** | Statistics **disabled** — handler does not participate in table-level row estimates |
| **`approximate`** | Fast estimate (text: physical line count) |
| **`exact`** | Authoritative count (binary metadata or full text parse) |

Binary self-describing formats (Parquet, Arrow) support **`exact`** and **`none`** only.
Avro/Excel support **`none`** only until a reader strategy exists.

## Proposed YAML

Only one setting under `statistics`:

```yaml
readers:
  - type: csv
    format:
      delimiter: ","
      hasHeader: true
      statistics:
        mode: approximate    # none | approximate | exact
```

| Property | Default | Values | Meaning |
|----------|---------|--------|---------|
| `statistics.mode` | format-specific (see below) | `none` / `approximate` / `exact` | Controls whether and how row counts are supplied |

When the whole `statistics` block is **omitted**, the format-specific default applies (not `none` for
stats-capable formats — that preserves today’s behaviour).

### Format-specific defaults and allowed modes

| Format | Default when omitted | Allowed modes | Notes |
|--------|----------------------|---------------|-------|
| Parquet | `exact` | `exact`, `none` | Footer row count |
| Arrow | `exact` | `exact`, `none` | IPC batch sums |
| CSV / TSV / FWF | `approximate` | `approximate`, `exact`, `none` | Line heuristic vs parser count |
| Avro | `none` | `none` | No reader yet |
| Excel | `none` | `none` | No reader yet |

Invalid combinations (e.g. `approximate` on Parquet) are rejected at **materialize** time with a
clear configuration error.

### Examples

**Disable stats** (planner treats row count as unknown for that table):

```yaml
format:
  type: csv
  hasHeader: true
  statistics:
    mode: none
```

**Exact row count for a small, critical CSV** (full-file parse cost at first planner access):

```yaml
format:
  type: csv
  hasHeader: true
  statistics:
    mode: exact
```

**Approximate** (explicit; same as omitting `statistics` on CSV):

```yaml
format:
  type: tsv
  statistics:
    mode: approximate
```

**Parquet with stats off**:

```yaml
format:
  type: parquet
  statistics:
    mode: none
```

## Data model

### Descriptor layer

```kotlin
data class StatisticDescriptor(
    val mode: StatisticMode = StatisticMode.DEFAULT,
)

enum class StatisticMode {
    DEFAULT,       // omitted block → format handler default
    NONE,          // disabled
    APPROXIMATE,
    EXACT,
}
```

Embed on format descriptors:

- `CsvFormatDescriptor`, `TsvFormatDescriptor`, `FwfFormatDescriptor`
- `ParquetFormatDescriptor`, `ArrowFormatDescriptor`

Map into handler settings via existing `toSettings()` / factory `create()` paths. Resolved
`DEFAULT` becomes the format’s default mode before wiring.

### Record statistic payload

Extend [RecordStatistic](../../../data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/statistics/RecordStatistic.kt):

```kotlin
data class RecordStatistic(
    val estimatedRowCount: Long?,
    val mode: StatisticMode,   // EXACT or APPROXIMATE only (never NONE on payload)
)
```

- **`none`** is a wiring/descriptor concept only — no provider is registered.
- **Approximate** counts remain usable by Calcite (`Statistics.of(rowCount, …)`) but are tagged for
  logging, UI, and future planner policy.
- Calcite 1.41 has no first-class “approximate row count”; tagging is Mill-side metadata for now.

### Handler behaviour (text)

| `statistics.mode` | Behaviour |
|-------------------|-----------|
| `none` | No stats provider for this blob |
| `approximate` | `TextLineRecordStatisticReader` (current line heuristic) |
| `exact` | Parse blob with same Univocity settings as read path; count logical records |

### Handler behaviour (Parquet / Arrow)

| `statistics.mode` | Behaviour |
|-------------------|-----------|
| `none` | No stats provider |
| `exact` | Existing metadata path (footer / IPC batches) |
| `approximate` | **Invalid** — reject at materialize |

### Wiring

[SourceStatisticWiring.forTable](../../../data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/statistics/SourceStatisticWiring.kt)
unchanged structurally:

- `recordStatisticProviderForBlob` returns `null` when resolved mode is **`none`** or handler lacks
  a strategy for the requested mode.
- **Multi-blob rule** stays strict: if any blob in the logical table lacks a provider, table-level
  record stats are omitted (planner sees `Statistics.UNKNOWN`).

Optional later relaxation: aggregate only blobs with stats and mark table `APPROXIMATE` if any leaf
is approximate — out of scope for first slice.

## Exact mode for text (implementation sketch)

1. Reuse `CsvRecordSource` / TSV / FWF record iterators with a counting pass, **or** a dedicated
   lightweight Univocity `beginParsing` loop.
2. Memoize in `BlobBoundRecordStatisticProvider` (same as today).
3. Document cost: **O(file size)** I/O + parse; suitable for small files or infrequent planner access.
4. Do **not** run exact count at schema resolve / materialize — only on lazy provider access.

## Public and design docs

When implemented, update:

- [format-capabilities.md](../../public/src/sources/format-capabilities.md) — `statistics.mode` section
- Per-format public pages (CSV/TSV/FWF) — defaults and cost warning for `mode: exact`
- [formats/README.md](formats/README.md) capability tables — note descriptor override

## Phasing

| Phase | Scope |
|-------|--------|
| **A** | `StatisticDescriptor` + `statistics.mode`; text + binary descriptors; wiring respects `none` |
| **B** | `mode: exact` on text — parser row count |
| **C** | Materialize validation (illegal mode per format) |
| **D** | Optional: surface resolved mode in Calcite / planner hints |

## Non-goals (this follow-up)

- Avro block-index or Excel sheet row counts (separate format work).
- Persisted statistics cache across restarts.
- Cross-table sampling or ANALYZE-style jobs.

## Open questions

1. **Default for text when omitted:** keep `approximate` (current behaviour) vs default `none`
   until operator opts in?
2. **Exact on multi-GB CSV:** hard cap or `maxBytesForExact` safety valve on `mode: exact`?
3. **Mixed readers on one table:** if one blob resolves to `none`, entire table loses stats — document
   clearly or allow partial aggregation?
