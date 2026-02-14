# Fixed-Width Format

Reads fixed-width (FWF) text files where each column occupies a fixed character range within each line.

FWF parsing is powered by [Univocity Parsers](https://github.com/uniVocity/univocity-parsers). Fixed-width files are commonly used in legacy systems, banking, and government data feeds. Unlike CSV, there are no delimiters — field boundaries are defined by character positions. Mill requires explicit column definitions specifying the name, start position, and end position of each field.

All columns are read as `STRING` type. No type coercion is applied during parsing.

---

## Configuration Reference

Specify `type: fwf` on the reader and provide column definitions under the `format` key.

```yaml
readers:
  - type: fwf
    format:
      hasHeader: false
      columns:
        - name: id
          start: 0
          end: 5
        - name: name
          start: 5
          end: 25
        - name: amount
          start: 25
          end: 35
```

### Column definition

| Property | Required | Description |
|----------|----------|-------------|
| `name` | yes | Column name in the output schema. |
| `start` | yes | Zero-based start position (inclusive). |
| `end` | yes | Zero-based end position (exclusive). |

### Format-level settings

These control the FWF file dialect.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `padding` | char | ` ` (space) | Padding character used to fill unused space in a field. |
| `comment` | char | `#` | Lines starting with this character are treated as comments. |
| `lineSeparator` | string | system | Line separator string. |
| `normalizedNewline` | char | `\n` | All line endings are normalized to this character internally. |

### Parser-level settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `columns` | list | — | **Required.** List of column definitions (see above). |
| `hasHeader` | bool | `false` | Whether the first line is a header (will be skipped). |
| `keepPadding` | bool | `false` | Retain padding characters in parsed values instead of stripping them. |
| `skipTrailingCharsUntilNewline` | bool | `true` | Discard characters beyond the last defined field until the next newline. |
| `recordEndsOnNewline` | bool | `false` | Treat newline as the record terminator (even if the record is shorter than expected). |
| `useDefaultPaddingForHeaders` | bool | `true` | Use the default padding character when parsing header values. |
| `nullValue` | string | `null` | Replacement for null/missing values. |
| `skipEmptyLines` | bool | `true` | Skip blank lines during parsing. |
| `ignoreLeadingWhitespaces` | bool | `true` | Trim leading whitespace from values. |
| `ignoreTrailingWhitespaces` | bool | `true` | Trim trailing whitespace from values. |
| `maxCharsPerColumn` | int | `4096` | Safety limit for characters per column. |
| `maxColumns` | int | `512` | Safety limit for number of columns. |
| `numberOfRecordsToRead` | long | `-1` | Stop after N records. `-1` means unlimited. |
| `numberOfRowsToSkip` | long | `0` | Skip N rows before parsing. |
| `lineSeparatorDetectionEnabled` | bool | `true` | Auto-detect line separator from the first line. |

---

## Examples

### Basic fixed-width file

```yaml
name: legacy-reports
storage:
  type: local
  rootPath: /data/legacy
readers:
  - type: fwf
    format:
      hasHeader: false
      columns:
        - name: account_id
          start: 0
          end: 10
        - name: customer_name
          start: 10
          end: 40
        - name: balance
          start: 40
          end: 55
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.dat$"
```

### Fixed-width with header row

```yaml
name: banking-data
storage:
  type: local
  rootPath: /data/banking
readers:
  - type: fwf
    format:
      hasHeader: true
      columns:
        - name: txn_id
          start: 0
          end: 12
        - name: txn_date
          start: 12
          end: 22
        - name: amount
          start: 22
          end: 35
        - name: description
          start: 35
          end: 75
    table:
      mapping:
        type: directory
        depth: 1
```

### With custom padding

```yaml
name: mainframe-extract
storage:
  type: local
  rootPath: /data/mainframe
readers:
  - type: fwf
    format:
      hasHeader: false
      padding: "0"
      keepPadding: false
      recordEndsOnNewline: true
      columns:
        - name: record_type
          start: 0
          end: 2
        - name: account_number
          start: 2
          end: 14
        - name: amount_cents
          start: 14
          end: 26
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.txt$"
```
