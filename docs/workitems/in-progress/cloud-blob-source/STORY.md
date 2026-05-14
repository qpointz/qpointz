# cloud-blob-source

**Story slug / family:** `cloud-blob-source` — stories under the **`cloud-`** prefix extend or enable Mill in **cloud environments** (this one: declarative **`BlobSource`** backends).

## Goal

Extend declarative **`SourceDescriptor`** storage beyond local filesystem so flow/catalog sources can
list and read blobs from **Amazon S3**, **Google Cloud Storage (GCS)**, and **Azure Data Lake Storage
Gen2** (blob endpoint), reusing existing **`BlobSource`** / **`FormatHandler`** separation in
**`mill-data-source-core`** and leaving **`data/formats/`** unchanged unless a format-specific I/O gap
appears during validation.

Operator-facing **cold start** behaviour (startup without blocking on object stores, lazy clients, probe guidance) is specified in
**§ Cold start** and **WI-265**.

Implementation follows the established pattern: **`@JsonTypeName` storage subtypes**, Jackson
registration via **`DescriptorSubtypeProvider`**, and **`StorageFactory`** + **`META-INF/services`**
SPI (same as **`LocalStorageFactory`** today).

### Gradle module layout (normative — `cloud/` tree)

New **cloud vendor** code is rooted at **`cloud/`** (not under **`data/`**), grouped by provider:

| Path | Contents |
|------|----------|
| **`cloud/aws/`** | AWS-specific modules (blob, later **SecretProvider**, …) |
| **`cloud/azure/`** | Azure-specific modules |
| **`cloud/gcp/`** | Google Cloud–specific modules |

**Per provider**, use **two** module kinds:

1. **Spring-free (framework-native) module(s)** — **`BlobSource`**, **`StorageDescriptor`** subtype,
   **`StorageFactory`**, vendor SDK wiring, SPI resource files — **must not** depend on **`spring-boot-*`**
   or **`spring-context`**. Suitable for **`test`**, **`SourceMaterializer`**, and reuse outside Boot.
2. **Spring Boot autoconfigure module** — **`AutoConfiguration`**, conditional beans, optional registration of
   the same **`META-INF/services`** artefacts if classpath ordering requires it — **thin** layer depending on
   the framework-free module. **`apps/mill-service`** (and operators) typically add **`implementation`** on the
   **autoconfigure** (or **`starter`**) coordinates so blob support is one dependency.

Exact **`mill-*` artifact IDs** are chosen in implementation (**WI-265** documents names and **`settings.gradle.kts`**
paths, e.g. **`:cloud:aws:mill-cloud-aws-blob`** + **`:cloud:aws:mill-cloud-aws-autoconfigure`**).

**Out of scope for this story** but **same tree**: **`SecretProvider`** readers, **`DescriptorPlaceholderResolver`**
aggregation beans — placeholder modules under **`cloud/<vendor>/`** when implemented.

Legacy **`mill-data-source-core`** in **`data/`** stays unchanged; cloud blob modules **`api`/`implementation`**
into **`mill-data-source-core`** as needed.

### Cold start, startup semantics, and operability (normative + guidance)

**Definitions (for this story)**

- **JVM / process cold start** — New OS process: Spring **`ApplicationContext`** is building or has just finished;
  no warm connection pools or SDK DNS/TCP history yet.
- **First-touch / warm path** — First operation that touches a given backend for a given **`BlobSource`**:
  **`listBlobs`**, **`openInputStream`**, **`openSeekableChannel`** (Parquet **`getLength()`** counts as channel use),
  **`HeadObject` / metadata** for size, credential resolution hop (IMDS/OAuth), TLS handshake.

**Goals**

1. **Application must become “ready” for traffic** without **blocking** on **live object-store** connectivity
   **unless** the deployment **explicitly** opts into that behaviour (documented knob or dedicated health indicator).
   Default: missing cloud credentials / unreachable emulator **does not** prevent **`mill-service`** from passing a
   **generic** readiness model (see WI-265 Helm / actuator notes).
2. **Do not spike startup latency** by **eagerly** constructing vendor HTTP clients for **every** possible source at
   **`ApplicationContext`** refresh. Build **heavy** SDK clients **`StorageFactory#create`** → **`BlobSource`**
   construction time—or **defer further** inside **`BlobSource`** until **`listBlobs` / open** if a cheap descriptor
   parse is insufficient (implementation choice; document which layer pays first-touch cost).
3. **Placeholder resolution** (**`${…}`**) runs at the documented **materialize boundary**, **not** at Spring static
   init or classpath scanning. Persisted templates loading from file/DB stays **cheap** (parse-only) until resolve.
