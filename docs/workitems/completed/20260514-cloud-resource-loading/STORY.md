# cloud-resource-loading

## Goal

Make Mill deployment descriptors and startup metadata seeds readable from protocol-addressed
resource locations, so cloud deployments can keep operational files in object storage while
preserving existing local filesystem and classpath behaviour.

This story closes the remaining cloud-deployment gaps after
[`cloud-blob-source`](../../completed/20260514-cloud-blob-source/STORY.md):

- Flow backend source descriptor YAML is still treated as local filesystem paths
  (`FlowBackendProperties.sources` → `Path` → `MultiFileSourceRepository`).
- Calcite model / `FlowSchemaFactory` descriptor operands still assume local files where they read
  deployment descriptors.
- Metadata startup seeds are loaded through Spring `ResourceLoader`, which handles
  `classpath:` and `file:` well but does not know Mill cloud storage schemes.

The target model is **Spring ResourceLoader-first** in Boot applications, with only a tiny clean
adapter contract at the backend boundary:
configuration strings start with a scheme such as `classpath:`, `file:`, `s3://`, `gs://`, or
`azure-blob://`. Clean backend modules consume a small backend-owned contract such as
`BackendResourceLoader`, not Spring `ResourceLoader`. Spring Boot autoconfiguration wires that
contract to the application `ResourceLoader`, extended with provider-specific `ProtocolResolver`s
from `cloud/*` modules.

Keep changes close to out-of-the-box Spring behaviour. Do not build a general Mill resource
framework unless Spring's `Resource` / `ResourceLoader` / `ProtocolResolver` model cannot satisfy a
specific requirement.

## Non-Negotiables

- Existing `classpath:` and `file:` locations continue to work.
- Bare local paths remain supported as a backwards-compatible alias for `file:`.
- Data backend and metadata core code must not reference AWS, GCP, Azure, or other cloud SDK types.
- Spring APIs must not be introduced into clean modules such as `data/mill-data-backends`,
  `data/mill-data-source-core`, `metadata/mill-metadata-core`, or any new resource core module.
- Cloud SDK dependencies stay in `cloud/<provider>/...` modules; Spring Boot registration stays in
  `cloud/<provider>/...-autoconfigure`.
- Resource loading must be lazy enough that application startup does not eagerly contact object
  stores except for explicitly configured metadata seed execution or flow descriptor parsing.

## Proposed Architecture

Use Spring's resource infrastructure as the primary implementation and introduce only the smallest
backend contract needed to avoid Spring imports in clean backend modules:

```text
BackendResourceLoader
  └─ Spring autoconfigure adapter
       └─ Spring ResourceLoader
            ├─ built-in classpath: / file:
            ├─ s3:// ProtocolResolver from cloud/aws autoconfigure
            ├─ gs:// ProtocolResolver from cloud/gcp autoconfigure
            └─ azure-blob:// ProtocolResolver from cloud/azure autoconfigure
```

The backend contract should cover only what flow descriptor loading needs:

- open an `InputStream` for a configured location;
- expose a safe display location for diagnostics.

`data/mill-data-autoconfigure` is the composition layer: it may depend on Spring and adapt Spring
`Resource` to the backend contract. `data/mill-data-backends` and `data/mill-data-source-calcite`
must stay Spring-free.

Stable seed ledger keys remain a metadata-autoconfigure concern because metadata seed loading
already lives in Spring autoconfiguration and can use Spring `Resource` directly.

Flow descriptor loading and metadata seed loading then share the same semantics:

```yaml
mill:
  data:
    backend:
      type: flow
      flow:
        sources:
          - classpath:flow/skymill-flow.yml
          - file:/etc/mill/flow/warehouse.yml
          - s3://mill-config-prod/flow/sales.yml
          - gs://mill-config-prod/flow/marketing.yml
          - azure-blob://config-container/flow/finance.yml

  metadata:
    seed:
      resources:
        - classpath:metadata/platform-bootstrap.yaml
        - s3://mill-config-prod/metadata/skymill-canonical.yaml
        - azure-blob://config-container/metadata/finance-canonical.yaml
```

