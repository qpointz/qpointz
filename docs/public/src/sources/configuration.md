# Source Configuration Reference

A source is configured as a YAML document. Mill supports a **multi-reader** model: one storage location can have multiple readers, each with its own format, table mapping, and table attributes.

## Basic Structure

```yaml
name: <source-name>
storage:
  type: <storage-type>
  # ... storage-specific options
table:                     # optional shared default
  mapping:
    type: <mapping-type>
    # ... mapping-specific options
  attributes:              # optional
    - name: <column-name>
      source: regex | constant
      # ...
conflicts: reject          # optional, default: reject
readers:
  - type: <format-type>
    label: <optional-suffix>
    format:
      # ... format-specific options
    table:                 # optional override (replaces source-level entirely)
      mapping:
        type: <mapping-type>
      attributes:
        - name: <column-name>
          source: regex | constant
```

| Property    | Required | Description                                                       |
|-------------|----------|-------------------------------------------------------------------|
| `name`      | yes      | Logical name for this source. Becomes the schema name.            |
| `storage`   | yes      | Where to find files.                                              |
| `readers`   | yes      | One or more reader configurations (format + mapping).             |
| `table`     | no       | Shared default table config (mapping + attributes).               |
| `conflicts` | no       | How to handle table name collisions across readers (default: `reject`). |

---

## Readers

Each reader describes how to read a subset of files from the storage. A reader has a **type** (the format identifier), an optional **label**, format-specific options under **format**, and an optional **table** override.

```yaml
readers:
  - type: csv
    label: raw
    format:
      delimiter: ","
      hasHeader: true
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.csv$"
      attributes:
        - name: pipeline
          source: constant
          value: "raw-ingest"
```

| Property | Required | Description                                                           |
|----------|----------|-----------------------------------------------------------------------|
| `type`   | yes      | Format identifier (e.g. `csv`, `parquet`).                            |
| `label`  | no       | Suffix appended to table names (e.g. `raw` -> `orders_raw`).         |
| `format` | no       | Format-specific options (delimiter, header, etc.).                    |
| `table`  | no       | Table config override. Replaces source-level `table` entirely.        |

### Labels

When a reader has a `label`, all its table names get the label appended with an underscore. For example, if a reader with `label: raw` discovers a table named `orders`, the final table name becomes `orders_raw`.

Labels are the primary way to avoid naming collisions when multiple readers discover the same table names.

---

## Table Configuration

The `table` section groups table-level settings: **mapping** (how files become tables) and **attributes** (extra columns). It can appear at two levels:

- **Source level** — shared default for all readers
- **Reader level** — completely replaces the source-level `table` for that reader

When a reader defines its own `table`, it overrides the source-level table entirely — there is no partial merging.

### Table Mapping

#### `regex` — Regular Expression

Extracts the table name from each file's path using a regular expression with a named capture group.

```yaml
table:
  mapping:
    type: regex
    pattern: ".*(?<table>[^/]+)\\.csv$"
    tableNameGroup: table
```

| Property         | Required | Default   | Description                                          |
|------------------|----------|-----------|------------------------------------------------------|
| `pattern`        | yes      | —         | Regex pattern applied to the full file path.         |
| `tableNameGroup` | no       | `table`   | Name of the capture group that holds the table name. |

#### `directory` — Parent Directory Name

Uses the parent directory name as the table name. All files in the same directory become one table.

```yaml
table:
  mapping:
    type: directory
    depth: 1
```

| Property | Required | Default | Description                                                     |
|----------|----------|---------|-----------------------------------------------------------------|
| `depth`  | no       | `1`     | How many directory levels up from the file to use as table name. |

With `depth: 1` (default), the immediate parent directory is the table name:

```
/data/cities/part-001.csv     -> table: cities
/data/cities/part-002.csv     -> table: cities
/data/flights/part-001.csv    -> table: flights
```

With `depth: 2`, the grandparent directory is used:

```
/data/cities/2024/part-001.csv   -> table: cities
/data/flights/2024/part-001.csv  -> table: flights
```

### Table Attributes

Table attributes let you inject extra columns into every record. Values can be **extracted from the file path** using a regex, or set to a **constant**.

```yaml
table:
  mapping:
    type: regex
    pattern: ".*(?<table>[^/]+)\\.csv$"
  attributes:
    - name: year
      source: regex
      pattern: ".*_(?<year>\\d{4})\\d{4}\\.csv$"
      group: year
      type: int
    - name: pipeline
      source: constant
      value: "raw-ingest"
    - name: file_date
      source: regex
      pattern: ".*_(?<date>\\d{8})\\.csv$"
      group: date
      type: date
      format: ddMMyyyy
```

| Property  | Required | Default  | Description                                                    |
|-----------|----------|----------|----------------------------------------------------------------|
| `name`    | yes      | —        | Column name in the output schema.                              |
| `source`  | yes      | —        | `regex` (extract from path) or `constant` (fixed value).       |
| `type`    | no       | `string` | Type hint: `string`, `int`, `long`, `float`, `double`, `bool`, `date`, `timestamp`. |
| `format`  | cond.    | —        | Date/timestamp format pattern. Required when type is `date` or `timestamp`. |
| `pattern` | cond.    | —        | Regex pattern (required when source is `regex`).               |
| `group`   | cond.    | —        | Named capture group (required when source is `regex`).         |
| `value`   | cond.    | —        | Constant value (required when source is `constant`).           |

