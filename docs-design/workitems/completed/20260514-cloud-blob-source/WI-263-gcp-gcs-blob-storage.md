# WI-263 — Google Cloud Storage (GCS) blob storage for sources

Status: `done`  
Type: `✨ feature`  
Area: `cloud`, `gcp`

## Problem Statement

Same gap as WI-262: object data in **GCS** cannot participate in **`BlobSource`**-based discovery and
formats.

## Goal

Under **`cloud/gcp/`**: **Spring-free** GCS blob module (**`mill-cloud-gcp-blob`**) with **`GcsStorageDescriptor`** (`@JsonTypeName("gcs")`),
**`GcsBlobSource`**, **`GcsBlobPath`**, and SPI registration, plus **`cloud/gcp/`** Spring Boot **autoconfigure**
depending on it (**`STORY.md`** § Gradle module layout **+ § Cold start**; **WI-265** aggregates runbook).

## In Scope

1. Descriptor covering bucket name, optional object prefix, optional project/emulator endpoint for tests.
2. **`BlobSource`** implementation using the official GCS client, with **`openSeekableChannel`** suited to Parquet.
3. Emulator or test-double strategy (**fake-gcs-server**, GCS emulator, or equivalent) wired into **`testIT`**.
4. **`storage.auth`** implementations **follow [`docs/design/data/cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)** (**GAP-4**) — delegated bundles are **`accessToken`** **or** **`serviceAccountJson`** **or** **`serviceAccountJsonPath`** (exactly **one** non-empty); **`preferAmbientCredentials`** semantics and unknown-key **`Verifiable`** rules **verbatim** therein. Ambient inference when delegated bundle absent (**ADC / workload**).
5. **`testIT`** seeded from **`test/datasets/skymill/parquet/*.parquet`** and
   **`test/datasets/skymill/avro/*.avro`** (see **`test/skymill.yaml`** **`write_parquet`** / **`write_avro`**; **`make regen-skymill`**):
   emulate GCS-compatible API (**fake-gcs-server** or official emulator **if suitable**); **Parquet** is the **primary**
   proof of **seekable** / ranged behaviour; **Avro** confirms **streaming**. Use **one** stable **emulator-friendly**
   auth setup (**typically delegated** token or keys the emulator documents — **`STORY.md`** § **Purpose of provider `testIT`**).
6. **Unit tests** (and **`Verifiable`**) **must** cover **inference** for **both** **ambient** (no delegated keys) and
   **delegated** credential branches **without** claiming **`testIT`** validates production **ADC / workload identity**.
7. **Cold start (`STORY.md`)** — **`Storage` / HTTP transport** must **not** be initialized during Spring refresh by
   default (**`cloud/gcp/…-autoconfigure`** follows **`@ConditionalOnClass`** + **`@Lazy`** patterns in **WI-265**).
   **ADC / `GoogleCredentials`**: **first token** fetch (metadata server, **workload identity**) adds latency—document.
   **`StorageOptions` / client** lifecycle: construct at **`StorageFactory#create`** or **defer** to first **`listBlobs`**
   (pick one; KDoc). For **`fake-gcs-server`** (or other emulators), document REST first-connection behaviour for **`testIT`**
   vs prod.
8. **`cloud/gcp/…-autoconfigure`** — no **`@PostConstruct`** bucket probes; SPI registration only unless
   product explicitly adds optional **readiness** contributor later (**WI-265**).

## Out of Scope

- Cross-cloud URI schemes beyond a single consistent **`BlobPath`** representation for gcs (for example **`gs://`** URIs).

## Acceptance Criteria

- Sources can declare GCS storage and run through **`SourceMaterializer`** with **`mill-service`** classpath including
  the new module (see WI-265 for wiring specifics).
- Parquet footer read works against at least one Skymill Parquet object in **`testIT`** after emulator seeding.
- Avro smoke read works against at least one Skymill Avro object the same way.
- **Ambient** vs **delegated** **inference** is implemented and covered by **unit** / **`Verifiable`** tests for **both**
  branches (**`STORY.md`** § Authentication **Tests**); **`testIT`** does **not** need dual emulator auth configs.
- **`testIT`** uses **one** emulator-stable credential strategy; proves **Parquet** (**primary**) + **Avro** (**streaming**)
  through **`SourceMaterializer`** + format handlers.
- Cold-start semantics documented: **lazy** client construction policy, **ADC first-touch** latency, **`close()`**
  behaviour for GCS clients if applicable.

## Risks and Mitigations

- **Seek implementation** — May require composable ranged-read channel (shared abstraction with WI-262 if duplicated).
- **Workload identity cold token** — First **`OAuth2`** token from metadata can **timeout** on busy clusters; align
  timeouts with GCP client defaults and document **retry** behaviour for operators.

## Test Plan

- **Unit**: descriptor verification, **`BlobPath`** / **`URI`** conventions for **`gs://`** (if exposed); **inference**
  for **ambient** vs **delegated** auth branches.
- **Integration**: **fake-gcs-server** (or agreed emulator) per **WI-265** matrix; **single** documented credential setup;
  seed Skymill **Parquet** + **Avro**; **emphasize Parquet** seek/footer path; Avro stream smoke (**`STORY.md`** § **Purpose of provider `testIT`**).
- **Cold start slice** (optional autoconfigure **`testIT`**): Boot context comes up **without** calling **`listBuckets`**
  at **`ApplicationReady`** unless **eager-verify** enabled (**WI-265** policy).

## Implementation Notes

**Branch:** `feat/cloud-blob-sources`

### Modules created

| Module path | Artifact |
|-------------|----------|
| `cloud/gcp/mill-cloud-gcp-blob` | Spring-free GCS blob source |
| `cloud/gcp/mill-cloud-gcp-autoconfigure` | Boot autoconfigure for GCS |

### Key classes (`cloud/gcp/mill-cloud-gcp-blob`)

| Class | Role |
|-------|------|
| `GcsStorageDescriptor` | `@JsonTypeName("gcs")` — bucket, prefix, projectId, endpoint, auth |
| `GcsAuthDescriptor` | accessToken, serviceAccountJson, serviceAccountJsonPath, preferAmbientCredentials |
| `GcsBlobSource` | `BlobSource` impl — list/stream/seek via `com.google.cloud.storage.Storage` |
| `GcsBlobPath` | `BlobPath` with `gs://bucket/name` URI |
| `GcsSeekableByteChannel` | Range-read backed `SeekableByteChannel` for Parquet |
| `GcsStorageFactory` | `StorageFactory` SPI impl |
| `GcsDescriptorSubtypeProvider` | `DescriptorSubtypeProvider` SPI for Jackson |

### Key classes (`cloud/gcp/mill-cloud-gcp-autoconfigure`)

| Class | Role |
|-------|------|
| `GcsStorageProperties` (Java) | `@ConfigurationProperties` |
| `GcsAutoConfiguration` (Kotlin) | `@AutoConfiguration` with `@ConditionalOnClass(Storage::class)` |

### SPI registration

- `META-INF/services/io.qpointz.mill.source.factory.StorageFactory`
- `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`

### Tests

- **Unit:** `GcsStorageDescriptorTest`, `SpiWiringTest` (`src/test/`)
- **Integration:** `GcsFlowBackendIT` (`src/testIT/`) — fake-gcs-server via Testcontainers, uploads Skymill parquet+avro, tests blob list/stream/seek + full Flow backend SQL queries

### Dependencies added to `libs.versions.toml`

- `googleCloudStorage = "2.64.1"` → `google-cloud-storage`

### Known quirks

- `cloud/gcp/mill-cloud-gcp-autoconfigure` needs explicit `compileOnly(libs.google.cloud.storage)` for `@ConditionalOnClass` resolution

## Related

- [`STORY.md`](STORY.md) — **§ Cold start**, **§ Template descriptors** (future placeholder wiring).
- [**WI-262**](WI-262-aws-s3-blob-storage.md), [**WI-264**](WI-264-azure-adls-blob-storage.md) (format/emulator parity across vendors).
- [**WI-265**](WI-265-cloud-storage-wiring-docs.md) — Gradle includes, **`mill-service`** dependency, consolidated **Cold start** / Helm / BNF docs.
- **[`cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)** — frozen **`storage.auth`** keys (**GAP-4**) for GCS delegated bundles.
