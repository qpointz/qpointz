# CSV Format

Reads delimited text files — CSV, pipe-separated, and other single-character delimiters.

CSV parsing is powered by [Univocity Parsers](https://github.com/uniVocity/univocity-parsers). Mill infers column names from the header row (when present) and treats every value as a `STRING`. No type coercion is applied during reading; use table attributes or downstream queries to cast values to other types.

!!! note "TSV files"
    For tab-separated files, consider using the dedicated [`tsv`](tsv.md) format which handles TSV escape sequences natively. Alternatively, set `delimiter: "\t"` on a `csv` reader.

---

## Configuration Reference

Specify `type: csv` on the reader and place format options under the `format` key.

```yaml
readers:
  - type: csv
    format:
      delimiter: ","
      hasHeader: true
      skipEmptyLines: true
```

### Format-level settings

These control the low-level CSV dialect (characters used for quoting, delimiting, etc.).

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `delimiter` | char | `,` | Field delimiter character. |
| `quote` | char | `"` | Character used to enclose (quote) field values. |
| `quoteEscape` | char | `"` | Character used to escape the quote character inside a quoted field. |
| `charToEscapeQuoteEscaping` | char | `\0` | Escape character for the quote-escape character itself. |
| `comment` | char | `#` | Lines starting with this character are treated as comments. |
| `lineSeparator` | string | system | Line separator string (e.g. `"\r\n"`). |
| `normalizedNewline` | char | `\n` | All line endings are normalized to this character internally. |

### Parser-level settings

These control parsing behavior: how empty/null values are handled, whitespace trimming, limits, and auto-detection.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `hasHeader` | bool | `true` | Whether the first row contains column names. |
| `emptyValue` | string | `null` | Replacement for empty quoted values (e.g. `""` → this value). |
| `nullValue` | string | `null` | Replacement for null/missing values. |
| `skipEmptyLines` | bool | `true` | Skip blank lines during parsing. |
| `ignoreLeadingWhitespaces` | bool | `true` | Trim leading whitespace from unquoted values. |
| `ignoreTrailingWhitespaces` | bool | `true` | Trim trailing whitespace from unquoted values. |
| `ignoreLeadingWhitespacesInQuotes` | bool | `false` | Trim leading whitespace inside quoted values. |
| `ignoreTrailingWhitespacesInQuotes` | bool | `false` | Trim trailing whitespace inside quoted values. |
| `escapeUnquotedValues` | bool | `false` | Process escape sequences in unquoted fields. |
| `keepEscapeSequences` | bool | `false` | Retain raw escape sequences instead of interpreting them. |
| `keepQuotes` | bool | `false` | Keep the enclosing quote characters in parsed values. |
| `normalizeLineEndingsWithinQuotes` | bool | `true` | Normalize line endings inside quoted fields. |
| `unescapedQuoteHandling` | enum | `STOP_AT_CLOSING_QUOTE` | How to handle unescaped quotes (see below). |
| `maxCharsPerColumn` | int | `4096` | Safety limit for characters per column. Set to `-1` for unlimited. |
| `maxColumns` | int | `512` | Safety limit for number of columns. |
| `headers` | list | — | Explicit column names (overrides the header row). |
| `numberOfRecordsToRead` | long | `-1` | Stop after N records. `-1` means unlimited. |
| `numberOfRowsToSkip` | long | `0` | Skip N rows before parsing (after the header). |
| `lineSeparatorDetectionEnabled` | bool | `true` | Auto-detect line separator from the first line. |
| `delimiterDetectionEnabled` | bool | `false` | Auto-detect the delimiter character. |
| `quoteDetectionEnabled` | bool | `false` | Auto-detect the quote character. |
| `commentCollectionEnabled` | bool | `false` | Collect comment lines (accessible via parser context). |

### Unescaped quote handling

| Value | Description |
|-------|-------------|
| `STOP_AT_CLOSING_QUOTE` | Accumulate content until a matching closing quote is found. |
| `BACK_TO_DELIMITER` | Go back and re-parse from the quote as unquoted content. |
| `STOP_AT_DELIMITER` | Stop accumulating at the next delimiter. |
| `SKIP_VALUE` | Skip the entire value. |
| `RAISE_ERROR` | Throw an error on encountering an unescaped quote. |

---

## Examples

### Flat CSV directory

```yaml
name: airline-data
storage:
  type: local
  rootPath: /data/airlines/csv
readers:
  - type: csv
    format:
      delimiter: ","
      hasHeader: true
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.csv$"
```

### Partitioned by directory

```yaml
name: warehouse
storage:
  type: local
  rootPath: /data/warehouse
readers:
  - type: csv
    format:
      delimiter: ","
    table:
      mapping:
        type: directory
        depth: 1
```

### Pipe-separated with custom settings

```yaml
name: legacy-extract
storage:
  type: local
  rootPath: /data/legacy
readers:
  - type: csv
    format:
      delimiter: "|"
      hasHeader: false
      skipEmptyLines: true
      maxCharsPerColumn: 8192
      nullValue: "NULL"
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.dat$"
```

### With table attributes

Extract date parts from file names and inject a constant tag.

```yaml
name: daily-imports
storage:
  type: local
  rootPath: /data/imports
readers:
  - type: csv
    format:
      delimiter: ","
    table:
      mapping:
        type: regex
        pattern: ".*?(?<table>[a-z_]+)_\\d{8}\\.csv$"
      attributes:
        - name: file_date
          source: regex
          pattern: ".*_(\\d{8})\\.csv$"
          group: date
          type: date
          format: ddMMyyyy
        - name: source_tag
          source: constant
          value: "daily-import"
```

Each record now includes `file_date` (parsed date) and `source_tag` (`"daily-import"`) alongside the original CSV columns.
