# Cloud blob storage — frozen `storage.auth` field sets (GAP-4)

Normative **`StorageDescriptor`** authentication shapes for the **cloud-blob-source** story (**AWS S3**, **GCS**, **Azure Blob / ADLS Gen2-compatible** paths). **`storage.auth`** is **not** a single cross-vendor map: **allowed keys depend on **`storage.type`**** (each provider may use **tokens**, **access keys**, **JSON**, **connection strings**, etc.). Implements **GAP-4** from [`docs/workitems/planned/cloud-blob-source/GAPS.md`](../../workitems/planned/cloud-blob-source/GAPS.md): provider WIs (**WI-262–264**) **consume** this document rather than defining divergent **`auth`** maps.

**Story-level summary and per-provider proposals:** [`STORY.md`](../../workitems/planned/cloud-blob-source/STORY.md) (**Authentication** section).

**Sibling references:**

- [`object-storage-emulator-docker.md`](object-storage-emulator-docker.md) — emulator credentials for **`testIT`**.
- [`../source/cloud-blob-flow-sources.md`](../source/cloud-blob-flow-sources.md) — how cloud **`storage`** fits **Flow** (**`SourceDescriptor`**, verifier phase **`STORAGE`**).

Field names below are **`storage.auth.*`** subtree keys in **`SourceDescriptor` YAML**. Jackson binding uses **camelCase** property names in Kotlin data classes (same spelling in YAML/JSON examples).

---

## Shared rules (all blob storage types in this doc)

### Ambient vs delegated (inference)

- **`storage.auth` omitted**, or **`auth` present but no credential-bearing key is non-blank** after trimming → implementation uses **ambient** identity for that cloud (**AWS SDK default chain**, **Google ADC**, **`DefaultAzureCredential`**, respectively).
- **Any non-blank value** under a **delegated-bundle** defined for that **`storage.type`** selects the **delegated** SDK constructors for that descriptor (see bundles per cloud below).
- **Unknown keys** under **`storage.auth`** (not documented for that **`storage.type`**) ⇒ **`Verifiable`** **ERROR**, phase **`STORAGE`**, message must list **allowed** keys (**fail closed**) so typos never silently drop credentials.

### Optional escape hatch (`preferAmbientCredentials`)

Optional boolean on **`storage.auth`** for **every** storage type defined here:

| Key | Values | Meaning |
|-----|--------|--------|
| **`preferAmbientCredentials`** | **`true`** / **`false`** (omit ⇒ **`false`**) | When **`true`**, the implementation **forces ambient** credential resolution **even if** delegated keys are also non-blank — **operator escape hatch** when inference would pick delegated material incorrectly (**STORY.md** § **Authentication** **Optional explicit override**). **Dangerous**: can mask misconfiguration if operators forget delegated keys exist. **`Verifiable`** should **WARN** when **`true`** **and** any delegated-bundle key is non-blank. |

If **`preferAmbientCredentials: false`** (default), normal **presence-based** delegated vs ambient inference applies.

### Blank vs missing

Treat **YAML `null`**, **empty string**, and **whitespace-only** strings as **blank** (“not present”). **Partial bundles** occur when **one** credential field of a **required set** is non-blank **and** another **required** sibling is blank ⇒ **ERROR** (below).

### `VerificationIssue` message style

Failures must be human-actionable:

- **Partial bundle:** **`Storage auth incomplete for AWS S3: 'accessKeyId' provided but `secretAccessKey` is blank (provide both access key fields or omit entirely for ambient credentials)`** — pattern for all clouds.
- **Unknown key:** **`Unknown storage.auth keys for type 's3': [foobar]. Allowed keys: accessKeyId, secretAccessKey, sessionToken, preferAmbientCredentials.`**
- **Mutually exclusive bundles:** **`Storage auth specifies more than one GCP delegated bundle (example: accessToken and serviceAccountJson); provide exactly one GCP delegated credential bundle or omit entirely for ambient credentials.`**

---

## AWS S3 — `storage.type: s3`

### Delegated bundles (presence triggers non-ambient SDK path)

Exactly **one** of the following delegated modes may be populated (non-blank) at a time. **Mixing modes** ⇒ **ERROR**.

| Bundle | Keys (all camelCase under `storage.auth`) | Required | Maps to emulator / prod |
|--------|-----------------------------|----------|-------------------------|
| **Static access keys** | **`accessKeyId`**, **`secretAccessKey`**, optional **`sessionToken`** | **`accessKeyId` + `secretAccessKey`** non-blank when this bundle is used | **MinIO** dev keys (**WI-262**); **IAM user** keys in prod. **`sessionToken`** optional **temporary** STS session creds |

**Forbidden partials for static bundle:**

| Non-blank | Blank forbidden sibling | Severity |
|-----------|--------------------------|----------|
| `accessKeyId` | `secretAccessKey` | **ERROR** |
| `secretAccessKey` | `accessKeyId` | **ERROR** |
| **`sessionToken` only** (no access key pair) | access key pair | **ERROR** |

(**Future extension**, not blocking WI-262: STS **assume-role** profile — add **`assumeRoleArn`**, **`assumeRoleExternalId`**, **`webIdentityTokenPath`** bundle in a separate design revision.)

### YAML examples

**Ambient:**

```yaml
storage:
  type: s3
  bucket: my-bucket
  region: us-east-1
  # auth omitted entirely
```

**Delegated (MinIO / static keys via env placeholders):**

