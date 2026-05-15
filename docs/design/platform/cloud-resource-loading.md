# Cloud resource loading (flow descriptors and metadata seeds)

## Summary

Flow backend descriptor paths (`mill.data.backend.flow.sources`) and metadata seed locations
(`mill.metadata.seed.resources`) are resolved through Spring’s `ResourceLoader` with optional
`ProtocolResolver` beans. Cloud schemes (`s3://`, `gs://`, `azure-blob://`) are implemented only in
provider autoconfigure modules so neutral data and metadata code never references cloud SDKs.

## Spring wiring

| Concern | Behaviour |
|--------|-----------|
| **Protocol resolvers** | Registered as Spring beans (`millS3ProtocolResolver`, `millGcsProtocolResolver`, `millAzureBlobProtocolResolver`) from `cloud/*/mill-cloud-*-autoconfigure`. Provider autoconfiguration uses `@AutoConfiguration(beforeName = …)` so resolvers exist before `FlowBackendAutoConfiguration` and `MetadataSeedAutoConfiguration` consume locations. |
| **Servlet web applications** | The servlet `ResourceLoader` does not treat custom schemes like ordinary classpath/file URLs. Mill therefore builds a **`DefaultResourceLoader`** for **metadata seeds** and for **`BackendResourceLoader`**, copies every `ProtocolResolver` bean onto it, and uses that loader for those reads. |
| **Stable seed keys** | Resources that implement `io.qpointz.mill.resource.MillConfigurationResourceKey` supply credential-free keys; otherwise cloud locations are normalised in `MetadataSeedKey` (query and user-info stripping). Content identity remains **MD5** of raw bytes (`MetadataSeedContentFingerprint`). |

## URI shapes (operator-facing)

| Scheme | Example | Notes |
|--------|---------|-------|
| `s3://` | `s3://bucket/path/to.yaml` | Optional `mill.cloud.aws.s3.endpoint`, `region`, `access-key`, `secret-key` for MinIO and similar. |
| `gs://` | `gs://bucket/path/to.yaml` | Optional `mill.cloud.gcp.gcs.emulator-host` and `project-id` for fake-gcs-server / emulators. |
| `azure-blob://` | `azure-blob://container/blob/path.yaml` | Use `mill.cloud.azure.adls.connection-string` or `blob-service-endpoint` with ambient credentials. |

Bucket or container names appear **only** in the URL. `mill.cloud.*` does not define a list of buckets.

## Authentication model

Two layers must not be conflated:

| Layer | What is loaded | Where credentials live |
|-------|----------------|------------------------|
| **Config loading** | Descriptor YAML and metadata seed YAML via `ProtocolResolver` (`s3://`, `gs://`, `azure-blob://`) | **One property object per provider** (`S3StorageProperties`, `GcsStorageProperties`, `AdlsStorageProperties`). All URLs of that scheme share one lazy SDK client. |
| **Query data** | Files under `storage:` in each `SourceDescriptor` | **Per descriptor** — `storage.auth` in YAML or ambient chain via `S3StorageFactory` / GCS / ADLS blob modules. |

Implementation: `MillS3ObjectResource` (and GCS/Azure equivalents) receives a single properties bean; `buildClient()` does not branch on bucket name for auth.

### One profile per provider (config loading)

`mill.metadata.seed.resources` and `mill.data.backend.flow.sources` can list different buckets on the same provider, for example seeds from `s3://metadata-bucket/…` and flow descriptors from `s3://flow-bucket/…`. Both locations use the **same** `mill.cloud.aws.s3.*` (or the same default credential chain). **Different static key pairs per S3 bucket for config URLs are not supported** in the current implementation.

Supported combinations:

- One identity can read every config bucket on that provider.
- Mixed schemes: seeds on `gs://…` with `mill.cloud.gcp.gcs.*`, flow on `s3://…` with `mill.cloud.aws.s3.*`.
- Config on `classpath:` / `file:` while only some paths use object storage.

Workarounds: unified IAM, split providers, split deployments, or keep mixed-auth config off object storage until named profiles exist.

Public operator detail: `docs/public/src/reference/cloud-resource-loading.md`.

### Future direction (not implemented)

Named profiles under `mill.cloud.<provider>.*` (for example `profiles.metadata` vs `profiles.flow`) and URL or mapping to select a profile would allow multiple auths per provider for config loading. Spring Cloud vendor resolvers have similar single-account limitations for generic `ResourceLoader` usage.

## Calcite limitation

`mill.data.backend.calcite.model` and Calcite `descriptorFile` operands remain **file-oriented**.
Cloud-hosted Calcite model files are out of scope; mount or sync files locally for those paths.

## Related public documentation

- `docs/public/src/reference/cloud-resource-loading.md` — operator reference (`mill.cloud.*`, one profile per provider, workarounds)
- `docs/public/src/backends/flow.md` — flow `sources` and cloud descriptors
- `docs/public/src/metadata/operators.md` — seed resources and cloud URLs
