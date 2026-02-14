# Parquet Format

Reads Apache Parquet files. Parquet files are self-describing — the schema is embedded in the
file footer, so no additional format options are needed.

Parquet is a columnar binary format optimized for analytical workloads. It supports rich type annotations and efficient compression. Mill reads the embedded schema and converts Parquet columns into Mill rows automatically.

---

## Configuration Reference

Specify `type: parquet` on the reader. No `format` section is required.

```yaml
readers:
  - type: parquet
```

There are no format-specific options for Parquet.

---

## Type Mapping

Parquet fields are mapped to Mill types as follows:

| Parquet Type           | Logical Annotation | Mill Type   |
|------------------------|--------------------|-------------|
| BOOLEAN                | —                  | `BOOL`      |
| INT32                  | —                  | `INT`       |
| INT32                  | DATE               | `DATE`      |
| INT64                  | —                  | `BIG_INT`   |
| INT64                  | TIMESTAMP          | `TIMESTAMP` |
| INT64                  | TIME               | `TIME`      |
| FLOAT                  | —                  | `FLOAT`     |
| DOUBLE                 | —                  | `DOUBLE`    |
| BINARY                 | STRING / UTF8      | `STRING`    |
| BINARY                 | —                  | `BINARY`    |
| FIXED_LEN_BYTE_ARRAY   | —                  | `BINARY`    |

Required fields produce non-nullable columns; optional fields produce nullable columns.

---

## Examples

### Parquet files with directory table mapping

```yaml
name: analytics
storage:
  type: local
  rootPath: /data/analytics/parquet
readers:
  - type: parquet
    table:
      mapping:
        type: directory
        depth: 1
```

### Parquet files with regex table mapping

```yaml
name: warehouse
storage:
  type: local
  rootPath: /data/warehouse/parquet
readers:
  - type: parquet
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.parquet$"
```