Exact cloud URI grammar is finalized in the provider WI, but all schemes must be explicit,
documented, and round-trip to stable ledger keys.

## Decisions

These choices lock planning gaps before implementation. They may be refined only if a WI proves a
different approach is unavoidable.

- **Backend resource contract module.** The Spring-free loader interface (for example
  `BackendResourceLoader`) lives in **`data/mill-data-backend-core`** and is used only by backend /
  autoconfigure paths such as flow repository wiring. That module already hosts backend execution
  contracts, **`mill-data-backends`** already depends on it, and **`mill-data-autoconfigure`** can
  adapt it to Spring without introducing Spring into **`mill-data-backends`**. Do **not** add a
  dependency from **`mill-data-source-calcite`** to **`mill-data-backend-core`** just to reuse this
  contract; **`FlowSchemaFactory`** must stay independent.

- **Protocol resolver registration timing.** Cloud providers register **`ProtocolResolver`**
  extensions on the application **`ResourceLoader`** from
  **`cloud/<provider>/<provider>-autoconfigure`**. Each cloud autoconfigure module owns both
  registration and ordering enforcement for its scheme. Provider autoconfigure tests must prove
  registration happens before Mill startup code resolves configured resource locations. **WI-277**
  owns those provider-specific startup-order tests; **WI-278** may add cross-provider/full-stack
  verification, but data and metadata modules must not carry provider-specific ordering hacks.

- **Stable seed ledger keys.** Metadata seed skip/reapply continues to use
  **`metadata_seed.seed_key`** plus the content MD5 fingerprint. For cloud resources, each provider
  must define a deterministic, credential-free canonical key for the resolved object. The key must
  include the provider scheme and non-secret namespace identity needed to avoid collisions, for
  example bucket/key plus endpoint, or account/container/blob where relevant. Do not include
  credentials, SAS tokens, signed URL query parameters, or other temporary auth material. If
  canonicalization is ambiguous, fall back to a normalized configured location with sensitive parts
  stripped, and document the limitation in **WI-277** / **WI-278**.

- **Seed content fingerprinting.** Metadata seed change detection remains content-based: read the
  resolved resource bytes, compute the existing MD5 fingerprint, and compare it with
  **`metadata_seed.fingerprint`**. Cloud provider metadata such as S3 ETag, GCS generation, Azure
  ETag, last-modified, or object version IDs is not used for skip/reapply decisions in this story.
  The implementation may read each seed object once and reuse those bytes for both fingerprinting
  and import. Provider-metadata optimizations require a separate story because they need
  provider-specific semantics and likely ledger/schema changes.

- **Recommended URI spellings for operators.** Public examples and docs use the Spring-documented
  resource location spellings: **`s3://`** for AWS S3, **`gs://`** for Google Cloud Storage, and
  **`azure-blob://`** for Azure Blob Storage. Do not introduce Mill-specific aliases such as
  **`gcs://`** or **`adls://`** unless a provider WI explicitly documents a compatibility alias and
  tests it.

- **End-to-end vs provider coverage.** At least **one** cloud provider must be exercised in a
  **full-stack or near-stack** test (flow descriptor + metadata seed from that provider) for story
  closure. **AWS S3 (`s3://`)**, **Google Cloud Storage (`gs://`)**, and **Azure Blob Storage
  (`azure-blob://`)** each require **resolver registration, read path, URI parsing, stable key, and
  safe display** coverage in provider tests. Provider read-path coverage should reuse the existing
  cloud blob source emulator pattern: **MinIO** for S3, **`fsouza/fake-gcs-server`** for Google
  Cloud Storage, and **Azurite** for Azure Blob. Three separate full-stack boots are **not**
  required; provider-level emulator **`testIT`** coverage is sufficient for schemes not used in the
  single full-stack/near-stack scenario.

- **Calcite model property.** **`mill.data.backend.calcite.model`** remains file-oriented if Calcite
  requires a local filesystem path. Do not add complex staging/download logic in this story. For
  cloud deployments that use Calcite model files, the supported approach is mounting the model file
  into the container/pod (ConfigMap, Secret, volume, init container, or equivalent) and pointing
  **`mill.data.backend.calcite.model`** at that mounted path. Document this limitation and workaround
  in **WI-275** / **WI-278**.