```yaml
storage:
  type: s3
  bucket: test-bucket
  region: us-east-1
  endpoint: http://127.0.0.1:9000
  pathStyleAccess: true
  auth:
    accessKeyId: ${AWS_ACCESS_KEY_ID}
    secretAccessKey: ${AWS_SECRET_ACCESS_KEY}
    # sessionToken: ${AWS_SESSION_TOKEN}   # optional
```

---

## Google Cloud Storage — `storage.type: gcs`

### Delegated bundles

Exactly **one** delegated mode non-blank. **Mixing modes** ⇒ **ERROR**.

| Bundle | Keys under `storage.auth` | Requirements | Maps to emulator / prod |
|--------|--------------------------|----------------|--------------------------|
| **OAuth2 access token** | **`accessToken`** | Non-blank string | Emulator / REST-style bearer (**WI-263** **`testIT`** where a static token suffices) |
| **Service account JSON** | **`serviceAccountJson`** | Non-blank string containing parseable JSON object with **`client_email`**, **`private_key`** (normalized field names inside file per Google **`service-account` schema**) | Uploaded SA key material |
| **Service account file path** | **`serviceAccountJsonPath`** | Non-blank filesystem path string | Mill reads JSON at resolve/verify; **must not** echo file contents in logs |

**Naming decision:** Prefer **`serviceAccountJson`** (**inline**) and **`serviceAccountJsonPath`** (**path**) so keys do not collide with AWS **`accessToken`** semantics at another **`storage.type`**. **`accessToken`**, **`serviceAccountJson`**, **`serviceAccountJsonPath`** ⇒ **pairwise mutually exclusive**: two or more non-blank ⇒ **ERROR**.

**Partial / invalid:**

- **`serviceAccountJson`** non-blank but JSON parse fails / missing **`client_email`** / **`private_key`** ⇒ **ERROR**.
- **`accessToken`** with leading/trailing whitespace only ⇒ treated as omitted (ambient).

### YAML examples

**Ambient:**

```yaml
storage:
  type: gcs
  bucket: my-bucket
```

**Delegated (inline SA JSON — illustrative; prefer env-mount or secret resolver in prod):**

```yaml
storage:
  type: gcs
  bucket: my-bucket
  auth:
    serviceAccountJson: |
      ${GCS_SA_JSON_BODY}
```

**Delegated (OAuth token):**

```yaml
storage:
  type: gcs
  bucket: my-bucket
  auth:
    accessToken: ${GCS_ACCESS_TOKEN}
```

---

## Azure Blob / ADLS Gen2-compatible — `storage.type: adls`

### Delegated bundles

Exactly **one** of:

| Bundle | Keys under `storage.auth` | Requirements | Emulator / prod |
|--------|--------------------------|----------------|-----------------|
| **Connection string** | **`connectionString`** | Single non-blank string | **Azurite** / connection strings (**WI-264**) |
| **Shared key** | **`accountName`**, **`accountKey`** | Both non-blank | Storage account shared key |

**Forbidden partial:** `accountName` without `accountKey` or converse ⇒ **ERROR**.  
If **`connectionString`** is non-blank **and** any shared-key field non-blank ⇒ **ERROR**.

**Optional future revision:** **Entra/AAD bearer** via **`aadAccessToken`** (non-blank); not required for **WI-264** emulator-centric **`testIT`** if **`connectionString`** covers Azurite—addendum when workload identity **`testIT`** appears.

### YAML examples

**Ambient:**

```yaml
storage:
  type: adls
  accountUrl: https://mystorageaccount.blob.core.windows.net
  filesystem: raw
  # relies on DefaultAzureCredential
```

**Delegated (Azurite connection string env):**

```yaml
storage:
  type: adls
  filesystem: raw
  auth:
    connectionString: ${AZURITE_STORAGE_CONNECTION_STRING}
```

**Delegated (shared key):**

```yaml
storage:
  type: adls
  auth:
    accountName: mystorageaccount
    accountKey: ${AZURE_STORAGE_ACCOUNT_KEY}
```

---

## Emulator `testIT` alignment (credential recipe)

Story requires **one** stable **delegated** configuration per emulator:

| Emulator | Typical delegated bundle (**this doc**) | Notes |
|----------|-----------------------------|-------|
| **MinIO / S3** | `accessKeyId` + `secretAccessKey` | Match MinIO **`MINIO_ROOT_USER`** / **`MINIO_ROOT_PASSWORD`** |
| **fake-gcs-server** | `accessToken` **or** `serviceAccountJson` / path | Pick **one** approach in **`testIT`** and document in **`WI-263`** fixtures |
| **Azurite** | `connectionString` | Canonical dev connection strings from [**Azurite docs**](https://github.com/Azure/Azurite) |

Ambient chains for **GCP/Azure** remain **validated in unit tests** only (**STORY.md** **`testIT`** scope); **do not** multiply emulator auth matrices.

---

## Changelog / revision discipline

Adds or renames **`storage.auth`** keys **only** through:

1. Amendment to **this document** (**PR + story/WI acknowledgement**).
2. **SPI / Jackson subtype** bump with migration note in **`WI-265`** public docs.

## Related work items

- [**WI-262**](../../workitems/planned/cloud-blob-source/WI-262-aws-s3-blob-storage.md) — binds **`auth`** to **§ AWS S3** herein.
- [**WI-263**](../../workitems/planned/cloud-blob-source/WI-263-gcp-gcs-blob-storage.md) — binds **`auth`** to **§ GCS** herein.
- [**WI-264**](../../workitems/planned/cloud-blob-source/WI-264-azure-adls-blob-storage.md) — binds **`auth`** to **§ Azure** herein; **finalize** discriminator string and align examples.
