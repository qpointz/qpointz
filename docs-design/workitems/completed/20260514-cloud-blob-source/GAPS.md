# cloud-blob-source GAPS

Status: `draft`  
Story: [`STORY.md`](STORY.md)

## Gap Tracker

| ID | Title | Status | Priority | Affects |
|----|-------|--------|----------|---------|
| GAP-1 | Placeholder resolution scope and fallback contract need to be stated precisely | closed | medium | Story scope, WI-265, runtime materialization |
| GAP-2 | Integration-test purpose should be narrowed to blob-source interoperability, not auth coverage | closed | medium | Story scope, WI-262, WI-263, WI-264, WI-265 |
| GAP-3 | Metadata redaction and remote facet hydration need implementation ownership | closed | medium | Story scope, WI-265, `mill-data-backends`, `mill-data-autoconfigure` |
| GAP-4 | Provider auth field schemas are not frozen before implementation | closed | medium | WI-262, WI-263, WI-264, **`docs/design/data/cloud-blob-storage-auth-descriptors.md`** |
| GAP-5 | Avro fixture generation for Skymill | closed | medium | **`test/`** Skymill parquet+avro fixtures, **STORY**, **WI-262–265** |

## Purpose

This file captures implementation gaps found while reviewing the planned `cloud-blob-source` story and its work items.

These are not objections to the story direction. They are places where the current story text is either internally inconsistent, missing an implementation owner, or likely to block delivery if not clarified before work starts.

## GAP-1 - Placeholder resolution scope and fallback contract need to be stated precisely

### Summary

The story intent for placeholder resolution needs to be stated more precisely so implementation and later extension points are aligned.

### Why this is a gap

The intended goal is narrower than “full secret-provider delivery”:

- implement placeholder grammar in this story
- implement env-backed resolution in this story
- define the extension point so future providers can be added without changing the resolver contract

That is a valid scope, but the current story text still reads as if the placeholder model is fully normative while `SecretProvider` delivery may be deferred. The gap is therefore specification clarity, not necessarily missing implementation ownership.

### Risk if left unresolved

- implementers may disagree on whether `${provider://ref}` must resolve now or is only reserved grammar
- fallback behavior may drift between modules
- future provider implementations may require incompatible resolver changes if the extension point is not fixed now

### Recommended resolution

State the contract explicitly in the story and WI-265:

1. `${ID}` resolves through env/property lookup.
2. `${env://ID}` is either explicitly supported as the env-qualified form or declared equivalent to `${ID}`.
3. `${provider://ref}` is part of the grammar now, but provider-backed resolution is an extension point for later work.
4. The resolver interface must be designed now so future providers can be added by registration/configuration, without changing descriptor grammar or call sites.
5. Verification must define what happens when a provider-qualified placeholder is encountered before that provider exists:
   either verification error or documented fallback rule.

If your intended rule is “providers resolved from a provider repository with env fallback”, that fallback order should be written down unambiguously.

### Resolution (2026-05-12)

Closed in **`STORY.md`** § **Template descriptors** and **WI-265** § **In Scope** (centralized docs) + **Acceptance Criteria**:

1. **`${ID}`** — env / Spring **`Environment`** only.
2. **`${env://ID}`** — **equivalent** to **`${ID}`** (explicit env channel).
3. **`${provider://ref}`** — **frozen grammar**; resolution via **`SecretProvider`** registry when a module registers **`<provider>`**; **concrete providers** optional, ship separately.
4. **Facade / SPI** — **Registration-based** extension; **must not** change descriptor types or resolver call-site signatures when adding providers.
5. **Verification** — **Fail closed**: missing **`<provider>`** or failed provider lookup ⇒ **`VerificationIssue`**; **no** silent env fallback for **`${provider://…}`** tokens.

### Story references

- [`STORY.md`](STORY.md) § **Template descriptors**
- [`WI-265-cloud-storage-wiring-docs.md`](WI-265-cloud-storage-wiring-docs.md)

### Codebase references

- [`SourceMaterializer.kt`](../../../../data/mill-data-source-core/src/main/kotlin/io/qpointz/mill/source/factory/SourceMaterializer.kt)

## GAP-2 - Integration-test purpose should be narrowed to blob-source interoperability, not auth coverage

### Summary

The story currently frames provider `testIT` as covering authentication behaviors, but the actual goal of these integration tests should be narrower: prove that each cloud `BlobSource` works end-to-end with Mill source materialization and existing format handlers.

### Why this is a gap

The important integration question for this story is:

- can the provider-specific `BlobSource` list, stream, and seek objects correctly
- can `SourceMaterializer` use it correctly
- can Parquet and Avro readers consume it correctly

There should be special focus on Parquet in these integration tests, because Parquet is the format that exercises the hardest storage requirement in this story:

- `openSeekableChannel`
- random access reads
- size discovery
- footer and ranged-read behavior

Avro still matters as a streaming path, but Parquet is the more implementation-critical integration target for cloud blob backends.

