# Data Sources

Data sources let you connect Mill to file-based data — whether the files live on your local disk or in cloud object storage. You describe a source in a short YAML file, and Mill discovers the files, maps them to tables, and makes everything queryable with SQL.

A source configuration tells Mill three things:

1. **Where** to find the files — the [storage](configuration.md#storage) backend (local directory, S3 bucket, GCS bucket, or Azure `endpoint` / `container`)
2. **How** to read them — one or more [readers](configuration.md#readers), each tied to a file [format](#supported-formats)
3. **Which files** map to which tables — [table mapping](configuration.md#table-mapping) rules (regex, directory, or glob)

---

## Quick Example

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

This scans a local folder, reads CSV files, and exposes each file as a queryable table (e.g. `cities.csv` becomes table `cities`) under the schema `airline-data`.

See the [Configuration](configuration.md) page for the full YAML specification, multi-reader setups, table attributes, conflict resolution, and more examples.

---

## Supported Storages

Mill supports local and cloud storage backends. The storage backend only controls where files are located — readers, table mapping, and all other settings work the same regardless of storage type.

| Type | Description | Page |
|------|-------------|------|
| `local` | Local filesystem directory | [Local storage](storages/local.md) |
| `s3` | Amazon S3 buckets | [AWS S3](storages/s3.md) |
| `gcs` | Google Cloud Storage buckets | [Google Cloud Storage](storages/gcs.md) |
| `adls` | Azure Blob Storage / ADLS Gen2 (`endpoint` + `container`) | [Azure Blob Storage](storages/azure.md) |

---

## Supported Formats

Each reader specifies a file format. Mill handles schema inference, type mapping, and parsing automatically.

| Format | Description | Page |
|--------|-------------|------|
| `csv` | Comma/delimiter-separated values | [CSV](formats/csv.md) |
| `tsv` | Tab-separated values (escape-based) | [TSV](formats/tsv.md) |
| `fwf` | Fixed-width text files | [FWF](formats/fwf.md) |
| `excel` | Microsoft Excel (.xlsx, .xls) | [Excel](formats/excel.md) |
| `avro` | Apache Avro binary data files | [Avro](formats/avro.md) |
| `parquet` | Apache Parquet columnar files | [Parquet](formats/parquet.md) |
| `arrow` | Apache Arrow IPC stream/file payloads | [Arrow](formats/arrow.md) |

---

## Next Steps

- [Configuration](configuration.md) — full YAML reference with all options and examples
- [Flow Backend](../backends/flow.md) — how to run Mill with file-based data sources
- [Type System](types.md) — Mill data types and type mapping across formats and clients
