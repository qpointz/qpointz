# Google Cloud Storage

The `gcs` storage type reads files from a Google Cloud Storage bucket. Readers, table mapping, and all other source configuration options work exactly the same as with [local storage](local.md) — only the `storage` block changes.

---

## Configuration

```yaml
storage:
  type: gcs
  bucket: my-data-bucket
  prefix: warehouse/parquet/
```

| Property   | Required | Default | Description |
|------------|----------|---------|-------------|
| `bucket`   | yes      | —       | GCS bucket name. |
| `prefix`   | no       | —       | Object name prefix to limit the scan scope. |
| `endpoint` | no       | —       | Custom GCS endpoint URL. Use for emulators (e.g. fake-gcs-server). |

---

## Authentication

When `storage.auth` is omitted, Mill uses **Google Application Default Credentials** (ADC) — environment variable, metadata server, or `gcloud` CLI login. This is the recommended approach for production.

For explicit credentials, provide the path to a service account JSON key file:

```yaml
storage:
  type: gcs
  bucket: my-data-bucket
  auth:
    serviceAccountJson: ${GOOGLE_APPLICATION_CREDENTIALS}
```

| Auth property        | Description |
|----------------------|-------------|
| `serviceAccountJson` | Path to a service account JSON key file, or the JSON content itself. |
| `preferAmbientCredentials` | When `true`, forces ADC resolution even if explicit credentials are present. |

Do not commit service account keys to version control. Use environment variable expansion (`${VAR}`) or a secret store.

---

## Local Development with fake-gcs-server

For local development and CI, use [fake-gcs-server](https://github.com/fsouza/fake-gcs-server) as a GCS emulator:

```yaml
storage:
  type: gcs
  bucket: test-bucket
  endpoint: http://localhost:4443
```

No `auth` block is needed when connecting to the emulator.

---

## Example

Complete source descriptor reading Avro files from GCS:

```yaml
name: events
storage:
  type: gcs
  bucket: analytics-events
  prefix: avro/
readers:
  - type: avro
    table:
      mapping:
        type: regex
        pattern: ".*(?<table>[^/]+)\\.avro$"
```

---

## See Also

- [Configuration](../configuration.md) — full YAML specification
- [AWS S3](s3.md) / [Azure Blob Storage](azure.md) — other cloud storages
- [Local storage](local.md) — for files on disk
- [Flow Backend](../../backends/flow.md) — wiring sources into the query engine