That can be proven with one stable auth method in emulator-backed tests. There is no need for `testIT` in this story to validate multiple authentication methods or cloud-native credential chains.

### Risk if left unresolved

- provider WIs will spend effort on auth permutations that do not help validate the real integration objective
- emulator-based tests may become more fragile without increasing confidence in blob-source correctness
- reviewers may conflate storage interoperability testing with authentication testing

### Recommended resolution

Update the story and provider WIs so they say explicitly:

1. provider `testIT` exists to validate blob-source interoperability with Mill sources and format handlers
2. a single stable emulator-friendly auth mode is sufficient for those tests
3. token or equivalent delegated auth is enough for `testIT`
4. authentication-method coverage is not the purpose of these integration tests
5. Parquet should be treated as the primary interoperability target because it proves seekable/random-access blob behavior, while Avro remains the secondary streaming-path check

If auth behavior needs separate validation later, that should be a different test concern from blob I/O and format integration.

### Resolution (2026-05-12)

Closed in **`STORY.md`** § **Authentication** (**Tests**) and § **Integration testing** (**Purpose of provider `testIT`**), plus
**WI-262–264** and **WI-265** (emulator matrix + CI note). **`testIT`** = **one** emulator-stable credential setup, **Parquet**
primary; **unit** tests cover **ambient vs delegated** inference; production **ambient** chains out of scope for provider **`testIT`**.

### Story references

- [`STORY.md`](STORY.md) § **Authentication**, § **Integration testing**
- [`WI-262-aws-s3-blob-storage.md`](WI-262-aws-s3-blob-storage.md)
- [`WI-263-gcp-gcs-blob-storage.md`](WI-263-gcp-gcs-blob-storage.md)
- [`WI-264-azure-adls-blob-storage.md`](WI-264-azure-adls-blob-storage.md)
- [`WI-265-cloud-storage-wiring-docs.md`](WI-265-cloud-storage-wiring-docs.md)

## GAP-3 - Metadata redaction and remote facet hydration need implementation ownership

### Summary

The story correctly identifies metadata leakage and cold-start risks around flow metadata facets, but currently assigns this mostly as documentation policy instead of explicit implementation work.

### Why this is a gap

Current `FlowDescriptorMetadataSource` behavior:

- serializes storage descriptors into metadata payloads by taking all non-`type` fields
- materializes and resolves sources while building snapshots
- therefore may trigger `listBlobs`, schema inference, and remote object-store access on first metadata fetch

For cloud descriptors, that creates two concrete risks:

1. delegated credential fields under `storage.auth` could leak into metadata payloads
2. first metadata hydration after restart can trigger remote I/O and dominate first-use latency

### Risk if left unresolved

- sensitive auth material may be exposed to metadata consumers
- operator expectations for “lazy startup” will not match first metadata access behavior
- teams may treat a docs note as sufficient while the runtime remains unsafe

### Recommended resolution

Add explicit implementation ownership, not just docs ownership:

1. redact or omit credential-bearing storage fields from metadata payloads
2. decide whether remote facet hydration is acceptable by default, feature-flagged, or disabled for cloud-backed sources
3. add tests for redaction behaviour

### Resolution (planning — 2026-05-12)

Normative contract captured in **`STORY.md`** § **Flow metadata facets — secret hygiene & inference control (GAP-3)** and
**WI-265** § **In Scope** item **7**:

1. **First line:** **`mill.data.backend.metadata.enabled`** (`true`|`false`) — no **`FlowDescriptorMetadataSource`** bean when **`false`** (global kill-switch **not** overridable per source; **secrets** + **inference performance**; **ship** relocation/alias from **`mill.data.backend.flow.metadata.enabled`** per **WI-265**).
2. **Second line:** **`mill.data.backend.metadata.redact`** — **`none`** (verbatim payloads + **WARN**), **`basic`** (default — hygiene + safe URLs), **`safe`** (**`basic`** plus allow-list / strictest display surface). **`metadata.redact` on a descriptor** overrides application default **per source**. **`metadata.enabled` on a descriptor** applies **only** when application **`metadata.enabled=true`** (per-source opt-out); global **`metadata.enabled=false`** cannot be overridden (**`STORY.md`**).
3. Remote hydration caveat and implementation/tests tracked under **WI-265**. Runtime delivery remains **pending** until implemented.

### Story references

- [`STORY.md`](STORY.md) § **Flow metadata facets — secret hygiene & inference control (GAP-3)**
- [`WI-265-cloud-storage-wiring-docs.md`](WI-265-cloud-storage-wiring-docs.md)

### Codebase references

