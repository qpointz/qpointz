# Local Storage

The `local` storage type scans a directory on the local filesystem. All regular files under the root path (including subdirectories) are discovered and made available to readers.

---

## Configuration

```yaml
storage:
  type: local
  rootPath: /data/airlines/csv
```

| Property   | Required | Description |
|------------|----------|-------------|
| `rootPath` | yes      | Absolute or relative path to the root directory. |

Hidden files and directories are included in the scan. Use [table mapping](../configuration.md#table-mapping) rules to filter unwanted files.

---

## Example

A complete source descriptor using local storage with CSV files:

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

---

## See Also

- [Configuration](../configuration.md) — full YAML specification
- [Formats](../formats/csv.md) — format-specific options
- [Cloud storages](s3.md) — for files stored in object storage instead of local disk
