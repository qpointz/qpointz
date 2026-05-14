# WI-262 — AWS S3 blob storage for sources

Status: `done`  
Type: `✨ feature`  
Area: `cloud`, `aws`

## Problem Statement

`StorageDescriptor` only supports `type: local`. Deployments store datasets on S3; they cannot be
described using the same `SourceDescriptor` model as local files.

## Goal

Provide an S3-backed **`BlobSource`** (list blobs under a bucket + prefix, readable streams and
**seekable** channels for Parquet) driven by a new **`StorageDescriptor`** subtype and **`StorageFactory`**
SPI implementation under **`cloud/aws/`**: a **Spring-free** blob module (**`mill-cloud-aws-blob`** —
name TBD) plus a **`cloud/aws/`** Spring Boot **autoconfigure** module that depends on it (see **`STORY.md`**
§ Gradle module layout and **§ Cold start**; cross-cutting runbook in **WI-265**).

## In Scope

1. **`S3StorageDescriptor`** (`@JsonTypeName("s3")`) —
   bucket, optional key prefix, region, optional custom endpoint URL, optional **`auth`** block.
   **Authentication** follows **story [`STORY.md`](STORY.md) § Authentication** — exact **`storage.auth`** keys, bundles,
   **`preferAmbientCredentials`**, and **`VerificationIssue` patterns** are **normative for implementation** in
   **`docs/design/data/cloud-blob-storage-auth-descriptors.md`** (**GAP-4**). **Delegated** bundles use **`accessKeyId` +
   `secretAccessKey`** (+ optional **`sessionToken`**) only; verifier rejects unknown keys **and partial** bundles.
2. **`S3BlobSource`** implementing **`BlobSource`**: **`listBlobs`**, **`openInputStream`**, **`openSeekableChannel`**.
3. **`S3BlobPath`** (implements **`BlobPath`**) using a stable **`URI`** (for example **`s3://`**).
4. SPI: **`META-INF/services/io.qpointz.mill.source.factory.StorageFactory`** and **`DescriptorSubtypeProvider`**
   for Jackson polymorphism alongside **`LocalStorageDescriptor`**.
5. Unit tests plus integration tests against **MinIO** (S3-compatible API; rangable GET behavior for seeks).
   **LocalStack** may be used optionally but **MinIO** is the default **`testIT`** target for S3.
6. **`testIT`** seeding objects from **`test/datasets/skymill/parquet/*.parquet`** and
   **`test/datasets/skymill/avro/*.avro`** into the emulator bucket (paths under **`test/skymill.yaml`** experiments **`write_parquet`** / **`write_avro`**; regenerate with **`make regen-skymill`** — **`test/datasets/skymill/README.md`**); assert schema inference + read for **both**
   formats (**Parquet** → primary proof of **seekable** channel / ranged reads; **Avro** → **streaming** path).
   **`testIT`** uses **one** stable **emulator-friendly** credential setup (**typically delegated** static keys or
   tokens for MinIO — see **`STORY.md`** § **Integration testing** / **Purpose of provider `testIT`**); **not** a dual
   **ambient-vs-delegated** emulator matrix.
7. **Unit tests** (and **`Verifiable`**) **must** cover **descriptor inference** for **both** **ambient** (no delegated keys)
   and **delegated** (credential keys populated) branches so SDK credential construction **does not** regress without claiming
   CI validates production **IMDS / IRSA** behaviour.
8. **Cold start (`STORY.md`)** — Implement **`S3BlobSource`** / client lifecycle so **AWS SDK v2 clients are not
   constructed at Spring `ApplicationContext` refresh** unless **`@Lazy`** or equivalent; pay **HTTP client + TLS**
   cost at **`StorageFactory#create`** or on **first** **`listBlobs` / open** (document in KDoc which). **Ambient**
   **`DefaultCredentialsProvider`**: first real API call may trigger **IMDS** / **container role** fetch—document
   **first-touch latency** for operators. **Delegated** static creds + **MinIO**: **no IMDS**; still document **DNS +
   TLS** to **`endpoint`**. **`BlobSource.close()`** releases clients if the SDK recommends explicit shutdown.
9. **`cloud/aws/…-autoconfigure`** — **`@AutoConfiguration`** guarded by **`@ConditionalOnClass`** for the
   AWS SDK types used; **thin** config: register SPI / optional beans **without** `@PostConstruct` **listObjects** or
   **verify-all-sources** (bootstrap storage checks **deferred** to WI-265 product policy).

## Out of Scope

- Writes / export sinks (unless reuse of existing **`BlobSink`** patterns is trivial in the same WI).
- Full IAM federation documentation beyond what fits in module KDoc.

## Acceptance Criteria

- A YAML **`SourceDescriptor`** with `storage.type: s3` loads through existing **`SourceObjectMapper`** tooling.
- **`SourceMaterializer.createBlobSource`** succeeds when the new factory is on the classpath.
- Parquet schema inference succeeds against a bucket object using **`BlobInputFile`** (requires working seek/size).
- CI-runnable emulator tests prove list + read + seek without live AWS credentials.
- **Ambient** vs **delegated** credential **inference** and SDK branch selection are implemented with clear KDoc rules,
  validated by **`Verifiable`**, and covered by **unit** (or similarly fast) tests for **both** branches — **without**
  implying emulator **`testIT`** proves production **ambient** chains (**`STORY.md`** § Authentication **Tests**).