4. **SPI (`ServiceLoader`)** cost is **acceptable once per class loader** when **`SourceMaterializer`** first runs;
   avoid repeating full classpath scans inside **hot loops**.

**Kubernetes / probes (guidance)**

- **`startupProbe`**: generous enough for JVM + Spring + optional JIT; **does not need** successful S3/GCS/Azure
  **blob list** unless product mandate.
- **`readinessProbe`**: default HTTP checks Mill core routes; **optional** vendor-specific actuator indicator
   “can reach configured storage sample” documented as **heavy**—operators enable when they want **traffic cut** on
   storage outage.
- **`livenessProbe`**: avoid coupling to flaky external IAM or storage (**false kill** loops).

**First-touch latency (document per provider)**

- **Ambient IAM** (**AWS**, **GCP**, **Azure**) — **First** credential load may consult **metadata** services,
   **OAuth** token exchanges, or **credential file** discovery—**additive tens–hundreds ms** (or worse on degraded
   network). Subsequent calls reuse cached credentials per SDK semantics; document in **WI-262–264** KDoc and public
   ops guide.
- **Delegated keys in descriptor/env** — **No IMDS**; first request still pays **TCP/TLS/DNS cold** connection cost to the
   object endpoint (**MinIO** in-cluster often sub-ms DNS; **cross-region cloud** varies).
- **Parquet footer** — first **`getLength`** + **`openSeekableChannel`** ⇒ **HEAD** (+ **partial GET**) pattern;
   quantify in design as **acceptable “query-time” latency**, not **deployment-time**.

**Spring Boot autoconfigure layers (`cloud/*/…-autoconfigure`)**

- Register **`AutoConfiguration`** with **`@ConditionalOnClass`** so **classpath absence** ⇒ **zero** bean cost.
- Prefer **`@Lazy`** or **factory-pattern beans** where a bean would otherwise **instantiate SDK clients at context
   refresh**. **Thin** configurations: bean methods create **factory**/`StorageFactory` registration helpers, **not**
   long-lived global **`S3Client`** unless reused safely and **lazy**.
- **`@DependsOn`** / ordering only when integrating existing Mill beans (**flow** **`SourceCatalogProvider`**); avoid
   cycles.

**Operational toggles (to document in WI-265)**

| Concern | Suggested handling |
|---------|---------------------|
| **Fail fast vs lazy** | Descriptor **verification** (**`materialized.verify()`**) may touch storage → **explicit** operator choice when running verify at bootstrap vs **on-demand** (`docs/design`). |
| **Flow metadata facets** | **GAP-3** § **Flow metadata facets — secret hygiene & inference control**: application **`mill.data.backend.metadata.enabled`** / **`mill.data.backend.metadata.redact`**; optional per-descriptor **`metadata:`**. **`enabled`** kill-switches (**global** application **`false`**; **local** descriptor **`metadata.enabled: false`**) gate **facet inference cost** (**materialization**, **`listBlobs`**, remote I/O on first metadata touch) — **same knobs** serve **performance** as **first-line defence**, not only **`redact` / secrecy**. **Precedence for `enabled`:** application **`enabled=false`** is **absolute** (**no** inferred flow metadata anywhere; **cannot** be overridden by source YAML); application **`enabled=true`** allows **`metadata.enabled: false`** on a **`SourceDescriptor`** to **skip inference for that source only**. **`redact`** precedence per **`STORY.md`** body (descriptor overrides **`redact`** only). |
| **Connection reuse** | SDK clients typically **reuse** TCP; **`BlobSource.close()`** should release where applicable; **document** lifecycle for long-running service. |
| **Optional warm-up job** | Out of scope for **this** story; future **scheduled** “touch bucket” if operators need **pre-warming** before traffic. |

**Testing cold start (WI-262–265)**

- **Unit / fast tests** — no network; no Spring full context unless autoconfigure slice test.
- **`testIT`** — may spin **emulator** (Testcontainers) **before** first blob op; document **container start** is **not**
  Mill “cold start” but **test environment** cost. Optional **assertion**: time from **`SourceMaterializer`** creation to
  first successful **`listBlobs`** under **bounded** threshold in CI (flaky—**guidance** only unless stabilized).

### Flow metadata facets — secret hygiene & inference control (GAP-3)

Flow **schema / source-configuration** inferred facets are produced by **`FlowDescriptorMetadataSource`**
(**`MetadataOriginIds.FLOW`**). They **must not** leak **delegated credentials**, **SAS/query tokens**, **connection strings**,
or **signed URLs** with embedded secrets.