- **`FlowSchemaFactory` descriptor operands.** **`FlowSchemaFactory`** is outside the Spring Boot
  cloud deployment path. Keep **`descriptorFile`** local-file-only and do not implement
  **`descriptorResource`** in this story. Cloud descriptor loading is supported through the Spring
  app flow backend property **`mill.data.backend.flow.sources`**. Non-Spring / direct Calcite usage
  continues to use local files or mounted files.

- **Implementation language.** Prefer Kotlin for new implementation code in this story. Use Java
  for Spring `@ConfigurationProperties` / properties-bound configuration classes where the existing
  module convention already uses Java for those bindings.

## Scope

In scope:

- Minimal backend resource contract and Spring `ResourceLoader` adapter.
- Flow backend descriptor repositories reading configured locations through that API.
- Metadata seed startup reading configured locations through that API.
- Cloud `ProtocolResolver` / `Resource` providers for AWS S3 (`s3://`), Google Cloud Storage
  (`gs://`), and Azure Blob Storage (`azure-blob://`), implemented in cloud modules.
- Spring Boot autoconfiguration in each cloud provider module that registers cloud providers when
  the relevant module is on the classpath and enforces resolver ordering.
- Tests that prove local and cloud-backed descriptor/seed loading paths work.
- Design and public documentation updates with cloud deployment examples.

Out of scope:

- General-purpose write support for cloud resources.
- A custom resource registry that duplicates Spring `ProtocolResolver`.
- Cloud resource loading for non-Spring/native/direct Calcite applications.
- Directory scanning/listing for descriptor discovery unless a provider can support it without
  expanding this story.
- HTTP/HTTPS resource loading unless explicitly added later.
- New secret-provider abstraction; this story may consume existing credential descriptor patterns
  but does not define a secret manager.
- Changing cloud blob data read/write behaviour already covered by the prior cloud blob story.

## Current Touchpoints

- `data/mill-data-autoconfigure/.../FlowBackendAutoConfiguration.java` maps flow source strings to
  `Path`.
- `data/mill-data-backends/.../SourceDefinitionReader.java` parses descriptors from `Path`.
- `data/mill-data-backends/.../SingleFileSourceRepository.java` and
  `MultiFileSourceRepository.java` store `Path` instances.
- `data/mill-data-source-calcite/.../FlowSchemaFactory.kt` supports only the `descriptorFile`
  operand and reads through `java.io.File`.
- `data/mill-data-autoconfigure/.../CalciteBackendProperties.java` exposes `model` as a Calcite
  model file path.
- `metadata/mill-metadata-autoconfigure/.../MetadataSeedStartup.kt` uses Spring `ResourceLoader`.
- `metadata/mill-metadata-autoconfigure/.../MetadataSeedKey.kt` derives stable keys from Spring
  resources.
- Existing cloud modules already provide cloud `BlobSource` implementations and Spring
  autoconfiguration under `cloud/aws`, `cloud/gcp`, and `cloud/azure`.

## Work Items

- [x] WI-274 — Minimal backend resource contract and Spring adapter (`WI-274-spring-free-resource-loading-core.md`)
- [x] WI-275 — Flow descriptor resource locations (`WI-275-flow-descriptor-resource-locations.md`)
- [x] WI-276 — Metadata seed resource locations (`WI-276-metadata-seed-resource-locations.md`)
- [x] WI-277 — Cloud resource providers and autoconfiguration (`WI-277-cloud-resource-providers-autoconfigure.md`)
- [x] WI-278 — Cloud resource loading verification (`WI-278-cloud-resource-loading-verification-docs.md`)
- [x] WI-279 — Design and public docs for cloud resource loading (`WI-279-cloud-resource-loading-design-public-docs.md`)

## Implementation Order

1. **WI-274** first: define the backend contract and Spring `ResourceLoader` adapter.
2. **WI-275** and **WI-276** next: these may run in parallel once WI-274 is available.
3. **WI-277** after the application `ResourceLoader` path is clear.
4. **WI-278** after provider implementation: verification closure.
5. **WI-279** last: design and public documentation closure.
