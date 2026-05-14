# WI-264 — Azure Data Lake Storage Gen2 blob storage for sources

Status: `done`  
Type: `✨ feature`  
Area: `cloud`, `azure`

## Problem Statement

Organizations store analytical files in **Azure Data Lake Storage Gen2** (HDFS-style paths over Blob
REST). Mill needs **`BlobSource`** support with the same **`SourceDescriptor`** story as S3 and GCS.

## Goal

Provide **Spring-free** ADLS-compatible blob support under **`cloud/azure/`**: module (**`mill-cloud-azure-blob`**
or **`…-datalake`**) implementing **`AdlsStorageDescriptor`** (`@JsonTypeName("adls")`),
**`AdlsBlobSource`**, **`AdlsBlobPath`**, factories and subtype providers, plus a **`cloud/azure/`**
Spring Boot **autoconfigure** module (**`STORY.md`** § Gradle module layout **+ § Cold start**; **WI-265** aggregates runbook).

## In Scope

1. Descriptor covering storage account (**`accountUrl`**) and filesystem (container), directory prefix optional, optional **`endpoint`**. Discriminator is **`adls`** (`@JsonTypeName("adls")`), frozen in **`docs/design/data/cloud-blob-storage-auth-descriptors.md`**; **do not** invent parallel **`storage.auth`** field names beyond it.
2. **`storage.auth`** implementations **follow [`docs/design/data/cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)** (**GAP-4**) — delegated bundles **`connectionString`** **or** **`accountName` + `accountKey`** (**mutually exclusive**); **`preferAmbientCredentials`** semantics and unknown-key **`Verifiable`** rules **verbatim** therein. No delegated credentials → **`DefaultAzureCredential`** (ambient inference).
3. **`BlobSource`** list + open + seek aligned with **`BlobInputFile`** requirements.
4. Integration tests via **Azurite** (or emulator agreed in **WI-265**) with fixture upload from
   **`test/datasets/skymill/parquet/*.parquet`** and **`test/datasets/skymill/avro/*.avro`**; verify
   **Parquet** (**primary**: seekable / **`BlobInputFile`**) and **Avro** (**streaming**) through existing format handlers.
   Use **one** stable **emulator-friendly** delegated bundle per **[`cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)** (typically **`auth.connectionString`** for Azurite — **`STORY.md`** § **Purpose of provider `testIT`**); **not** separate **`testIT`** suites for **ambient**
   vs **delegated** auth matrices.
5. **Unit tests** (and **`Verifiable`**) **must** cover **inference** for **both** **ambient** (**`DefaultAzureCredential`** path when
   no delegated keys) and **delegated** branches **without** claiming **`testIT`** validates production **managed identity** or full
   credential-chain ordering.
6. **Cold start (`STORY.md`)** — **`BlobServiceClient`/`DataLakeServiceClient`** (or chosen stack) **lazy** vs context
   refresh (**same rule as AWS/GCP**). **`DefaultAzureCredential`** tries **multiple** chained providers—**first**
   successful credential source may take **multiple seconds** on developer boxes (**Azure CLI**)—document **production**
   vs dev ordering (see Azure docs); **managed identity** path preferred in K8s for predictable latency. **`close()`**
   semantics documented.
7. **`cloud/azure/…-autoconfigure`** — **`@ConditionalOnClass`**, **`@Lazy`** aligned with **WI-265**; no
   storage **list** during static init.

## Out of Scope

- Full ABFS Hadoop filesystem integration (**wasb/abfs/** drivers beyond REST SDK).
- Table ACL or Azure-specific metadata facets.

## Acceptance Criteria

- Emulator-backed tests list objects under a path prefix and read **Skymill Parquet + Avro** (see **STORY.md**)
  through **`mill-data-format-parquet`** and **`mill-data-format-avro`** format handlers after fixture upload.
- Descriptor verifies required fields consistently with **`Verifiable`** patterns used by **`LocalStorageDescriptor`**.
- **Ambient** vs **delegated** **inference** is implemented and covered by **unit** / **`Verifiable`** tests (**`STORY.md`** § Authentication **Tests**); **`testIT`** uses **one** emulator-stable auth setup (**`STORY.md`** § **Purpose of provider `testIT`**).
- Cold-start semantics documented: **DefaultAzureCredential** chain impact, **lazy** client policy, **Azurite** vs cloud
  first-touch differences.

## Risks and Mitigations

- **SDK surface** (`azure-storage-blob` / `azure-storage-file-datalake`) — pick one coherent client stack and keep it in the isolated module only.
- **Credential chain “false start”** — A **failing** chain step (e.g. **IMDS** absent locally) may add **delay** before CLI
  path; document **timeout tuning** and **managed identity**-only production config to avoid **cold start penalty**.

## Test Plan

- **Unit**: **`AzureStorageDescriptor`** / **`AdlsBlobPath`** verification, **`Verifiable`** for partial credential bundles;
  **inference** for **ambient** vs **delegated** branches.
- **Integration**: **Azurite** (or emulator agreed in **WI-265**); **single** credential strategy for emulator; seed Skymill **Parquet** + **Avro**; **prioritize Parquet** seek path; Avro streaming parity with WI-262/WI-263 (**`STORY.md`** § **Purpose of provider `testIT`**).
- **Cold start slice** (optional autoconfigure **`testIT`**): full Boot context **without** storage **list**/HEAD at refresh
  unless **eager-verify** is enabled (**WI-265** policy).

## Implementation Notes

**Branch:** `feat/cloud-blob-sources`

### Modules created

| Module path | Artifact |
|-------------|----------|
| `cloud/azure/mill-cloud-azure-blob` | Spring-free Azure ADLS/Blob source |
| `cloud/azure/mill-cloud-azure-autoconfigure` | Boot autoconfigure for Azure |

### Key classes (`cloud/azure/mill-cloud-azure-blob`)

| Class | Role |
|-------|------|
| `AdlsStorageDescriptor` | `@JsonTypeName("adls")` — accountUrl, filesystem, prefix, endpoint, auth |
| `AdlsAuthDescriptor` | connectionString, accountName, accountKey, preferAmbientCredentials |
| `AdlsBlobSource` | `BlobSource` impl — list/stream/seek via `azure-storage-blob` SDK |
| `AdlsBlobPath` | `BlobPath` with Azure blob URI |
| `AdlsSeekableByteChannel` | Range-read backed `SeekableByteChannel` for Parquet |
| `AdlsStorageFactory` | `StorageFactory` SPI impl |
| `AdlsDescriptorSubtypeProvider` | `DescriptorSubtypeProvider` SPI for Jackson |

### Key classes (`cloud/azure/mill-cloud-azure-autoconfigure`)

| Class | Role |
|-------|------|
| `AdlsStorageProperties` (Java) | `@ConfigurationProperties` |
| `AdlsAutoConfiguration` (Kotlin) | `@AutoConfiguration` with `@ConditionalOnClass` |

### SPI registration

- `META-INF/services/io.qpointz.mill.source.factory.StorageFactory`
- `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`

### Tests

- **Unit:** `AdlsStorageDescriptorTest`, `SpiWiringTest` (`src/test/`)
- **Integration:** `AdlsFlowBackendIT` (`src/testIT/`) — Azurite via Testcontainers, uploads Skymill parquet+avro, tests blob list/stream/seek + full Flow backend SQL queries

### Dependencies added to `libs.versions.toml`

- `azureStorageBlob = "12.33.2"` → `azure-storage-blob`
- `azureIdentity = "1.18.2"` → `azure-identity`

### Known quirks

- Azurite requires `--skipApiVersionCheck` flag because Azure SDK v12.33.2 sends API version `2026-02-06` which Azurite doesn't support yet
- `testIT` needs explicit `implementation(libs.azure.storage.blob)` for SDK types to be visible

## Related

- [`STORY.md`](STORY.md); [**WI-262**](WI-262-aws-s3-blob-storage.md), [**WI-263**](WI-263-gcp-gcs-blob-storage.md) (format/emulator parity);
  **WI-265** (emulator matrix, BOM, probes, **`mill.flow.storage.eager-verify`** TBD).
- **[`cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)** — frozen **`storage.auth`** keys (**GAP-4**) for Azure delegated bundles.