**Inference gate (`enabled`) vs payload hygiene (`redact`):** Turning **off** inferred metadata (**application** **`metadata.enabled`** or **descriptor** **`metadata.enabled: false`**) **also** avoids the **performance** tax of building facet snapshots (**`SourceResolver`**, **`SourceMaterializer`**, possible **`listBlobs`** / remote work — see **Remote hydration caveat** below). Operators may disable inference **primarily for latency or cold-path control**; **`redact`** stays the **second** line when inference remains **on**.

**Canonical application configuration**

Flow inferred metadata is tuned at the **Mill data backend** level (not nested under **`flow`** in property names):

- **`mill.data.backend.metadata.enabled`** — **`true`** | **`false`** (default **`true`**).
- **`mill.data.backend.metadata.redact`** — **`none`** | **`basic`** | **`safe`** (default **`basic`**).

**Shipment note:** **`FlowBackendProperties`** historically bound the registration flag under **`mill.data.backend.flow.metadata.enabled`**; **WI-265** relocates (**or aliases**) those bindings to **`mill.data.backend.metadata.*`** so facet behaviour is discoverable beside other backend concerns. Prefer **`mill.data.backend.metadata`** in new docs and examples.

**Descriptor-level overrides**

Each **`SourceDescriptor`** (flow YAML such as **`apps/mill-service/.../skymill-flow.yml`**) **may** include an optional top-level **`metadata`** block mirroring **subset semantics** of the application keys:

```yaml
name: skymill
metadata:
  redact: safe   # optional — overrides mill.data.backend.metadata.redact for this source only
storage:
  type: local
  # ...

# Omit `metadata:` entirely ⇒ inherit mill.data.backend.metadata.* application defaults.

# Optional — omit inferred facets for this descriptor only (only when mill.data.backend.metadata.enabled=true)
# metadata:
#   enabled: false
```

**Precedence**

1. **`mill.data.backend.metadata.enabled` (application — global)**  
   - **`false`** ⇒ **kill-switch**: **do not register** **`FlowDescriptorMetadataSource`**; **no** flow inferred facet rows for **any** source (**query** / Calcite schemas **unchanged**) — avoids **every** facet snapshot / inference path Mill would otherwise run for Data Model projections. Use when operators want **minimal metadata cost** or **no** flow facet surface. **Source-level `metadata.enabled` is ignored** — it **cannot** re-enable inferred metadata.  
   - **`true`** ⇒ inferred metadata **may** run; **`metadata.enabled` on individual `SourceDescriptor`s** applies next.

2. **`metadata.enabled` on a `SourceDescriptor` (optional — per source)** — meaningful **only when** **`mill.data.backend.metadata.enabled=true`**. **`false`** ⇒ **suppress inferred facets for that source only** (other sources unaffected) — trims **per-source** inference / materialization work for **heavy** catalogs (e.g. object stores) while leaving lighter sources on. Omit or **`true`** ⇒ inferred facets **follow** global policy for that descriptor.

3. **`metadata.redact`** — omitted on the descriptor ⇒ use **`mill.data.backend.metadata.redact`**; if set on the descriptor ⇒ overrides **application `redact` for that source’s facet payloads**.

4. **Effective `redact` and cache keys:** facet snapshot caching **must** incorporate **effective** **`redact`** per source (application **`redact`** merged with **`metadata.redact`** on that descriptor **when** that source participates in inferred metadata per bullets **1–2**).

**First line of defence — disable facet inference**

- **`mill.data.backend.metadata.enabled`** — **`true`** | **`false`** (default **`true`**). When **`false`**, **`FlowDescriptorMetadataSource`** is **not** registered — **no** flow inferred facets **globally**; **no** metadata-driven inference path for those facets — **source YAML cannot override**. This is the **first-line** control for **both** **secret surface** (nothing to leak if nothing is inferred) and **runtime cost** (skip snapshot build / remote touch for flow metadata). When **`true`**, **`FlowDescriptorMetadataSource`** is registered; flow **query** / Calcite schemas **always** behave as today.
- **`metadata.enabled` on the descriptor** — **only when** application **`mill.data.backend.metadata.enabled=true`**: **`false`** ⇒ **that** source emits **no** inferred facet contributions (**local** opt-out for cost or policy); **`true`** or omitted ⇒ participates like other sources.

**Second line of defence — facet payload redaction (`metadata.redact`)**

- **`mill.data.backend.metadata.redact`** — **`none`** | **`basic`** | **`safe`** (default **`basic`**). **Naming rationale:** **`redact`** names the behaviour (how much credential / URL hygiene is applied); **`basic`** is the **default hygienic** tier (operators who want a synonym in runbooks may read this as **standard** emission).
- Optional **`metadata.redact`** on the **`SourceDescriptor`** overrides the application default **for facets emitted from that descriptor only**.

