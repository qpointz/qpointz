# Arrow Format

Reads Apache Arrow IPC payloads (stream and file variants). Arrow is a
columnar format with explicit schema metadata, including optional per-column
timezone on timestamp fields.

---

## Configuration Reference

Specify `type: arrow` on the reader. No extra `format` options are required.

```yaml
readers:
  - type: arrow
```

---

## Type Mapping

Arrow fields are mapped to Mill types as follows:

| Arrow Type | Mill Type |
|------------|-----------|
| `int8` | `TINY_INT` |
| `int16` | `SMALL_INT` |
| `int32` | `INT` |
| `int64` | `BIG_INT` |
| `bool` | `BOOL` |
| `float32` | `FLOAT` |
| `float64` | `DOUBLE` |
| `utf8` / `large_utf8` | `STRING` |
| `binary` / `large_binary` | `BINARY` |
| `fixed_size_binary(16)` | `UUID` |
| `date32` | `DATE` |
| `time64` | `TIME` |
| `timestamp(unit, null)` | `TIMESTAMP` |
| `timestamp(unit, timezone)` | `TIMESTAMP_TZ` |
| `duration` | `INTERVAL_DAY` |
| `interval(year_month)` | `INTERVAL_YEAR` |

---

## Timezone Semantics

- Arrow timezone support is handled at the **column level**.
- For `timestamp(..., timezone)` fields, the reader is timezone-aware and
  normalizes values to a UTC instant representation.
- No companion timezone string columns are introduced by Arrow format handling.

### Current limitations

- Per-row timezone representation is not supported in Arrow format handling.
- End-to-end timezone metadata propagation across all contracts and clients is
  tracked separately in backlog items `P-29` and `P-30`.

---

## Examples

### Regex table mapping

```yaml
name: events
storage:
  type: local
  rootPath: /data/events/arrow
readers:
  - type: arrow
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.arrow$"
```

### Glob table mapping

```yaml
name: lakehouse
storage:
  type: local
  rootPath: /data/lake
readers:
  - type: arrow
    table:
      mapping:
        type: glob
        pattern: "**/events/**/*.arrow"
        table: events
```