- [`FlowDescriptorMetadataSource.kt`](../../../../data/mill-data-backends/src/main/kotlin/io/qpointz/mill/data/backend/flow/FlowDescriptorMetadataSource.kt)
- [`FlowDescriptorMetadataSourceAutoConfiguration.java`](../../../../data/mill-data-autoconfigure/src/main/java/io/qpointz/mill/autoconfigure/data/backend/flow/FlowDescriptorMetadataSourceAutoConfiguration.java)
- [`FlowBackendProperties.java`](../../../../data/mill-data-autoconfigure/src/main/java/io/qpointz/mill/autoconfigure/data/backend/flow/FlowBackendProperties.java)

## GAP-4 - Provider auth field schemas are not frozen before implementation

### Summary

The story makes auth inference normative, but the exact delegated credential fields for each provider are still deferred to later design/docs work.

### Why this is a gap

The provider WIs need these decisions before implementation starts:

- exact `storage.auth` field names
- which combinations are valid
- what counts as partial vs complete
- when explicit override is allowed
- what YAML examples look like
- what verifier failures should say

Without this, the provider WIs are forced to design their own auth shapes while they are being implemented.

### Risk if left unresolved

- provider WIs diverge in auth schema shape and verification style
- public docs and implementation may drift
- cross-provider consistency becomes a cleanup task instead of a design decision

### Recommended resolution

Create a small prerequisite design decision before or at the start of implementation:

- freeze provider auth field sets for AWS, GCP, and Azure
- define the optional explicit override knob
- make WI-262, WI-263, and WI-264 consume that decision rather than inventing local variants

### Story references

- [`STORY.md`](STORY.md)
- [`WI-263-gcp-gcs-blob-storage.md`](WI-263-gcp-gcs-blob-storage.md)
- [`WI-264-azure-adls-blob-storage.md`](WI-264-azure-adls-blob-storage.md)

### Resolution (planning — design freeze)

Frozen **`storage.auth`** field sets (**AWS S3**, **GCS**, **Azure**) plus shared **`preferAmbientCredentials`** semantics and verifier message patterns:

- **[`docs/design/data/cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)** (**normative for WI-262–264**).

**WI-265** expands user-facing snippets in **`docs/public/`** to match the same YAML keys. **Runtime implementation** follows in provider WIs (**GAP-4 planning** is satisfied at the design layer).

### Codebase references (design artefact only)

- [`cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)

## GAP-5 — Skymill Avro fixtures (**closed**)

### Summary

Skymill **Avro** binary fixtures are **committed** under **`test/datasets/skymill/avro/`**, generated from the same model as CSV/Parquet.

### Why this was a gap (historical)

The story and WIs relied on **`write_avro`** in **`test/skymill.yaml`**, committed **`*.avro`**, and regeneration docs — earlier revisions were missing pieces.

### Resolution (2026 repository state)

Contract satisfied:

1. **`test/skymill.yaml`** defines **`write_avro`** (path **`./datasets/{model-name}/avro/{dataset-name}.avro`**) alongside **`write_csv`** / **`write_parquet`**.
2. **`test/datasets/skymill/avro/*.avro`** — one file per table (**17** artefacts), regenerated with **`make regen-skymill`** (**`qsynth run … -a`**).
3. **`test/datasets/skymill/README.md`** documents CSV, Parquet, and Avro output paths and explicit **`write_avro`** / **`-a`** usage.

### Recommended resolution (**done** — kept for audit)

Make fixture enablement explicit, with an owner:

1. ~~add **`write_avro`** to **`test/skymill.yaml`~~
2. ~~regenerate or CI-generate Avro artifacts~~
3. ~~update the Skymill dataset README~~
4. CI policy for “commit binaries vs regenerate in pipeline” remains a **WI-265**/release choice; artefacts are present in-repo for **`testIT`**.

### Story references

- [`STORY.md`](STORY.md)
- [`WI-262-aws-s3-blob-storage.md`](WI-262-aws-s3-blob-storage.md)
- [`WI-263-gcp-gcs-blob-storage.md`](WI-263-gcp-gcs-blob-storage.md)
- [`WI-264-azure-adls-blob-storage.md`](WI-264-azure-adls-blob-storage.md)
- [`WI-265-cloud-storage-wiring-docs.md`](WI-265-cloud-storage-wiring-docs.md)

### Codebase references

- [`test/skymill.yaml`](../../../../test/skymill.yaml)
- [`test/datasets/skymill/README.md`](../../../../test/datasets/skymill/README.md)

## Suggested Review Order

1. ~~GAP-1~~ — **closed** (placeholder contract in **`STORY.md`** + **WI-265**).
2. ~~GAP-2~~ — **closed** (provider **`testIT`** scoped to interoperability — **`STORY.md`** + **WI-262–265**).
3. ~~GAP-3~~ — **closed** (planning — facet **`enabled`** + **`redact`** in **`STORY.md`** + **WI-265**; runtime pending).
4. ~~GAP-4~~ — **closed** (**planning** — **`docs/design/data/cloud-blob-storage-auth-descriptors.md`** links **WI-262–264**).
5. ~~GAP-5~~ — **closed** (Skymill **`write_avro`**, committed **`avro/*.avro`**, **`make regen-skymill`** + README).
