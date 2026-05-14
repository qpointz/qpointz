# WI-265 — Classpath wiring, emulator matrix, documentation

Status: `done`  
Type: `✨ feature` · `📝 docs`  
Area: `cloud`, `apps`, `docs`

**Story:** [`STORY.md`](STORY.md). Vendor **`BlobSource`** delivery and **`testIT`** detail live in [**WI-262**](WI-262-aws-s3-blob-storage.md),
[**WI-263**](WI-263-gcp-gcs-blob-storage.md), [**WI-264**](WI-264-azure-adls-blob-storage.md); this work item aggregates classpath wiring,
emulator matrix, design/public docs, and cross-cutting cold-start guidance.

## Problem Statement

New storage modules are useless unless optional dependencies reach runtime classpaths Spring Boot/apps use,
and operators need a single place documenting YAML fields, credential behavior, Parquet prerequisites, and **cold start**
(operator probes, lazy vs eager storage verification, first-touch latency).

## Goal

Finalize product integration touchpoints without forcing every deployment to pull all three cloud SDKs, codify **cold-start**
behaviour (**lazy** blob clients, **probe**/readiness separation, optional **startup verification**) consistent with **`STORY.md`**,
and close **GAP-3** planning for **flow metadata facet** emission (**enabled** inference gate — **secrets + performance** — plus **`redact`** hygiene) via **`WI-265`** docs and tracked runtime work.

## In Scope

1. **`settings.gradle.kts`** — register the **`cloud/`** subtree per **`STORY.md`**: **`cloud/aws`**, **`cloud/gcp`**, **`cloud/azure`**
   (include paths like **`:cloud:aws:mill-cloud-aws-blob`**, matching names chosen in WI-262–264 + autoconfigure jars).
   Document **`build-logic`** / BOM updates if Gradle conventions need extending for `cloud/**/*`.
2. **`apps/mill-service`** (`build.gradle.kts`) — optional **`implementation`** on each vendor’s **`…-autoconfigure`**
   (or **`starter`**) module so classpath brings in **blob** + SPI without depending on **`data/`** blobs. Policy for
   “all clouds” vs pick-one documented here.
