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

## Calcite limitation

`mill.data.backend.calcite.model` and Calcite `descriptorFile` operands remain **file-oriented**.
Cloud-hosted Calcite model files are out of scope; mount or sync files locally for those paths.

## Related public documentation

Operator examples live under `docs/public/src/` (flow backend and metadata operator guides).
