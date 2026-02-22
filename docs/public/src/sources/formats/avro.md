# Avro Format

Reads Apache Avro data files. Avro files are self-describing — the schema is embedded in the
file header, so no additional format options are needed.

Avro is a compact binary format commonly used in data pipelines and event streaming platforms. Mill reads the embedded schema and converts Avro records into Mill rows automatically.

---

## Configuration Reference

Specify `type: avro` on the reader. No `format` section is required.

```yaml
readers:
  - type: avro
```

There are no format-specific options for Avro.

---

## Type Mapping

Avro fields are mapped to Mill types as follows:

| Avro Type   | Mill Type   |
|-------------|-------------|
| BOOLEAN     | `BOOL`      |
| INT         | `INT`       |
| LONG        | `BIG_INT`   |
| FLOAT       | `FLOAT`     |
| DOUBLE      | `DOUBLE`    |
| STRING      | `STRING`    |
| BYTES       | `BINARY`    |
| FIXED       | `BINARY`    |
| ENUM        | `STRING`    |

Nullable unions (`["null", <type>]`) are supported and produce nullable columns.

---

## Examples

### Avro files with regex table mapping

```yaml
name: events
storage:
  type: local
  rootPath: /data/events/avro
readers:
  - type: avro
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.avro$"
```

### Avro files with directory table mapping

```yaml
name: event-store
storage:
  type: local
  rootPath: /data/events
readers:
  - type: avro
    table:
      mapping:
        type: directory
        depth: 1
```

### Avro files with glob table mapping

```yaml
name: stream-archive
storage:
  type: local
  rootPath: /data/streams
readers:
  - type: avro
    table:
      mapping:
        type: glob
        pattern: "**/*.avro"
        table: stream_events
```

All `.avro` files under `/data/streams` are combined into the `stream_events` table.