| **`redact`** | Behaviour |
|---------|-----------|
| **`none`** | Emit storage-related facet payloads **without** redaction (mirror descriptor / resolved maps **verbatim**). **Dangerous** when **`storage.auth`**, SAS tokens, or connection strings exist. Activation **must** log **WARN** at startup (**once**) that **`none`** facet emission is unsafe outside tightly controlled diagnostics. |
| **`basic`** | Emit **non-sensitive** identity fields **as-is** (bucket / container / filesystem name, prefix, region where not secret). **Strip or substitute** **`storage.auth`** and other credential-bearing keys; **never** echo strings that embed secrets inside URLs — build **safe display URLs** from **non-sensitive** properties only (vendor SDK URL builders / canonical forms — detailed per cloud in **`docs/design/data/`**). Non-secret hints such as **`authConfigured`** are allowed. |
| **`safe`** | Apply **`basic`** hygiene, then retain **only** an **allow-listed** minimal parameter set (bucket/container/filesystem, prefix, **`rootPath`** for local where policy allows). **Exclude** endpoints and connection strings unless classified non-sensitive in design — **strictest** display-oriented surface for metadata facets. |

**Remote hydration caveat**

- Building facet snapshots **materializes** **`SourceDescriptor`** (**`SourceResolver`** / **`SourceMaterializer`**) and may call **`listBlobs`** / inference — **first** metadata access after restart can touch **object stores** regardless of the **effective** redaction tier (**`metadata.redact`**: application default merged with any per-descriptor override). **`mill.data.backend.metadata.enabled=false`** removes this metadata path **entirely** (**performance** as well as secrecy). Per-descriptor **`metadata.enabled: false`** removes it **for that source**. **`redact`** governs **payload leakage** when inference **runs**, not **whether** remote calls occur **during** snapshot build (unless a future knob adds lazy hydration).

### Streaming I/O and memory (normative)

Cloud **`BlobSource`** implementations **must not** load **whole blobs into heap** on normal read paths.

- **`openInputStream`** must be **streaming** (consumer-driven reads; no buffering of the entire object
  unless a format requires it—and then only inside the format layer with explicit streaming APIs).
- **`openSeekableChannel`** must implement random access via **range reads** plus a **fixed, bounded**
  in-memory window (read-ahead / cache cap), never `readAllBytes()` or an unbounded growable buffer of the
  full object size.

An **optional**, explicitly documented spill-to-disk (or mmap) policy for gigantic objects remains a possible
later enhancement; **default** behaviour in **WI-262–264** stays **bounded RAM**.

### Authentication (normative for every cloud blob backend)

#### `storage.auth` is provider-specific (**not** one shared schema)

