# Google Cloud Storage

The `gcs` storage type reads files from a Google Cloud Storage bucket. Readers, table mapping, and all other source configuration options work exactly the same as with [local storage](local.md) — only the `storage` block changes.

---

## Configuration

```yaml
storage:
  type: gcs
  bucket: my-data-bucket
  projectId: my-gcp-project
  prefix: warehouse/parquet/
```

| Property   | Required | Default | Description |
|------------|----------|---------|-------------|
| `bucket`   | yes      | —       | GCS bucket name. |
| `projectId`| no       | —       | Google Cloud project ID. Recommended when using a service account key so quota and metadata requests target the correct project (matches `project_id` in the key JSON). |
| `prefix`   | no       | —       | Object name prefix to limit the scan scope. |
| `endpoint` | no       | —       | Custom GCS endpoint URL. Use for emulators (e.g. fake-gcs-server). |

---

## Authentication

When `storage.auth` is omitted, Mill uses **Google Application Default Credentials** (ADC) — `GOOGLE_APPLICATION_CREDENTIALS` pointing to a key file, workload identity, metadata server, or `gcloud auth application-default login`. This is the recommended approach for production.

### Service account key file (explicit path)

Use **`serviceAccountJsonPath`** with an absolute or relative path to the JSON key file downloaded from Google Cloud Console (IAM → Service accounts → Keys → Add key → JSON):

```yaml
storage:
  type: gcs
  bucket: my-data-bucket
  auth:
    serviceAccountJsonPath: /secure/keys/my-project-sa.json
```

### Inline service account JSON (advanced)

**`serviceAccountJson`** must contain the **full JSON key body** as a string (the file content starting with `{`), not a path. Putting a path here causes `MalformedJsonException` from the Google client.

```yaml
storage:
  type: gcs
  bucket: my-data-bucket
  auth:
    serviceAccountJson: |
      {
        "type": "service_account",
        "project_id": "my-project",
        ...
      }
```

| Auth property | Description |
|---------------|-------------|
| `serviceAccountJsonPath` | Path on disk to the service account **JSON key file**. Prefer this for local/manual testing. |
| `serviceAccountJson` | **Inline** JSON key content only (must start with `{`). |
| `accessToken` | Short-lived OAuth2 bearer token (emulators / special cases). |
| `preferAmbientCredentials` | When `true`, forces ADC even if other `auth` fields are set. |

Flow descriptor YAML is **not** processed by Spring property placeholders: a value like `${GOOGLE_APPLICATION_CREDENTIALS}` is passed **literally** unless you inject it another way. To use that env var, either export it for ADC (omit `auth`) or copy the resolved path into `serviceAccountJsonPath`.

Do not commit service account keys to version control.

---

## IAM permissions

The service account needs read access to objects in the bucket. The usual role is **Storage Object Viewer** (`roles/storage.objectViewer`) on the bucket or project.

```bash
gcloud storage buckets add-iam-policy-binding gs://YOUR_BUCKET \
  --member="serviceAccount:YOUR_SA@YOUR_PROJECT.iam.gserviceaccount.com" \
  --role="roles/storage.objectViewer"
```

For listing objects under a prefix, **Object Viewer** is sufficient (it includes `storage.objects.list` and `storage.objects.get`).

### Troubleshooting `401 Unauthorized`

- **`auth` nesting:** `serviceAccountJson` / `serviceAccountJsonPath` must be under `storage.auth`, not directly under `storage`. If they are ignored, Mill falls back to ADC and may call the API with the wrong identity.
- **`projectId`:** Set `storage.projectId` to the same value as `project_id` in the service account JSON when using explicit keys.
- **Key validity:** Confirm the key is not deleted or disabled in IAM → Service accounts → Keys.
- **OAuth scopes:** Mill applies the `devstorage.read_only` scope when loading service account keys; if you still see `401`, verify the key JSON is unchanged and that no HTTP proxy strips `Authorization` headers.

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
