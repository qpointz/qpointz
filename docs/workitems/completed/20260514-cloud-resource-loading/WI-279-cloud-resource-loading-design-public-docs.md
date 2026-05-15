# WI-279 — Design and public docs for cloud resource loading

## Goal

Update relevant design and public documentation so future implementers and operators understand how
Spring Boot cloud resource loading works for flow descriptors and metadata seeds.

## Documentation Scope

Update implementation-facing design docs where the final implementation lands:

- Flow backend configuration: `mill.data.backend.flow.sources` accepts Spring resource locations,
  not just local paths.
- Metadata seed configuration: `mill.metadata.seed.resources` accepts Spring resource locations with
  provider resolvers.
- Provider resolver design: `s3://`, `gs://`, and `azure-blob://` are registered through cloud
  autoconfigure modules using Spring `ProtocolResolver`.
- Resolver ordering: cloud autoconfigure modules own resolver registration timing and tests.
- Stable seed keys: provider canonical keys must be deterministic, credential-free, namespace-aware,
  and paired with content-MD5 fingerprinting.
- Calcite limitation: `mill.data.backend.calcite.model` and direct `FlowSchemaFactory.descriptorFile`
  remain file-oriented; cloud deployments should mount those files when needed.

Update public/operator docs:

- Copyable local examples: `classpath:` and `file:`.
- Copyable cloud examples: `s3://`, `gs://`, and `azure-blob://`.
- Required modules/dependencies for each provider.
- Credential guidance: use ambient credentials or provider properties/environment; do not put secrets
  in resource URIs.
- Emulator/development guidance: MinIO for S3, `fsouza/fake-gcs-server` for Google Cloud Storage,
  and Azurite for Azure Blob.

## Constraints

- Do not document cloud SDK-specific implementation classes as required backend or metadata APIs.
- Do not claim directory scanning/listing support unless implemented by prior WIs.
- Do not document `gcs://` or `adls://` as supported operator spellings unless WI-277 explicitly
  added and tested compatibility aliases.
- Do not imply non-Spring/direct Calcite applications can load descriptors from object storage.

## Acceptance Criteria

- Design docs describe the Spring `ResourceLoader` / `ProtocolResolver` architecture and dependency
  boundaries.
- Public docs include copyable examples for local, classpath, S3, Google Cloud Storage, and Azure
  Blob resource locations.
- Public docs state which provider modules/operators must include.
- Public docs explain stable seed key and content-MD5 behaviour at an operator level.
- Public docs document the Calcite/model-file limitation and mounted-file workaround.
- Examples avoid secrets in URIs.

## Verification

- Design: `docs/design/platform/cloud-resource-loading.md` (linked from `docs/design/platform/README.md`); configuration inventory rows in `docs/design/platform/mill-configuration.md`.
- Public: `docs/public/src/backends/flow.md`, `docs/public/src/metadata/operators.md`.
- Optional full site build: `make docs-build` from repo root (not run in this WI).