There is **no** single **`auth`** shape for **all** clouds. **`storage.auth`** under **`storage`** is **determined by
`storage.type`**: AWS, GCP, and Azure each accept **different** credential material (IAM-style **access keys** vs OAuth
**access tokens** vs **service account JSON** vs **storage connection strings** / **account keys**). Keys that are legal
under **`storage.type: s3`** **must not** be assumed valid under **`storage.type: gcs`** (and vice versa) — implementations
reject **unknown keys** per type (**see** **[`cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)**).

**Cross-provider convention (only shared key):** optional **`preferAmbientCredentials`** (boolean) appears on **every** blob
storage **`auth`** object with **identical** meaning — force ambient credential resolution (see **Optional explicit override** later in this section).
All **other** `auth.*` entries are **vendor-specific delegated bundles**.

**Normative detail** (bundles, exclusivity, verifier patterns): **[GAP-4 · cloud-blob-storage-auth-descriptors.md](../../../design/data/cloud-blob-storage-auth-descriptors.md)**.

Every object-store **`StorageDescriptor`** in this story (**S3**, **GCS**, **ADLS**/Azure blob REST) **must** support **both**
**(a) ambient / workload-style credentials** and **(b) transferable (injected) delegated credentials**. **`auth.mode` /
`auth.type`** are **optional** globally — delegation is inferred from **whether any provider-specific delegated key is
non-blank** (rules per provider doc above).

##### **`storage.type: s3`** (S3-compatible — frozen)

| Role | **`storage.auth`** fields (camelCase) |
|------|---------------------------------------|
| **Delegated — static IAM / MinIO-style keys** | **`accessKeyId`**, **`secretAccessKey`**, optional **`sessionToken`** (STS / session credentials) |
| **Ambient** | Omit **`auth`**, or only blank fields — **AWS SDK default credential provider chain** (IRSA, instance profile, env, profile, …) |

YAML sketch (delegated, env placeholders):

```yaml
storage:
  type: s3
  bucket: my-bucket
  region: us-east-1
  auth:
    accessKeyId: ${AWS_ACCESS_KEY_ID}
    secretAccessKey: ${AWS_SECRET_ACCESS_KEY}
    # optional: sessionToken: ${AWS_SESSION_TOKEN}
```

##### **`storage.type: gcs`** (Google Cloud Storage — frozen)

| Role | **`storage.auth`** fields (camelCase) |
|------|---------------------------------------|
| **Delegated — mutually exclusive bundles (exactly one non-blank)** | **`accessToken`** (OAuth bearer) **or** **`serviceAccountJson`** (full SA JSON inline) **or** **`serviceAccountJsonPath`** (filesystem path to SA JSON file) |
| **Ambient** | Omit **`auth`**, or only blank fields — **Application Default Credentials** (Workload Identity, metadata, `GOOGLE_APPLICATION_CREDENTIALS`, …) |

YAML sketch (delegated token):

```yaml
storage:
  type: gcs
  bucket: my-bucket
  auth:
    accessToken: ${GCS_ACCESS_TOKEN}
```

(Service account bundles and validation rules — design doc.)

##### **`storage.type: adls`** (Azure Data Lake Storage / Blob — frozen)

| Role | **`storage.auth`** fields (camelCase) |
|------|---------------------------------------|
| **Delegated — mutually exclusive bundles** | **`connectionString`** **or** the pair **`accountName` + `accountKey`** (**shared key**); partial pairs **fail** **`Verifiable`** |
| **Ambient** | Omit **`auth`**, or only blank fields — **`DefaultAzureCredential`** (managed identity, CLI, env, …) |

YAML sketch (**Azurite** / dev connection string):

```yaml
storage:
  type: adls
  filesystem: raw
  auth:
    connectionString: ${AZURITE_STORAGE_CONNECTION_STRING}
```

**Inference (default — keeps YAML terse):**

- **Within a given **`storage.type`****: omit **`auth`**, or provide **`auth`** with **no** non-blank **delegated-bundle** keys
  for that provider → use **ambient** identity (**AWS default chain**, **ADC**, **`DefaultAzureCredential`**, respectively).
- If **`auth`** includes **any non-blank** key that participates in that provider's **delegated** bundle (**field sets** in **[`cloud-blob-storage-auth-descriptors.md`](../../../design/data/cloud-blob-storage-auth-descriptors.md)**)
  → use the **delegated** SDK constructors for **that storage type**, not the ambient chain, unless **`preferAmbientCredentials`** is **`true`**.

**Verification:** **`Verifiable`** on each **`StorageDescriptor`** **must** reject **partial / inconsistent**
credential bundles (**unknown keys**, **half-filled** delegated sets, **two** GCP bundles at once, **both** Azure
`connectionString` **and** shared-key fields populated, …) with a clear **`VerificationIssue`** — patterns in the design doc.

**Optional explicit override** (escape hatch only — when inference would be ambiguous or undesirable): **`storage.auth.preferAmbientCredentials: true`** (**boolean**, **opt-in**, **same key on all three blob types**) **forces ambient** despite non-blank delegated keys; **`Verifiable`** **WARN** when both apply. Older sketch **`auth.useAmbientCredentials`** is **not** used.

Committed YAML with raw secrets stays **explicitly discouraged** in user docs (**prefer env placeholders** /
secret mounts resolved before or during config load).

**Tests (split by concern):**

- **Product behaviour** — Each provider **must still implement** inference for **both** **(a) ambient / workload-style**
  and **(b) delegated** credential construction (see bullets above), with **`Verifiable`** rejecting partial bundles. **Unit**
  tests (and small focused tests **without** claiming live cloud identity) **should** exercise **descriptor / factory**
  branching for **both** paths so regressions in **which** SDK credential path is chosen are caught in CI.
- **Provider `testIT` (WI-262–264)** — Exists primarily to prove **blob-source interoperability**: **`BlobSource`**
  **list / stream / seek** (especially **Parquet** via **`openSeekableChannel`** and ranged reads), wiring through
  **`SourceMaterializer`**, and consumption by **`mill-data-format-parquet`** / **`mill-data-format-avro`**. **One**
  **emulator-stable** auth configuration is **sufficient** for these tests (typically **delegated** static keys or
  emulator token material the vendor documents), **not** a matrix of authentication methods. **`testIT`** **does not**
  validate production **ambient** chains (IMDS, workload identity, **`DefaultAzureCredential`** in cloud) — that remains
  **operator / optional live-environment** validation or a **separate** test story if the team wants it.

### Template descriptors and variable resolution (DB- and file–compatible)

Flow sources will likely move from **static files** toward **persistence + UI** (manage **`SourceDescriptor`** at
runtime). Variable substitution **must** stay compatible with that direction.

**Normative model**

1. **Persisted form (canonical)** — Store and edit the **template**: string fields may contain **placeholders**
   using the grammar below. **Repositories, ConfigMaps, Helm, and future DB rows** all hold this form. **Never**
   persist resolved secret values as the primary record (no silent write-back of substituted strings into the DB).

2. **Resolved form (ephemeral)** — Apply substitution **once** at a **defined boundary** immediately before
   verification / **`SourceMaterializer`** (or inside a dedicated **`SourceCatalogProvider`** adapter). Resolved
   objects exist **only in memory** for that operation (unless an explicit audited “effective config” preview is added later).

3. **Placeholder grammar (normative sketch; exact BNF in `docs/design/data/` / WI-265)** — The resolver **distinguishes**:

   - **`${ID}`** (simple identifier) — resolve through the **default ambient channel**: **`System.getenv(ID)`**
     with fallback to Spring **`Environment`** properties keyed by **`ID`** (precedence **fixed in implementation**).
     No secret-store I/O.
   - **`${env://ID}`** — **Semantically equivalent** to **`${ID}`** (explicit env/property channel only). Same lookup
     rules as the bullet above; **no** secret-store I/O.
   - **`${<provider>://<reference>}`** — **`reference`** is opaque to core (interpreted only by the provider). Dispatch to a
     **`SecretProvider`** registered under **`<provider>`** (e.g. **`${kv://my-secret}`** when id **`kv`** is registered).
     **Only** this form triggers **secret-backend** clients. Delimiter rules and token shapes are **frozen** at the facade
     boundary so future providers **do not** change grammar or materialize-time call sites (**WI-265** BNF).

4. **`SecretProvider` SPI (normative contract; implementations optional)** — Vault/cloud integrations plug in via
   **`ServiceLoader`** (and/or Spring beans that register the same contract) under a **stable string id** (`kv`,
   `aws-sm`, …). Core Mill **must not** embed vendor SDKs in the generic resolver: it **only** discovers providers and
   **delegates** **`${provider://…}`**. **Concrete provider JARs** may ship **later** than the facade; grammar and dispatch
   stay stable when they appear. **Caching / TTL** for live fetches lives inside each provider or an injected policy.

5. **`DescriptorPlaceholderResolver` (facade)** — Single **choke point** that **parses** each candidate string and dispatches
   **`${ID}`** / **`${env://ID}`** vs **`${provider://reference}`**. **Registration-based** extension: adding a provider
   **must not** require changing **`StorageDescriptor`** types or **facade method signatures** at resolve → verify →
   materialize.

6. **Must not block cloud secret backends (normative)** — **No** implementation path that **cannot** attach **`SecretProvider`**
   implementations (item 4); **avoid** globals; **inject** the facade on resolve → verify → materialize (composite chains,
   Spring bridges, etc.).

7. **Implementation scope for `cloud-blob-source` (closes specification ambiguity)** — Delivery splits **contract + env
    resolution** from **optional provider modules**:

    - **In scope with WI-265 / materialization boundary**: freeze **BNF** in **`docs/design/data/`**; implement env-backed
      resolution for **`${ID}`** and **`${env://ID}`**; **parse** **`${provider://…}`** and dispatch via **`SecretProvider`**
      registry; ship **`SecretProvider`** **interface** + registration discovery **documented in code** so new modules plug in
      **without** resolver signature changes.
    - **Explicitly optional unless bundled**: individual **`SecretProvider`** implementations (**`kv`**, **`aws-sm`**, …).

8. **Verification when a provider-qualified placeholder cannot resolve (normative)** — For **`${<provider>://<reference>}`**:
    if **no** **`SecretProvider`** is registered for **`<provider>`**, **or** the provider fails lookup — surface a
    **`VerificationIssue`** (**fail closed**). **Do not** silently fall back to **`Environment`** / **`getenv`** for the
    **`reference`** string (provider-qualified tokens are **not** env aliases).

9. **No env fallback across forms** — **`${ID}`** / **`${env://ID}`** never triggers secret backends; **`${provider://…}`**
    never impersonates env lookup on failure (item **8**).

10. **Field-scoped substitution (preferred)** — After Jackson parses **`SourceDescriptor`**, traverse **chosen string fields**
   (at minimum **credential-bearing** paths under **`storage.auth`**) and run the resolver **per leaf**. Prefer this over
   **whole-file regex** substitution on YAML text (avoids corrupting structure and matches **DB-stored JSON** round-trips).

11. **No double resolution** — Resolver should be **idempotent** for already-literal values, or **only** replace
    well-delimited tokens, so re-loading from DB and resolving again does not mutate non-placeholder data.

12. **Metadata & APIs** — List/get for the **UI** should return the **template** where possible; any **“effective”** view
    must **redact** secrets. Flow inferred facets follow **§ Flow metadata facets — secret hygiene & inference control (GAP-3)** and **WI-265** metadata hygiene delivery.

**WI-265** documents the resolver contract, **grammar**, **`SecretProvider` SPI** package name and registration file,
and boundary in **`docs/design/data/`** so a later **“flow sources in DB”** story reuses the same pipeline (load template → resolve → verify → materialize). Concrete vault modules remain **optional** (Template items **7–9**).

Placeholder § scope recap: grammar + **`${ID}`** / **`${env://ID}`** env resolution + **`${provider://…}`** dispatch contract ship with **WI-265**; optional **`SecretProvider`** JARs add backends without changing grammar.

### Integration testing (binary Skymill + emulators)

**All** cloud **`BlobSource`** integration tests (**WI-262–264**) MUST exercise real format handlers against
**Skymill-generated** binary artefacts under **`test/datasets/skymill/`**:

- **Parquet** — **`test/datasets/skymill/parquet/*.parquet`**, produced by the existing **`write_parquet`**
  experiment in **`test/skymill.yaml`** and **`make regen-skymill`** (see **`test/datasets/skymill/README.md`**).
- **Avro** — **`test/datasets/skymill/avro/*.avro`**, produced by the **`write_avro`** experiment in **`test/skymill.yaml`**
  (same logical tables as CSV/Parquet). Regenerate with **`make regen-skymill`** (**`qsynth run … -a`** runs all experiments,
  including Avro) — see **`test/datasets/skymill/README.md`**.

**Why both:** Avro stresses **forward-only / streaming reads** (**`openInputStream`**, row iteration);
Parquet stresses **random-access** (**`openSeekableChannel`** via **`BlobInputFile`**). **Parquet is the primary
proof** of seekable blob behaviour; Avro confirms the **streaming** path still works end-to-end.

**Purpose of provider `testIT` (normative)** — **`testIT`** in **WI-262–264** proves **blob-source interoperability**
with **`SourceMaterializer`** and format handlers over emulators (**list**, **stream**, **seek**). It is **not** intended to
validate **every** authentication method or **production ambient** identity (IMDS, cloud workload tokens, full
**`DefaultAzureCredential`** chains). Use **one** stable **emulator-documented** credential strategy per provider for **`testIT`**
(often **delegated** static keys or emulator tokens); rely on **unit tests** + **`Verifiable`** for **ambient vs delegated**
**inference** and SDK branch selection. Optional **live-environment** auth validation is **out of scope** for these **`testIT`** suites unless the team adds it explicitly elsewhere.

Tests **provision object storage via emulators** where available (**MinIO** for S3-compatible integration
tests—lightweight and S3-API faithful; **LocalStack** remains an optional alternative; **fake-gcs-server**
or GCS emulator for GCS; **Azurite** for Azure), seed buckets/files from the paths above, then run **`SourceMaterializer`** + existing readers (**`mill-data-format-parquet`**,
**`mill-data-format-avro`**). Prefer **Testcontainers** or documented **Docker Compose** so CI does not hit
live clouds. **Concrete `docker run` + Compose** for **MinIO**, **LocalStack**, **fake-gcs-server**, and **Azurite** are in **`docs/design/data/object-storage-emulator-docker.md`**. **`testIT`** implementations should **spin containers from those images** (typically **Testcontainers**) with the **same** ports/credentials unless documented otherwise. **WI-265** completes the emulator matrix table, CI tags, and Gradle/asset copy conventions.

### Responsibility split (work items)

| WI | Primary delivery | **`STORY.md` sections** anchored here |
|----|------------------|--------------------------------------|
| [**WI-262**](WI-262-aws-s3-blob-storage.md) | **`cloud/aws/*`** S3 **`BlobSource`**, descriptor, **`testIT`** (Skymill + MinIO), **AWS-specific** cold-start KDoc | § Gradle layout (aws row), Streaming I/O, Auth, Integration testing, Cold start (**first-touch** via AWS SDK) |
| [**WI-263**](WI-263-gcp-gcs-blob-storage.md) | **`cloud/gcp/*`** GCS analogue + fake-gcs / emulator **`testIT`** | Same + GCS credential / transport notes |
| [**WI-264**](WI-264-azure-adls-blob-storage.md) | **`cloud/azure/*`** ADLS-compatible blob + Azurite **`testIT`** | Same + **`DefaultAzureCredential`** chain nuance |
| [**WI-265**](WI-265-cloud-storage-wiring-docs.md) | Root **`settings.gradle`**, **`mill-service`** deps, **`docs/design/data/`**, **`docs/public/`**, Helm/probes/BOM/**Cold start** runbook aggregates (items **§8–§12**), placeholder BNF (**Template** §), **GAP-3 metadata hygiene** (**enabled** + **`redact`**) | All normative prose; resolves **canonical property names** (e.g. **`mill.flow.storage.eager-verify`**) |

Cross-story items (**`${…}` placeholders**, **`SecretProvider`** contract, DB-backed catalog) are specified in **`STORY.md`** § **Template descriptors**. **WI-265** delivers the **facade + env resolution + grammar + SPI contract** per items **7–9**; **optional** provider JARs ship when added to the build, without changing grammar or facade call sites. **WI-265** also owns documenting and implementing **GAP-3** facet emission controls (**§ Flow metadata facets — secret hygiene**).

## Current State (implementation summary)

**Branch:** `feat/cloud-blob-sources` (from `origin/dev`)  
**Story status:** all 4 WIs implemented and committed; story is **in-progress** (not yet closed/archived)

### What was built

Six new Gradle modules under `cloud/` (two per provider: Spring-free blob + Boot autoconfigure):

```
cloud/
├── build.gradle.kts          # aggregate (dokka, test, testIT)
├── .gitlab-ci.yml             # CI job cloud:build
├── aws/
│   ├── mill-cloud-aws-blob/
│   └── mill-cloud-aws-autoconfigure/
├── gcp/
│   ├── mill-cloud-gcp-blob/
│   └── mill-cloud-gcp-autoconfigure/
└── azure/
    ├── mill-cloud-azure-blob/
    └── mill-cloud-azure-autoconfigure/
```

Each blob module contains: `*StorageDescriptor` (`@JsonTypeName`), `*AuthDescriptor`, `*BlobSource`, `*BlobPath`,
`*SeekableByteChannel`, `*StorageFactory` (SPI), `*DescriptorSubtypeProvider` (SPI), unit tests, and a single
consolidated `*FlowBackendIT` integration test covering the full pipeline (emulator → upload Skymill → blob
list/stream/seek → Flow backend schema listing + SQL queries).

### Cross-cutting deliverables (WI-265)

- **Placeholder resolution:** `DescriptorPlaceholderResolver` + `SecretProvider` SPI in `data/mill-data-source-core`
- **Metadata hygiene:** `BackendMetadataProperties` (`mill.data.backend.metadata.enabled/redact`) in `data/mill-data-autoconfigure`
- **Wiring:** `settings.gradle.kts` includes, `apps/mill-service` runtimeOnly deps, root `.gitlab-ci.yml` cloud include
- **Docs:** design docs under `docs/design/data/` and `docs/design/source/`, public docs under `docs/public/src/sources/`

### Integration tests (all green)

| Provider | Test class | Emulator | Tests |
|----------|-----------|----------|-------|
| AWS S3 | `S3FlowBackendIT` | MinIO (`minio/minio:latest`) | 9/9 |
| GCS | `GcsFlowBackendIT` | fake-gcs-server (`fsouza/fake-gcs-server:latest`) | 9/9 |
| Azure ADLS | `AdlsFlowBackendIT` | Azurite (`mcr.microsoft.com/azure-storage/azurite`) | 9/9 |

Each IT covers: blob listing (parquet + avro), streaming read (PAR1 magic), seekable channel, schema listing,
table listing, SELECT with LIMIT, SELECT with WHERE filter — all against real Skymill datasets uploaded to emulated storage.

### Remaining for story closure

Per `docs/workitems/RULES.md`:

1. Squash/regroup commits for MR-ready history (~10 commits above merge base)
2. Update `docs/workitems/MILESTONE.md` with completed WIs
3. Update `docs/workitems/BACKLOG.md` — set related rows to `done`
4. Move story folder to `docs/workitems/completed/YYYYMMDD-cloud-blob-source/`
5. Open MR targeting `dev`

## Work Items

- [x] **[WI-262](WI-262-aws-s3-blob-storage.md)** — AWS S3 `StorageDescriptor`, `BlobSource`, MinIO/Skymill tests, **AWS cold-start KDoc**
- [x] **[WI-263](WI-263-gcp-gcs-blob-storage.md)** — GCS **`BlobSource`**, emulator/Skymill tests, **GCP cold-start KDoc**
- [x] **[WI-264](WI-264-azure-adls-blob-storage.md)** — Azure ADLS **`BlobSource`**, Azurite/Skymill tests, **Azure cold-start KDoc**
- [x] **[WI-265](WI-265-cloud-storage-wiring-docs.md)** — Service wiring, emulator matrix, design/public docs, **cold start & operability**
  (probes, lazy verification, BOM), **GAP-3 flow metadata hygiene** (**enabled** + **`redact`** + tests)
- [ ] **[WI-271](WI-271-unified-backend-metadata-controls.md)** — Unified `mill.data.backend.metadata` enabled/redact controls across all backend `MetadataSource` beans
