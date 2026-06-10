# WI-278 — Cloud resource loading verification

## Goal

Prove the end-to-end deployment scenarios for flow descriptors and metadata seeds from cloud storage
while retaining local filesystem and classpath behaviour.

## Scenarios

Cover these deployment-style cases:

1. Flow backend reads descriptor YAML from `classpath:` and local `file:` locations.
2. Flow backend reads descriptor YAML from cloud object storage.
3. Metadata seed startup reads canonical seed YAML from `classpath:` and local `file:` locations.
4. Metadata seed startup reads seed YAML from cloud object storage.
5. A mixed configuration uses classpath platform seeds plus cloud-hosted tenant/customer seeds.
6. Missing cloud resource produces an actionable startup error under `fail-fast`.
7. Missing cloud resource logs and continues under `on-failure: continue`.

## Test Plan

Minimum coverage:

- Unit tests for built-in resource providers and stable-key behaviour.
- Flow backend context test using a non-file descriptor location.
- Metadata seed startup test using a non-file seed location.
- Cross-provider/full-stack startup verification as practical. Provider-specific ordering tests are
  owned by **WI-277** in each `cloud-*-autoconfigure` module.
- Provider integration tests against the existing Testcontainers-backed cloud blob source fixtures:
  MinIO for AWS S3, `fsouza/fake-gcs-server` for Google Cloud Storage, and Azurite for Azure Blob.
- Regression tests showing bare local path and `file:` path configurations still work.

Recommended full-stack validation:

- Start a Mill service test profile with:
  - `mill.data.backend.type=flow`;
  - one `mill.data.backend.flow.sources` entry pointing to a cloud-hosted descriptor;
  - `mill.metadata.seed.resources` containing classpath platform seeds and at least one cloud seed;
  - provider autoconfigure modules on the classpath.
- Query schema availability after startup.
- Assert metadata entities/facet types from the cloud seed are present.

## Constraints

- Do not add new feature scope while stabilizing verification.
- Do not rely on live cloud accounts; use existing emulator/Testcontainers-backed fixtures.
- Public/operator documentation is handled by WI-279.

## Acceptance Criteria

- End-to-end tests prove flow descriptors and metadata seeds can both load from at least one cloud
  provider.
- Provider-specific tests cover AWS S3 (`s3://`), Google Cloud Storage (`gs://`), and Azure Blob
  (`azure-blob://`) resource reads using the existing emulator/Testcontainers-backed fixtures.
- Local filesystem and classpath regression tests pass.
- The story can be closed without adding Spring or cloud SDK references to clean backend modules, and
  without adding cloud SDK references to metadata modules.

## Verification

Commands run (2026-05-15, local):

```text
./gradlew :cloud:aws:mill-cloud-aws-autoconfigure:build \
  :cloud:gcp:mill-cloud-gcp-autoconfigure:build \
  :cloud:azure:mill-cloud-azure-autoconfigure:build

./gradlew :cloud:aws:mill-cloud-aws-autoconfigure:testIT \
  :cloud:gcp:mill-cloud-gcp-autoconfigure:testIT \
  :cloud:azure:mill-cloud-azure-autoconfigure:testIT

./gradlew :data:mill-data-autoconfigure:test --tests "io.qpointz.mill.autoconfigure.data.backend.flow.FlowBackendS3DescriptorTest"

./gradlew :metadata:mill-metadata-persistence:testIT --tests "io.qpointz.mill.persistence.metadata.jpa.MetadataStartupSeedS3IT"

./gradlew :data:mill-data-autoconfigure:test --tests "io.qpointz.mill.autoconfigure.data.backend.flow.*" \
  :metadata:mill-metadata-autoconfigure:test
```

All completed successfully (Docker required for Testcontainers emulators).