**Type coercion:** When a type is specified, the extracted string value is converted to that type. If conversion fails, the value becomes `null`.

---

## Conflicts

When multiple readers produce the same table name, Mill needs a conflict resolution strategy. Configure this with the `conflicts` property.

### String shorthand

Apply one strategy to all tables:

```yaml
conflicts: reject    # fail if any collision (default)
conflicts: union     # merge files from all readers into one table
```

### Per-table rules

Set a default and override specific tables:

```yaml
conflicts:
  default: reject
  orders: union        # merge 'orders' from all readers
  customers: reject    # keep strict for 'customers'
```

| Strategy | Description                                                |
|----------|------------------------------------------------------------|
| `reject` | Fail with an error if two readers produce the same name.   |
| `union`  | Combine files from all readers into a single table.        |

**Resolution order:**

1. If a table has an explicit per-table rule, that rule applies (labels are ignored).
2. If all readers producing the same name have different labels, labels disambiguate.
3. Otherwise, the `default` strategy applies.

---

## Storage

The `storage` section defines where Mill looks for data files.

### `local` — Local Filesystem

Scans a directory on the local filesystem recursively.

```yaml
storage:
  type: local
  rootPath: /data/airlines/csv
```

| Property   | Required | Default | Description                                       |
|------------|----------|---------|---------------------------------------------------|
| `rootPath` | yes      | —       | Absolute or relative path to the root directory.   |

All regular files under `rootPath` (including subdirectories) are discovered. Hidden files and directories are included in the scan; use table mapping rules to filter unwanted files.

---

## Formats

The `type` field on each reader selects the format. Format-specific options go under the `format` key. Each format has its own dedicated page with configuration reference, type mapping, and examples.

| Format | Description |
|--------|-------------|
| [CSV](formats/csv.md) | Comma/delimiter-separated text files |
| [TSV](formats/tsv.md) | Tab-separated value files (escape-based) |
| [FWF](formats/fwf.md) | Fixed-width text files |
| [Excel](formats/excel.md) | Microsoft Excel (.xlsx, .xls) files |
| [Avro](formats/avro.md) | Apache Avro binary data files |
| [Parquet](formats/parquet.md) | Apache Parquet columnar files |

---

## Complete Examples

For single-reader examples specific to each format, see the individual format pages: [CSV](formats/csv.md), [TSV](formats/tsv.md), [FWF](formats/fwf.md), [Excel](formats/excel.md), [Avro](formats/avro.md), [Parquet](formats/parquet.md).

### Multi-Reader — Mixed Formats

Two readers sharing the same storage, each reading different file types:

```yaml
name: mixed-warehouse
storage:
  type: local
  rootPath: /data/warehouse
table:
  mapping:
    type: directory
    depth: 1
conflicts: reject
readers:
  - type: csv
    label: raw
    format:
      delimiter: ","
  - type: parquet
    label: processed
```

Result: `orders_raw`, `orders_processed`, `customers_raw`, `customers_processed` (assuming both formats discover `orders` and `customers`).

### Multi-Reader with Per-Table Rules

```yaml
name: selective-merge
storage:
  type: local
  rootPath: /data/mixed
table:
  mapping:
    type: directory
conflicts:
  default: reject
  orders: union
  events: union
readers:
  - type: csv
    format:
      delimiter: ","
  - type: parquet
```

Here, `orders` and `events` are merged across readers, while all other table name collisions are rejected.

---

## Source Verification

Mill can verify a source configuration before using it. Verification checks for configuration errors, probes storage access, tests table mapping rules against actual files, and analyzes potential conflicts across readers.

### What is checked

| Phase | Checks |
|-------|--------|
| **Descriptor** | Name not blank, readers defined, no duplicate labels, table mapping coverage, attribute validity |
| **Storage** | Storage accessible, blobs discoverable, empty storage warning |
| **Table mapping** | Valid regex patterns, named groups exist, depth values valid |
| **Schema** | Schema inference succeeds on a sample file per table |
| **Attributes** | Regex patterns compile, named groups exist, date formats valid, attribute extraction succeeds on sample blobs |
| **Conflict** | Cross-reader table name collisions, unused conflict rules |

### Issue severity

| Level | Meaning |
|-------|---------|
| **ERROR** | Source cannot be used — must be fixed |
| **WARNING** | Potential problem — source may work but behavior could be unexpected |
| **INFO** | Nothing wrong — informational (e.g. how a conflict was resolved) |

### Verification modes

- **Static** — validates configuration only (fast, no I/O)
- **Deep** — probes actual storage, runs table mapping, infers schemas
- **Full** — runs both static and deep checks

Verification never fails with an exception — all issues are collected into a report. This makes it suitable for interactive configuration workflows where users want to see all problems at once.
