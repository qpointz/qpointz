# Cloud resource loading

Mill can load **configuration YAML** from object storage — not only from `classpath:` and `file:` paths. This applies to:

- **Flow backend** — entries in `mill.data.backend.flow.sources` (source descriptor files)
- **Metadata seeds** — entries in `mill.metadata.seed.resources` (canonical metadata YAML)

Query-time **data files** (Parquet, CSV, etc.) use a separate mechanism: the `storage:` block inside each source descriptor. See [Authentication for query data](#authentication-for-query-data) below.

---

## Supported URL schemes

| Scheme | Example | Autoconfigure module |
|--------|---------|----------------------|
| `s3://` | `s3://my-bucket/path/descriptor.yaml` | `mill-cloud-aws-autoconfigure` |
| `gs://` | `gs://my-bucket/path/seed.yaml` | `mill-cloud-gcp-autoconfigure` |
| `azure-blob://` | `azure-blob://container/path/seed.yaml` | `mill-cloud-azure-autoconfigure` |

Add the JAR for each provider you use. Bucket or container name is part of the URL only — **not** in `mill.cloud.*`.

---

## `mill.cloud.*` properties (config loading)

Credentials and client overrides for **all** cloud config URLs of a given provider are set once under that provider’s prefix:

### Amazon S3 (`mill.cloud.aws.s3.*`)

| Property | Description |
|----------|-------------|
| `enabled` | Default `true`. Set `false` to disable S3 autoconfiguration. |
| `endpoint` | Optional S3-compatible endpoint (MinIO, LocalStack). Enables path-style access. |
| `region` | Optional AWS region. |
| `access-key` / `secret-key` | Optional static credentials. When omitted, the AWS SDK **default credential chain** is used. |

### Google Cloud Storage (`mill.cloud.gcp.gcs.*`)

| Property | Description |
|----------|-------------|
| `enabled` | Default `true`. |
| `emulator-host` | Optional emulator base URL (for example fake-gcs-server). |
| `project-id` | Optional project id (emulator / tests). |

Production typically uses **Application Default Credentials** (workload identity, `GOOGLE_APPLICATION_CREDENTIALS`, metadata server).

### Azure Blob (`mill.cloud.azure.adls.*`)

| Property | Description |
|----------|-------------|
| `enabled` | Default `true`. |
| `connection-string` | Full storage connection string (common for emulators such as Azurite). |
| `blob-service-endpoint` | HTTPS blob service URL; use with ambient credentials (`DefaultAzureCredential`). |

Do **not** put secrets in `s3://`, `gs://`, or `azure-blob://` URIs.

---

## One authentication profile per provider

For **config loading**, Mill builds **one cloud client per provider** from the `mill.cloud.*` block above. Every URL of that scheme shares it:

- All `s3://…` entries in `mill.metadata.seed.resources` **and** `mill.data.backend.flow.sources` use the same S3 client configuration.
- The bucket name in each URL can differ; **authentication cannot differ per bucket** on the same provider today.

### Example: seeds and flow from different buckets

```yaml
mill:
  metadata:
    seed:
      resources:
        - classpath:metadata/platform-bootstrap.yaml
        - s3://metadata-config-bucket/seeds/env.yaml
  data:
    backend:
      type: flow
      flow:
        sources:
          - s3://flow-descriptors-bucket/retail.yaml
```

Both `s3://` lines use **`mill.cloud.aws.s3.*`** (or the same default credential chain). If `metadata-config-bucket` requires access key A and `flow-descriptors-bucket` requires access key B, **this layout is not supported** unless a single identity can read both buckets.

### What works today

| Scenario | Supported |
|----------|-----------|
| Same IAM user / role / static keys can read all buckets used in config URLs | Yes |
| Omit static keys; ambient credentials allowed on every config bucket | Yes |
| Seeds on `gs://…`, flow descriptors on `s3://…` (different providers) | Yes — separate `mill.cloud.gcp.*` and `mill.cloud.aws.*` |
| Seeds on `classpath:` or `file:`, flow on `s3://…` with one S3 profile | Yes |
| Two S3 buckets, two different static key pairs | **No** (current release) |

### Workarounds

1. Grant one process identity read access to both config buckets.
2. Keep one class of config on classpath or local disk; use object storage only where that single profile applies.
3. Use **different cloud providers** for different config paths (for example GCS for seeds, S3 for flow descriptors).
4. Run separate Mill deployments per trust boundary, each with its own `mill.cloud.*` settings.

Per-bucket named profiles for config URLs are **not implemented**; see `docs/design/platform/cloud-resource-loading.md` in the source tree.

---

## Authentication for query data

Loading **descriptor YAML** from `s3://` is separate from reading **data files** the descriptor points at.

Inside each source descriptor, the `storage:` block defines where Parquet/CSV/etc. live. Each descriptor can use its own `storage.auth` (or ambient credentials per [S3](../sources/storages/s3.md), [GCS](../sources/storages/gcs.md), [Azure](../sources/storages/azure.md)):

```yaml
# Loaded from s3://config-bucket/descriptor-a.yaml (uses mill.cloud.aws.s3.*)
name: vendor-a
storage:
  type: s3
  bucket: vendor-a-data
  auth:
    accessKey: ${VENDOR_A_KEY}
    secretKey: ${VENDOR_A_SECRET}
```

```yaml
# descriptor-b.yaml — different data bucket and keys
name: vendor-b
storage:
  type: s3
  bucket: vendor-b-data
  auth:
    accessKey: ${VENDOR_B_KEY}
    secretKey: ${VENDOR_B_SECRET}
```

List both descriptor **files** under `mill.data.backend.flow.sources`. Data-plane credentials are per descriptor; config-plane credentials for the YAML files themselves remain one profile per provider.

---

## Calcite models

`mill.data.backend.calcite.model` and Calcite `descriptorFile` paths remain **local or file URLs**. Mount or sync Calcite model files locally if they live in object storage.

---

## Related pages

- [Flow backend](../backends/flow.md) — `mill.data.backend.flow.sources`
- [Metadata operator guide](../metadata/operators.md) — `mill.metadata.seed.resources`
- Design (repository): `docs/design/platform/cloud-resource-loading.md`
