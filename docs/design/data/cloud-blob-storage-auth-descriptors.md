# Cloud blob storage — frozen `storage.auth` field sets (GAP-4)

Normative **`StorageDescriptor`** authentication shapes for the **cloud-blob-source** story (**AWS S3**, **GCS**, **Azure Blob / ADLS Gen2-compatible** paths). **`storage.auth`** is **not** a single cross-vendor map: **allowed keys depend on **`storage.type`**** (each provider may use **tokens**, **access keys**, **JSON**, **connection strings**, etc.). Implements **GAP-4** from [`docs/workitems/completed/20260514-cloud-blob-source/GAPS.md`](../../workitems/completed/20260514-cloud-blob-source/GAPS.md): provider WIs (**WI-262–264**) **consume** this document rather than defining divergent **`auth`** maps.

**Story-level summary and per-provider proposals:** [`STORY.md`](../../workitems/completed/20260514-cloud-blob-source/STORY.md) (**Authentication** section).

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

- **Partial bundle:** **`Storage auth incomplete for AWS S3: 'accessKey' provided but 'secretKey' is blank (provide both access key fields or omit entirely for ambient credentials)`** — pattern for all clouds.
- **Unknown key:** **`Unknown storage.auth keys for type 's3': [foobar]. Allowed keys: accessKey, secretKey, sessionToken, preferAmbientCredentials.`**
- **Mutually exclusive bundles:** **`Storage auth specifies more than one GCP delegated bundle (example: accessToken and serviceAccountJson); provide exactly one GCP delegated credential bundle or omit entirely for ambient credentials.`**

---

## AWS S3 — `storage.type: s3`

### IAM policy requirements (read-only)

Mill requires **`s3:ListBucket`** (bucket-level) and **`s3:GetObject`** (object-level) permissions. Note: `HeadObject` is authorized via `s3:GetObject` — there is no separate IAM action.

