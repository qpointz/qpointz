# TSV Format

Reads tab-separated value files using native TSV escape sequences.

TSV parsing is powered by [Univocity Parsers](https://github.com/uniVocity/univocity-parsers). Unlike CSV with a tab delimiter, the TSV format uses **escape sequences** (`\t`, `\n`, `\r`, `\\`) instead of quoting to represent special characters within fields. This makes TSV simpler and more predictable for data that may contain quotes.

Mill infers column names from the header row (when present) and treats every value as a `STRING`. No type coercion is applied during reading.

!!! note "TSV vs CSV with tab delimiter"
    If your files use quoting (e.g. `"value with\ttab"`), use the [`csv`](csv.md) format with `delimiter: "\t"` instead. The `tsv` format handles escape sequences natively and does not support quoting.

---

## Configuration Reference

Specify `type: tsv` on the reader and place format options under the `format` key.

```yaml
readers:
  - type: tsv
    format:
      hasHeader: true
      skipEmptyLines: true
```

### Format-level settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `escapeChar` | char | `\` | Character used for escaping special characters (`\t`, `\n`, `\r`, `\\`). |
| `escapedTabChar` | char | `t` | Character that follows the escape char to represent a tab. |
| `comment` | char | `#` | Lines starting with this character are treated as comments. |
| `lineSeparator` | string | system | Line separator string. |
| `normalizedNewline` | char | `\n` | All line endings are normalized to this character internally. |

### Parser-level settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `hasHeader` | bool | `true` | Whether the first row contains column names. |
| `lineJoiningEnabled` | bool | `false` | Join lines ending with the escape character followed by a newline. Allows multi-line values without quoting. |
| `nullValue` | string | `null` | Replacement for null/missing values. |
| `skipEmptyLines` | bool | `true` | Skip blank lines during parsing. |
| `ignoreLeadingWhitespaces` | bool | `true` | Trim leading whitespace from values. |
| `ignoreTrailingWhitespaces` | bool | `true` | Trim trailing whitespace from values. |
| `maxCharsPerColumn` | int | `4096` | Safety limit for characters per column. Set to `-1` for unlimited. |
| `maxColumns` | int | `512` | Safety limit for number of columns. |
| `headers` | list | — | Explicit column names (overrides the header row). |
| `numberOfRecordsToRead` | long | `-1` | Stop after N records. `-1` means unlimited. |
| `numberOfRowsToSkip` | long | `0` | Skip N rows before parsing. |
| `lineSeparatorDetectionEnabled` | bool | `true` | Auto-detect line separator from the first line. |
| `commentCollectionEnabled` | bool | `false` | Collect comment lines. |

---

## Examples

### Basic TSV directory

```yaml
name: tsv-dataset
storage:
  type: local
  rootPath: /data/exports
readers:
  - type: tsv
    format:
      hasHeader: true
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.tsv$"
```

### TSV without header

```yaml
name: raw-logs
storage:
  type: local
  rootPath: /var/log/data
readers:
  - type: tsv
    format:
      hasHeader: false
      headers:
        - timestamp
        - level
        - message
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.log$"
```

### TSV with line joining

Multi-line values are supported when `lineJoiningEnabled` is `true`. A backslash at the end of a line joins it with the next.

```yaml
name: multiline-data
storage:
  type: local
  rootPath: /data/multiline
readers:
  - type: tsv
    format:
      hasHeader: true
      lineJoiningEnabled: true
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.tsv$"
```