3. Centralized docs:
   - **Design**: `docs/design/data/` — object storage **`BlobSource`** contract, **`URI`** conventions,
     seekable access as **range requests + bounded buffers** (never full-blob heap materialization),
     **`auth`** (**[`cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)** — frozen **`storage.auth`** keys per cloud, **`preferAmbientCredentials`**, `VerificationIssue` wording; **GAP-4** closure),
     mapping **delegated bundles** to AWS/GCP/Azure SDK types; expiry **and rotation** notes; secret-handling guidance (env / mounted files; never logged).
   - **Template vs resolved + placeholder resolution** (per **STORY.md** § **Template descriptors**, items **3–12**):
     canonical **persisted** descriptor is the **template**; **single** pass at **verify / materialize** via
     **`DescriptorPlaceholderResolver`**. **Grammar (frozen at the facade):** **`${ID}`** and **`${env://ID}`** (**equivalent**)
     resolve **only** through **`getenv`** / Spring **`Environment`** (precedence fixed in design); **`${<provider>://<reference>}`**
     dispatches to a registered **`SecretProvider`** (**`<provider>`** id) — grammar and call sites **stable** when optional
     provider JARs are added later. **`SecretProvider`** is a **normative SPI contract**; **concrete** providers (**`kv`**, **`aws-sm`**, …)
     are **optional modules**. **Verification:** unknown **`<provider>`** or failed secret lookup ⇒ **`VerificationIssue`**
     (**fail closed**); **no** silent env fallback for **`${provider://…}`**. **Field-scoped** substitution; **no** write-back;
     idempotent tokens; delimiter **BNF** in **`docs/design/data/`**. **Must not** block additional providers (see **STORY.md**).
   - **User-facing**: `docs/public/src/` — example **`SourceDescriptor`** snippets for each provider showing
     **both** behaviours side by side: **minimal** ambient config (omit `auth` or empty) vs **delegated**
     (credential keys populated; typically env placeholders)—**without requiring** `auth.type: token`.
4. CI note: emulator-based **`testIT`** jobs or tags documented so heavyweight tests do not regress default `./gradlew test` budget.
   State explicitly that provider **`testIT`** (**WI-262–264**) targets **blob-source + format interoperability** (**Parquet**
   primary, Avro streaming check) with **one** emulator-documented credential strategy each — **not** authentication-method
   coverage or proof of production **ambient** chains (**`STORY.md`** § Authentication **Tests** + § **Purpose of provider `testIT`**).
5. **Fixture contract** — **`testIT`** consumes **Skymill** artefacts at **`test/datasets/skymill/parquet/`** and **`test/datasets/skymill/avro/`** (**committed**). **`test/skymill.yaml`** defines **`write_parquet`** and **`write_avro`**; **`make regen-skymill`** (**`qsynth run … -a`**) refreshes both — **`test/datasets/skymill/README.md`**. **GAP-5** (**closed**) tracks this contract; optionally document in Gradle/release policy whether binaries stay committed vs regenerated exclusively in CI.
6. **Emulator matrix** — single design table: S3 (**MinIO** default; **LocalStack** optional),
   GCS (**fake-gcs-server**
   vs alternatives), Azure (**Azurite**): image versions, endpoints, **one recommended `testIT` credential recipe** per emulator
   (typically **delegated** / static / token material — **not** a matrix of prod ambient flows), how tests upload **`test/datasets/skymill/*`** objects. **Docker parity:** concrete **`docker run`** and **Compose** instructions for every emulator live in **`docs/design/data/object-storage-emulator-docker.md`** — `testIT` should use **Testcontainers** (or equivalent) with the **same** images/ports unless a test documents an override.

7. **Metadata / telemetry hygiene (GAP-3)** — **`FlowDescriptorMetadataSource`** facet payloads (**normative**: **`STORY.md`** § **Flow metadata facets — secret hygiene & inference control**):
   - **Document** canonical properties under **`mill.data.backend.metadata`**: **`enabled`** (**`true`** | **`false`**, default **`true`**) — when **`false`**, **no** **`FlowDescriptorMetadataSource`** bean (**first-line defence**): **no** facet inference work for flow sources — **performance** (**avoids snapshot / materialization / possible remote touch** for metadata) **and** **no** inferred payload surface (**secrets**). (**Aligns** with historical **`SPEC`** / **`WI-148`**, migrating from **`mill.data.backend.flow.metadata.enabled`** via **WI-265**.) **`redact`** — **`none`** | **`basic`** | **`safe`** (default **`basic`**) — second-line defence (**GAP-3** table in **`STORY.md`**).
   - **Optional per-descriptor overrides** on each flow **`SourceDescriptor`** YAML (**`metadata.redact`**, **`metadata.enabled`**): **`metadata.enabled=false`** suppresses inferred facets **for that source only** when **`mill.data.backend.metadata.enabled=true`** — **same** motivations (**cost** / **latency** / **policy**) as global off, scoped to one descriptor; when application **`metadata.enabled=false`** (global kill-switch), **ignore** descriptor-level **`metadata`** for registration (no bean). **`metadata.redact`** on the descriptor overrides application **`redact`** for that source. Document precedence in **`STORY.md`** § **GAP-3** and **`skymill-flow.yml`-style examples**.
   - Extend **`FlowBackendProperties`** (and/or dedicated **`mill.data.backend` metadata properties**) so **`mill.data.backend.metadata.*`** binds; **aliases** **`mill.data.backend.flow.metadata.enabled`** (**deprecated**) until removal.
   - **Implementation ownership** (**tracked here**, code in **`mill-data-backends`** + **`mill-data-autoconfigure`**): parse optional **`metadata`** on **`SourceDescriptor`**; wire **effective** **`redact` / `enabled`** into facet projection (**storage** facet payloads minimum; extend to **reader `params`** where strings may embed SAS/userinfo per design); **`none`** effective tier ⇒ startup **WARN** log (scope: **once** when **any** active source resolves to **`none`**, or per design).
   - **Remote hydration**: document that **`enabled=false`** removes metadata-driven materialization path; **`redact`** does **not** by itself disable **`SourceResolver`** work during snapshot build — note future optional lazy-hydration knob if product requests it.
   - **Tests**: unit coverage for projection / redaction matrices per **`redact`** value; autoconfigure slice asserting bean absent when **`enabled=false`** and **`none`** emits WARN (existing tests extended).

8. **Cold start — Spring Boot autoconfigure (normative patterns)** — For each **`cloud/*/…-autoconfigure`** module:
   - **`@AutoConfiguration`** with **`@ConditionalOnClass`** / **`@ConditionalOnBean`** guards so absent vendor JAR ⇒ **zero** beans.
   - Avoid **eager** **`S3Client`/`Storage`/`BlobServiceClient`** beans unless **`@Lazy`** **or** the bean is **only**
     a **`Supplier`/factory** that **materializes clients when `StorageFactory` runs** (preferred hand-off to **`cloud/*/…-blob`**).
   - **No `@PostConstruct` storage probes** (bucket **Head**, **list** prefix) unless behind **`mill.flow.storage.eager-verify`**
     (exact property name TBD) default **`false`**.
   - Prefer **constructor injection** / **immutable** configs; optional **`ObjectProvider<…>`** for optional collaboration beans.
   - Document **`spring.main.lazy-initialization`** interaction: global lazy init may **defer** unintended beans—**neutral**
     default **off** at Mill repo level; blob modules **still** lazy by design per bullet above.

9. **Cold start — Kubernetes / Helm (`apps/mill-service/src/main/helm` or successor)** — Update **operator docs** /
   **`values.yaml` comments**: **`startupProbe`** includes **generous `failureThreshold`/timeout** for JVM + Spring (**no**
     required S3 **list** unless new optional flag); **`readinessProbe`** HTTP path remains **Mill core**; optional future
     **`management.endpoint.health`** group for **`storage`** **off by default**. **`livenessProbe`** must **not** call
     external object stores. Cross-link **STORY.md** cold-start goals.

10. **Cold start — Flow descriptor verification policy** — Document **whether** **`MaterializedSource.verify()`**
    ( **`listBlobs` per source** ) runs at **Spring `ApplicationRunner` / `@PostConstruct` on flow beans** versus **lazy on
    first query**. **Default recommendation: lazy** except **explicit** dev/CI **`--verify-flow-sources-at-startup`** or
    config property—to keep **cold start fast** while allowing **staging** rigs to fail fast.

11. **Cold start — design & observability docs** (`docs/design/data/` subsection **Cold start**) — Consolidate vendor
    **first-touch matrices** (**IMDS**, **OAuth**, TLS, **`HeadObject`** / object metadata for Parquet length); defer
    SDK-specific timings to WI-262–264 KDoc while this section stays the **comparison** table. Cover **`BlobSource.close()`**
    lifecycle, interplay with **`SourceMaterializer`** **SPI caching**, recommendation for **horizontal pod autoscaling**
    (new JVM = new cold paths). Optionally standard Micrometer timers **`mill.storage.blob.first_touch`** (names TBD) —
    defer concrete metrics to implementation if costly.

12. **Dependencies & BOM** — Add cloud SDK versions to **`libs.versions.toml`** / Gradle BOM pattern used by **`data/`**;
    ensure **duplicate** conflicting AWS/Azure/GCP BOMs resolved; shade **not** expected—document classpath hygiene for
    autoconfigure-only consumers.

## Out of Scope

- Cloud-specific monitoring or billing tooling.

## Acceptance Criteria

- A developer can enable one provider module and run **`mill-service`** with a YAML source pointing at that backend;
  **`mill-service` starts successfully** without object-store connectivity when **verification-at-startup** remains
  disabled (lazy default)—**or** documented opt-in behaviour **fails closed** intentionally.
- Public docs explain required properties and reference Parquet/random-access dependency on **`openSeekableChannel`**.
- Docs state clearly that **every** cloud blob backend supports **ambient identity** when no credential keys
  are supplied, **delegated credentials** when credential keys are supplied, **inferred** without requiring
  `auth.type`; document per-provider credential fields and the rare **explicit override**.
- Emulator matrix and Skymill Parquet **+** Avro fixture policy are documented; developers can run **`testIT`**
  for each provider locally or in CI **without live cloud**. Docs align **`testIT`** scope with **`STORY.md`**: **blob + format**
  interoperability (**Parquet** primary); **one** emulator credential recipe per provider — **not** multi-method auth validation or production **ambient** proof.
- **`docs/design/data/`** and **`docs/public/src/`** document **`mill.data.backend.metadata.enabled`**, **`mill.data.backend.metadata.redact`**, **per-descriptor `metadata:` overrides**, and **legacy alias** **`mill.data.backend.flow.metadata.enabled`** per **`STORY.md`** § **Flow metadata facets — secret hygiene & inference control (GAP-3)** — including **`enabled`** as **performance + secrecy** first line, **`none`** danger + **WARN**, and remote-hydration caveat.
- Runtime **`FlowDescriptorMetadataSource`** honours **effective** **`redact`** / **source-level `enabled`** for facet payloads (**storage** minimum; reader **`params`** per design); **`none`** activates **WARN**; **`mill.data.backend.metadata.enabled=false`** omits bean (**FlowDescriptorMetadataSourceAutoConfiguration** updated for new prefix / alias).
- Automated tests cover redaction/projection for **`basic`** / **`safe`** and **`enabled=false`** registration; **`none`** logging behaviour tested where practical.
- Placeholder resolution contract is documented for **file-backed** and **future DB-backed** catalogs (same resolver,
  template persistence, no double substitution): **`${ID}`** and equivalent **`${env://ID}`** (env / **`Environment`** only);
  **`${provider://ref}`** dispatches to **`SecretProvider`**; **unknown provider** or failed lookup ⇒ **verification error**
  (**no** env fallback for provider-qualified tokens); **`SecretProvider` SPI** ids, registration file, stable facade extension;
  optional concrete provider modules (see **STORY.md** items **7–9**).
- **`docs/design/data/`** includes a dedicated **Cold start** section per **§11** (**first-touch matrices**, probes,
   lazy verify policy reference).
- Helm / operator snippets document **startup vs readiness vs liveness** separation from blob availability (**§9**).
- **BACKLOG.md** row **S-9** references the archived story **`docs/workitems/completed/20260514-cloud-blob-source/`**.

## Implementation Notes

**Branch:** `feat/cloud-blob-sources`

### Gradle / settings

- `settings.gradle.kts` — added `include(":cloud")` + all 6 leaf module includes
- `cloud/build.gradle.kts` — aggregate with dokka, `test`, `testITClasses`, `testIT` tasks
- `cloud/.gitlab-ci.yml` — CI job `cloud:build`
- Root `.gitlab-ci.yml` — added `cloud/.gitlab-ci.yml` include with `cloud/**/*` change detection
- `apps/mill-service/build.gradle.kts` — `runtimeOnly` on all 3 autoconfigure modules

### Dependencies added to `libs.versions.toml`

- `testcontainers = "2.0.5"` → `testcontainers-core`, `testcontainers-junit-jupiter`
- All cloud SDK versions (see WI-262–264)

### Placeholder resolution (`data/mill-data-source-core`)

| Class | Role |
|-------|------|
| `SecretProvider` | SPI interface — `id` + `resolve(reference)` |
| `DescriptorPlaceholderResolver` | Facade — parses `${ID}`, `${env://ID}`, `${provider://ref}` |
| `DescriptorPlaceholderResolverTest` | Unit tests for all resolution paths |

### Metadata hygiene (`data/mill-data-autoconfigure`)

| Class | Role |
|-------|------|
| `BackendMetadataProperties` (Java) | `@ConfigurationProperties(prefix = "mill.data.backend.metadata")` — `enabled`, `redact` |
| `FlowDescriptorMetadataSourceAutoConfiguration` | Updated `@ConditionalOnProperty` prefix + `@EnableConfigurationProperties` |

### Documentation

- `docs/design/data/cloud-blob-storage-auth-descriptors.md` — frozen auth field schemas
- `docs/design/data/object-storage-emulator-docker.md` — emulator matrix
- `docs/design/source/cloud-blob-flow-sources.md` — cloud blob architecture
- `docs/public/src/sources/cloud-object-storage.md` — user-facing docs
- `docs/public/src/sources/configuration.md` — updated with cloud storage types
- `docs/public/src/backends/flow.md` — updated for cloud sources

## Depends On

- WI-262, WI-263, WI-264 (implementation land); this WI may partly parallel **docs** stubs but must not claim completion early.

## Related

- [`STORY.md`](STORY.md) — **`Template descriptors`**, **`auth`**, streaming I/O, **GAP-3 flow metadata facets**.
- [**WI-262**](WI-262-aws-s3-blob-storage.md), [**WI-263**](WI-263-gcp-gcs-blob-storage.md), [**WI-264**](WI-264-azure-adls-blob-storage.md).
