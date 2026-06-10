# WI-277 — Cloud resource providers and autoconfiguration

## Goal

Provide Spring-documented cloud resource loading capabilities from cloud modules, with Spring Boot
autoconfiguration registering Spring `ProtocolResolver`s when provider modules are present.

## Problem

After WI-274 through WI-276, flow backend consumes `BackendResourceLoader` and metadata seed loading
uses Spring `ResourceLoader`. Cloud schemes still need provider implementations. Those
implementations must not live in flow backend or metadata modules because they require cloud SDKs
and provider-specific credential handling.

## Target Design

Implement cloud Spring `ProtocolResolver` providers in the same provider autoconfigure trees
established by the cloud blob story:

| Scheme | Preferred implementation location |
|--------|-----------------------------------|
| `s3://` | `cloud/aws/mill-cloud-aws-autoconfigure` |
| `gs://` | `cloud/gcp/mill-cloud-gcp-autoconfigure` |
| `azure-blob://` | `cloud/azure/mill-cloud-azure-autoconfigure` |

Provider implementations should reuse the credential and endpoint conventions already introduced
for cloud `BlobSource` where practical. The Spring `Resource` implementations may use provider SDK
types internally, but backend/metadata consumers must not see those types.

Only create separate clean cloud resource modules if direct reuse outside Spring is clearly needed.
The default implementation path is Spring-native and minimal.

## URI Grammar

Finalize and document a deterministic grammar per scheme. Planning recommendation:

```text
s3://<bucket>/<key>
gs://<bucket>/<key>
azure-blob://<container>/<blob>
```

Open items to settle during implementation:

- How to express custom endpoints/emulators without leaking secrets into the URI.
- Whether endpoint/region/default credential mode comes from provider properties such as
  `mill.cloud.aws.s3.*`, `mill.cloud.gcp.gcs.*`, and `mill.cloud.azure.adls.*`.
- Whether query parameters are allowed, and if so which are safe for stable keys/logging. Auth-like
  query parameters must be stripped from stable keys and display locations.
- How `azure-blob://` maps to the existing Azure Blob / ADLS Gen2 implementation and endpoint
  configuration.
- For Azure specifically, define how account identity and endpoint configuration bind to
  `azure-blob://<container>/<blob>` and to the provider canonical key. The canonical key must include
  enough non-secret account/endpoint identity to avoid collisions between containers with the same
  name in different accounts or emulator endpoints.

## Autoconfiguration

Provider autoconfiguration should:

- activate only when provider SDK classes are present;
- respect existing enabled flags such as `mill.cloud.aws.s3.enabled`;
- register a Spring `ProtocolResolver` with the application `ResourceLoader`;
- own ordering enforcement for its scheme so the resolver is available before Mill data or metadata
  autoconfig resolves configured resources;
- ensure the `BackendResourceLoader` Spring adapter can resolve the same schemes;
- allow metadata seed loading to resolve cloud locations without component-specific cloud code;
- include provider-local startup-order tests proving protocol resolvers are registered before
  configured locations are resolved during context refresh / startup runners;
- avoid creating heavyweight SDK clients during Spring context refresh when possible;
- not replace built-in `classpath:` / `file:` providers.

## Constraints

- Backend and metadata modules must not import provider classes directly.
- Spring Boot autoconfigure modules may depend on Spring and provider SDKs.
- Credential material must not be included in stable keys or display locations.
- Provider canonical keys must include the provider scheme and non-secret namespace identity needed
  to avoid collisions, such as bucket/key plus endpoint or account/container/blob where relevant.
- Resource reads should stream from the SDK response rather than buffering whole objects in provider
  code unless a consumer explicitly buffers for fingerprinting.
- Providers do not define seed skip/reapply semantics from ETag, generation, last-modified, or
  object version metadata in this story; metadata seed fingerprinting remains content-MD5 based.
- Do not introduce a second provider registry parallel to Spring `ProtocolResolver`.

## Acceptance Criteria

- S3 provider opens a configured object as an `InputStream`.
- Google Cloud Storage provider opens a configured `gs://` object as an `InputStream`.
- Azure Blob provider opens a configured `azure-blob://` blob as an `InputStream`.
- Each provider defines deterministic stable keys that exclude credentials, SAS tokens, signed URL
  query parameters, and other temporary auth material.
- Each provider stable key includes enough non-secret namespace identity to avoid collisions across
  accounts/endpoints where applicable.
- Autoconfiguration registers protocol resolvers when the module is on the classpath and enabled.
- Each provider autoconfigure module has a test that fails if its resolver is registered too late
  for Mill startup consumers.
- Disabling a provider leaves the scheme unsupported with a clear error.
- Provider tests cover URI parsing, stable keys, safe display locations, sensitive query stripping,
  missing object errors, and emulator-backed reads where feasible.
- Provider read-path tests reuse the existing cloud blob source emulator fixtures: MinIO for S3,
  `fsouza/fake-gcs-server` for Google Cloud Storage, and Azurite for Azure Blob.

## Verification

- Run cloud provider unit tests.
- Run provider `testIT` suites with the existing emulator/Testcontainers setup: MinIO for S3,
  `fsouza/fake-gcs-server` for Google Cloud Storage, and Azurite for Azure Blob.
- Run autoconfiguration slice tests for AWS, GCP, and Azure provider registration.

## Notes

If sharing code with existing `StorageFactory` implementations would force awkward coupling, prefer
small duplication over leaking cloud SDK types into neutral modules.