- **`testIT`** (single emulator-stable auth — typically **delegated** keys for MinIO) proves **list + read + seek** with live
  **`SourceMaterializer`** + format handlers; **Parquet** is the **primary** assertion target (**footer / random access**).
- **`testIT`** reads at least **one Parquet** and **one Avro** object sourced from **`test/datasets/skymill/`**
  after emulator upload (**not** bespoke inline bytes).
- **Cold-start contract** documented in module KDoc + cross-linked design doc: **no eager** blob I/O during Spring
   context initialization by default; **first-touch** cost and **credential chain** behaviour described for prod + MinIO.

## Test Plan

- **Unit**: descriptor verification (blank bucket, malformed endpoint), **`BlobPath`** URI stability; **inference**
  branches (**ambient** vs **delegated**) for credential construction (**no** live cloud / IMDS).
- **Integration (`testIT`)**: **MinIO** via Testcontainers (or equivalent documented in **WI-265**); **one**
  emulator-documented credential configuration (**typically delegated** static keys); custom endpoint + path-style /
  region as needed; upload Skymill **Parquet** + **Avro**; list under prefix; **prioritize Parquet** (**seek**, footer,
  row read via **`BlobInputFile`**); **Avro** smoke on **`openInputStream`**. Purpose is **blob + format interoperability**,
  not auth-method coverage (**`STORY.md`** § **Purpose of provider `testIT`**).
- **Cold start (where applicable)**: assert **no** Spring **full** context test requires **successful S3 list at
  `contextRefreshed`** unless an explicit **eager-verify** test slice is added later (**WI-265** default **lazy**).

## Risks and Mitigations

- **Seekable S3 reads** — Implement via ranged GET wrapper or documented SDK-supported channel; spike early.
- **Dependency weight** — Keep AWS SDK confined to **`cloud/aws/*blob*`** module(s); **`mill-service`** pulls
  **`cloud/aws/*-autoconfigure*`** only when S3 support is desired.
- **IMDS / role assumption latency on cold VPC** — First request after deploy may **stall** if metadata service is
  slow; mitigate with **health docs**, **timeouts** per AWS SDK guidance, prefer **IRSA/WebIdentity** paths that avoid
  legacy IMDS where possible (**ops doc**, not code hack).
- **Global `S3Client` bean anti-pattern** — Eager singleton clients **tied** to context refresh **hurt** startup;
  prefer factory-per-**`BlobSource`** or **lazy** `@Bean` (**WI-265** aligns Spring patterns).

## Implementation Notes

**Branch:** `feat/cloud-blob-sources`

### Modules created

| Module path | Artifact |
|-------------|----------|
| `cloud/aws/mill-cloud-aws-blob` | Spring-free S3 blob source |
| `cloud/aws/mill-cloud-aws-autoconfigure` | Boot autoconfigure for S3 |

### Key classes (`cloud/aws/mill-cloud-aws-blob`)

| Class | Role |
|-------|------|
| `S3StorageDescriptor` | `@JsonTypeName("s3")` — bucket, prefix, region, endpoint, auth |
| `S3AuthDescriptor` | accessKeyId, secretAccessKey, sessionToken, preferAmbientCredentials |
| `S3BlobSource` | `BlobSource` impl — list/stream/seek via AWS SDK v2 `S3Client` |
| `S3BlobPath` | `BlobPath` with `s3://bucket/key` URI |
| `S3SeekableByteChannel` | Range-read backed `SeekableByteChannel` for Parquet |
| `S3StorageFactory` | `StorageFactory` SPI impl |
| `S3DescriptorSubtypeProvider` | `DescriptorSubtypeProvider` SPI for Jackson |

### Key classes (`cloud/aws/mill-cloud-aws-autoconfigure`)

| Class | Role |
|-------|------|
| `S3StorageProperties` (Java) | `@ConfigurationProperties` |
| `S3AutoConfiguration` (Kotlin) | `@AutoConfiguration` with `@ConditionalOnClass` |

### SPI registration

- `META-INF/services/io.qpointz.mill.source.factory.StorageFactory`
- `META-INF/services/io.qpointz.mill.source.descriptor.DescriptorSubtypeProvider`

### Tests

- **Unit:** `S3StorageDescriptorTest`, `SpiWiringTest` (`src/test/`)
- **Integration:** `S3FlowBackendIT` (`src/testIT/`) — MinIO via Testcontainers, uploads Skymill parquet+avro, tests blob list/stream/seek + full Flow backend SQL queries

### Dependencies added to `libs.versions.toml`

- `awsSdk = "2.42.30"` → `aws-sdk-s3`, `aws-sdk-sts`

## Related

- **Story**: [`STORY.md`](STORY.md) — especially **§ Cold start**, **§ Template descriptors** (future placeholder wiring).
- **[`cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)** — frozen **`storage.auth`** for S3 (**GAP-4**).
- [**WI-263**](WI-263-gcp-gcs-blob-storage.md), [**WI-264**](WI-264-azure-adls-blob-storage.md) (parity across cloud vendors).
- **WI-265** — Gradle includes, **`mill-service`** dependency, consolidated **Cold start** / Helm / BNF docs.
