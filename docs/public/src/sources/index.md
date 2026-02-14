# File-Based Data Sources

Mill can connect to file-based data stored on disk (or cloud storage) and expose it as queryable tables. You describe a data source in a YAML configuration file, and Mill takes care of discovering files, mapping them to tables, and making them available for queries.

A single source configuration produces one **schema** in Mill, containing one or more **tables** discovered from the files.

---

## How It Works

A source configuration tells Mill:

1. **Where** to find the files (storage)
2. **How** to read them (one or more readers, each with a format)
3. **Which files** belong to which tables (table mapping)
4. **What extra columns** to inject (table attributes — optional)

Mill scans the storage location, groups files into logical tables based on the mapping rules, and reads them using the specified format. When multiple readers are configured, Mill applies conflict resolution rules to handle table name collisions. Table attributes let you enrich records with values extracted from file paths or injected as constants.

---

## Quick Example

### Single Reader

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

This configuration:

- Scans `/data/airlines/csv` on the local filesystem
- Reads files as CSV with comma delimiter and header row
- Extracts table names from filenames (e.g. `cities.csv` becomes table `cities`)
- Exposes everything under the schema name `airline-data`

### Multiple Readers

```yaml
name: warehouse
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

This configuration:

- Scans `/data/warehouse` on the local filesystem
- Uses two readers: one for CSV files (labeled "raw") and one for Parquet files (labeled "processed")
- Discovers tables by directory name (shared mapping)
- Labels disambiguate: `orders_raw`, `orders_processed`

### With Table Attributes

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

This adds two extra columns to every record: `file_date` (extracted from the filename) and `source_tag` (a constant).

---

## Supported Storage Backends

| Type    | Description                         |
|---------|-------------------------------------|
| `local` | Local filesystem directory          |

See [Configuration Reference](configuration.md#storage) for details.

## Supported Formats

| Type      | Description                          |
|-----------|--------------------------------------|
| [`csv`](formats/csv.md)     | Comma/delimiter-separated values     |
| [`tsv`](formats/tsv.md)     | Tab-separated values (escape-based)  |
| [`fwf`](formats/fwf.md)     | Fixed-width text files               |
| [`excel`](formats/excel.md)   | Microsoft Excel (.xlsx, .xls) files  |
| [`avro`](formats/avro.md)    | Apache Avro binary data files        |
| [`parquet`](formats/parquet.md) | Apache Parquet columnar files        |

See individual format pages for configuration options, type mappings, and examples.

## Table Configuration

### Mapping Strategies

| Type        | Description                                      |
|-------------|--------------------------------------------------|
| `regex`     | Extract table name from file path using regex     |
| `directory` | Use parent directory name as table name           |

See [Configuration Reference](configuration.md#table-mapping) for details and examples.

### Table Attributes

Inject extra columns from file paths or constants into every record. Supported types: `string`, `int`, `long`, `float`, `double`, `bool`, `date`, `timestamp`.

See [Configuration Reference](configuration.md#table-attributes) for details.

## Conflict Resolution

| Strategy  | Description                                              |
|-----------|----------------------------------------------------------|
| `reject`  | Fail if multiple readers produce the same table name     |
| `union`   | Merge files from all readers into a single table         |

Per-table overrides are supported. See [Configuration Reference](configuration.md#conflicts) for details.

---

## Next Steps

- [Configuration Reference](configuration.md) — full YAML specification with all options and examples