**Minimal policy:**

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "ListBlobs",
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::BUCKET"
    },
    {
      "Sid": "ReadObjects",
      "Effect": "Allow",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::BUCKET/*"
    }
  ]
}
```

**Common 403 causes:**

| Symptom | Cause |
|---------|-------|
| `ListBucket` works, `GetObject`/`HeadObject` fails | Policy only has bucket ARN (`arn:aws:s3:::bucket`), missing object ARN (`arn:aws:s3:::bucket/*`) |
| All calls fail with generic 403 | Bucket policy explicit `Deny`, SCP, or VPC endpoint policy blocking access |
| 403 on `HeadObject` for KMS-encrypted objects | Missing `kms:Decrypt` on the CMK used for SSE-KMS |
| 403 on Requester Pays bucket | Missing `requesterPays: true` in descriptor (sends `x-amz-request-payer: requester` header) |

### Requester Pays support

When **`storage.requesterPays: true`**, all S3 API calls (`ListObjectsV2`, `HeadObject`, `GetObject`) include the `x-amz-request-payer: requester` header. Required for non-owner access to Requester Pays buckets.

### Delegated bundles (presence triggers non-ambient SDK path)

Exactly **one** of the following delegated modes may be populated (non-blank) at a time. **Mixing modes** ⇒ **ERROR**.

| Bundle | Keys (all camelCase under `storage.auth`) | Required | Maps to emulator / prod |
|--------|-----------------------------|----------|-------------------------|
| **Static access keys** | **`accessKey`**, **`secretKey`**, optional **`sessionToken`** | **`accessKey` + `secretKey`** non-blank when this bundle is used | **MinIO** dev keys (**WI-262**); **IAM user** keys in prod. **`sessionToken`** optional **temporary** STS session creds |

**Forbidden partials for static bundle:**

| Non-blank | Blank forbidden sibling | Severity |
|-----------|--------------------------|----------|
| `accessKey` | `secretKey` | **ERROR** |
| `secretKey` | `accessKey` | **ERROR** |
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
  auth:
    accessKey: ${AWS_ACCESS_KEY_ID}
    secretKey: ${AWS_SECRET_ACCESS_KEY}
    # sessionToken: ${AWS_SESSION_TOKEN}   # optional
```

**Requester Pays bucket:**

```yaml
storage:
  type: s3
  bucket: public-datasets
  prefix: opendata/
  region: us-east-1
  requesterPays: true
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

**Implementation note (Mill):** When materialising the GCS client, service account credentials from JSON (inline or path) are OAuth-scoped with **`https://www.googleapis.com/auth/devstorage.read_only`** before use. Set optional **`storage.projectId`** to match the key’s **`project_id`** when not obvious from the environment.

**Partial / invalid:**

- **`serviceAccountJson`** non-blank but does not start with **`{`** (common mistake: a filesystem path was pasted into **`serviceAccountJson`**) ⇒ **ERROR** — use **`serviceAccountJsonPath`** for paths; **`serviceAccountJson`** is **inline JSON body only**.
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

**Delegated (service account key file path):**

```yaml
storage:
  type: gcs
  bucket: my-bucket
  auth:
    serviceAccountJsonPath: /secure/keys/my-sa.json
```

---

## Azure Blob / ADLS Gen2-compatible — `storage.type: adls`

### Azure RBAC / SAS requirements (read-only)

Mill requires **list** and **read** access to blobs in the target container.

**Entra ID (Azure AD) — recommended:**

| Role | Scope | Notes |
|------|-------|-------|
| **`Storage Blob Data Reader`** | Container or Storage Account | Minimum for list + read. Generic **`Reader`** role is **not sufficient** (grants ARM metadata only, not data-plane access). |

**SAS token permissions:**

| Permission | Flag | Purpose |
|------------|------|---------|
| List | `l` | `ListBlobs` |
| Read | `r` | `GetBlob`, `GetBlobProperties` |

Minimal SAS permissions string: **`rl`**

**Common 403 causes:**

| Symptom | Cause |
|---------|-------|
| 403 with Entra ID | Principal lacks **`Storage Blob Data Reader`** (or higher) on container/account |
| 403 on list, read works | SAS missing **`l`** permission |
| 403 on read, list works | SAS missing **`r`** permission |
| 403 with firewall | Client IP / VNet not in storage account network rules |
| 403 via private endpoint | DNS not resolving to private IP; public endpoint blocked |

### Surface fields (storage level)

| Field | Role |
|-------|------|
| **`endpoint`** | Blob service URL (Azure SDK / docs terminology). May include an embedded SAS query. |
| **`container`** | Blob container name (Gen2 “filesystem”). |
| **`connectionString`** | Optional full connection string at storage level. |

### Delegated bundles (under `storage.auth`)

Exactly **one** credential style applies per descriptor when **`storage.connectionString`** is not used:

| Bundle | Keys | Requirements | Emulator / prod |
|--------|------|----------------|-----------------|
| **Shared key** | **`accountKey`** | Non-blank; **`accountName`** optional if derivable from **`endpoint`** host (`{account}.blob.core.windows.net` / `{account}.dfs.core.windows.net`) | Storage account key |
| **SAS** | **`sasToken`** | Non-blank; combined with **`endpoint`** (or SAS query already on **`endpoint`**) | SAS URIs |

**Forbidden partial:** `accountName` without `accountKey` ⇒ **ERROR** (name alone is never sufficient).  
**Forbidden:** **`accountKey`** together with **`sasToken`**.  
**Forbidden:** SAS on **`endpoint`** query **and** non-blank **`auth.sasToken`** (duplicate SAS).

**Optional future revision:** **Entra/AAD bearer** via **`aadAccessToken`** (non-blank); addendum when workload identity **`testIT`** appears.

### YAML examples

**Ambient:**

```yaml
storage:
  type: adls
  endpoint: https://mystorageaccount.blob.core.windows.net
  container: raw
  # relies on DefaultAzureCredential
```

**Delegated (Azurite connection string env):**

```yaml
storage:
  type: adls
  container: raw
  connectionString: ${AZURITE_STORAGE_CONNECTION_STRING}
```

**Delegated (shared key, inferred account):**

```yaml
storage:
  type: adls
  endpoint: https://mystorageaccount.blob.core.windows.net
  container: raw
  auth:
    accountKey: ${AZURE_STORAGE_ACCOUNT_KEY}
```

---

## Emulator `testIT` alignment (credential recipe)

Story requires **one** stable **delegated** configuration per emulator:

| Emulator | Typical delegated bundle (**this doc**) | Notes |
|----------|-----------------------------|-------|
| **MinIO / S3** | `accessKey` + `secretKey` | Match MinIO **`MINIO_ROOT_USER`** / **`MINIO_ROOT_PASSWORD`** |
| **fake-gcs-server** | `accessToken` **or** `serviceAccountJson` / path | Pick **one** approach in **`testIT`** and document in **`WI-263`** fixtures |
| **Azurite** | `connectionString` | Canonical dev connection strings from [**Azurite docs**](https://github.com/Azure/Azurite) |

Ambient chains for **GCP/Azure** remain **validated in unit tests** only (**STORY.md** **`testIT`** scope); **do not** multiply emulator auth matrices.

---

## Changelog / revision discipline

Adds or renames **`storage.auth`** keys **only** through:

1. Amendment to **this document** (**PR + story/WI acknowledgement**).
2. **SPI / Jackson subtype** bump with migration note in **`WI-265`** public docs.

## Related work items

- [**WI-262**](../../workitems/completed/20260514-cloud-blob-source/WI-262-aws-s3-blob-storage.md) — binds **`auth`** to **§ AWS S3** herein.
- [**WI-263**](../../workitems/completed/20260514-cloud-blob-source/WI-263-gcp-gcs-blob-storage.md) — binds **`auth`** to **§ GCS** herein.
- [**WI-264**](../../workitems/completed/20260514-cloud-blob-source/WI-264-azure-adls-blob-storage.md) — binds **`auth`** to **§ Azure** herein; **finalize** discriminator string and align examples.
