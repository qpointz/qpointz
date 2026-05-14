# Azure Blob Storage

The `adls` storage type reads files from **Azure Blob Storage** or **Azure Data Lake Storage Gen2**. Configuration uses the same names as the Azure Blob SDK and portal: **`endpoint`** (blob service URL passed to `BlobServiceClientBuilder`) and **`container`** (blob container; in Gen2 this is also called a filesystem in the Data Lake REST API).

Readers, table mapping, and all other source configuration options work the same as for [local storage](local.md); only the `storage` block changes.

---

## Configuration

```yaml
storage:
  type: adls
  endpoint: https://myaccount.blob.core.windows.net
  container: my-data-container
  prefix: warehouse/parquet/
```

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `endpoint` | yes* | — | Blob service URL for the storage account. If you use a Gen2 **DFS** hostname (`https://{account}.dfs.core.windows.net`), Mill rewrites it to the equivalent **blob** host for the Blob SDK. You may append a SAS query (`?sv=...&sig=...`) **or** use `auth.sasToken`, but not both. |
| `container` | yes | — | Blob container name. |
| `prefix` | no | — | Blob name prefix that limits listing scope. |
| `connectionString` | no | — | Full Azure Storage connection string at `storage` level. When set, the SDK builds the client from it alone; omit `endpoint` if the string fully defines the account. |

\*Required unless **`connectionString`** is set at `storage` level; then `endpoint` may be omitted.

---

## How credentials are chosen

Mill evaluates `storage` in a fixed order (matching the implementation):

1. **Connection string** — non-blank `storage.connectionString`.
2. **Forced ambient** — `auth.preferAmbientCredentials: true` and a non-blank `endpoint` (see below). Ignored if step 1 already applied.
3. **SAS** — non-blank `auth.sasToken`, or a SAS query on `endpoint`.
4. **Shared key** — non-blank `auth.accountKey` (and optional `auth.accountName`; see [Shared key](#shared-key-account-key)).
5. **Default ambient** — `DefaultAzureCredential` with `endpoint`.

If step 1 applies, `auth.accountName`, `auth.accountKey`, and `auth.sasToken` must be omitted (the verifier rejects them).

---

## Authentication

### Default (recommended for production)

Omit `auth` and omit `connectionString`. Mill uses **`DefaultAzureCredential`** with your **`endpoint`** (managed identity, environment variables, Azure CLI, etc.).

```yaml
storage:
  type: adls
  endpoint: https://myaccount.blob.core.windows.net
  container: my-data-container
```

### Connection string

Use **`storage.connectionString`** for Azurite or any setup where a single string defines account, credentials, and endpoints:

```yaml
storage:
  type: adls
  container: my-data-container
  connectionString: ${AZURE_STORAGE_CONNECTION_STRING}
```

### Shared key (account key)

If **`endpoint`** uses a public hostname **`{account}.blob.core.windows.net`** or **`{account}.dfs.core.windows.net`**, Mill infers **`{account}`** and you only need the key:

```yaml
storage:
  type: adls
  endpoint: https://myaccount.blob.core.windows.net
  container: my-data-container
  auth:
    accountKey: ${AZURE_STORAGE_ACCOUNT_KEY}
```

For **Azurite**, **private endpoints**, or other hosts where the account name is not in the DNS name, set **`accountName`** and **`accountKey`** together:

```yaml
storage:
  type: adls
  endpoint: http://127.0.0.1:10000/devstoreaccount1
  container: test-container
  auth:
    accountName: devstoreaccount1
    accountKey: ${AZURE_STORAGE_ACCOUNT_KEY}
```

### SAS

Use **either** a SAS query on **`endpoint`** **or** **`auth.sasToken`** (with or without a leading `?`). Combining both is invalid.

```yaml
storage:
  type: adls
  endpoint: https://myaccount.blob.core.windows.net
  container: my-data-container
  auth:
    sasToken: ${AZURE_BLOB_SAS_TOKEN}
```

Do not combine SAS authentication with **`auth.accountKey`** / **`auth.accountName`**.

### Forced ambient (`preferAmbientCredentials`)

When **`auth.preferAmbientCredentials`** is `true`, Mill uses **`DefaultAzureCredential`** with **`endpoint`** (which must be non-blank). You must not set **`accountName`**, **`accountKey`**, or **`sasToken`** under `auth`. A top-level **`storage.connectionString`** may still be present; if it is non-blank, step 1 wins and the connection string is used instead of ambient auth.

| `auth` field | Role |
|--------------|------|
| `accountName` | Optional with `accountKey` when the storage account can be inferred from `endpoint`; required with `accountKey` for emulators/custom hosts. |
| `accountKey` | Shared key (mutually exclusive with `sasToken`). |
| `sasToken` | SAS token or query string (mutually exclusive with `accountKey`). |
| `preferAmbientCredentials` | If `true`, use ambient credentials only; no other `auth` fields except this flag. |

Do not commit connection strings, keys, or SAS tokens to source control. Use `${VAR}` expansion or a secret store.

---

## Azure RBAC Permissions

Mill requires **read-only** access to list and retrieve blobs.

### Using Entra ID (Azure AD) — recommended

Assign the **Storage Blob Data Reader** built-in role at the container or storage account scope:

| Role | Scope | Purpose |
|------|-------|---------|
| `Storage Blob Data Reader` | Container or Storage Account | List blobs, read blob content and metadata |

Azure CLI example:

```bash
az role assignment create \
  --role "Storage Blob Data Reader" \
  --assignee <principal-id-or-email> \
  --scope /subscriptions/<sub>/resourceGroups/<rg>/providers/Microsoft.Storage/storageAccounts/<account>/blobServices/default/containers/<container>
```

!!! note "Reader vs Storage Blob Data Reader"
    The generic **Reader** role grants access to Azure Resource Manager metadata but **not** to blob data. You need **Storage Blob Data Reader** (or higher) for actual blob operations.

### Using SAS tokens

Generate a SAS with at least these permissions:

| Permission | Flag | Purpose |
|------------|------|---------|
| List | `l` | List blobs in container |
| Read | `r` | Read blob content |

Minimal SAS permissions string: `rl`

Azure CLI example:

```bash
az storage container generate-sas \
  --account-name <account> \
  --name <container> \
  --permissions rl \
  --expiry 2025-12-31T00:00:00Z \
  --auth-mode key \
  --account-key <key>
```

### Using shared key (account key)

Account keys grant full access to the storage account. For least-privilege, prefer **SAS** or **RBAC** scoped to the specific container.

### Common 403 causes

| Symptom | Cause |
|---------|-------|
| 403 with Entra ID | Missing **Storage Blob Data Reader** role (generic **Reader** is not enough) |
| 403 on list but read works | SAS missing `l` (list) permission |
| 403 on read but list works | SAS missing `r` (read) permission |
| 403 with firewall enabled | Client IP not in storage account firewall allowlist or missing virtual network rule |
| 403 on private endpoint | DNS not resolving to private IP; falling back to public endpoint which may be blocked |

---

## Local development with Azurite

Use [Azurite](https://learn.microsoft.com/en-us/azure/storage/common/storage-use-azurite) and a connection string (adjust host and port to match your emulator):

```yaml
storage:
  type: adls
  container: test-container
  connectionString: ${AZURITE_STORAGE_CONNECTION_STRING}
```

The Azurite docs publish sample connection strings (including the well-known **devstoreaccount1** development key); treat them as **non-production** credentials.

---

## Example

Full source descriptor reading Parquet from Azure:

```yaml
name: warehouse
storage:
  type: adls
  endpoint: https://myaccount.blob.core.windows.net
  container: analytics
  prefix: warehouse/
readers:
  - type: parquet
    table:
      mapping:
        type: directory
        depth: 1
```

---

## See Also

- [Configuration](../configuration.md) — full YAML specification
- [AWS S3](s3.md) / [Google Cloud Storage](gcs.md) — other cloud storages
- [Local storage](local.md) — files on disk
- [Flow backend](../../backends/flow.md) — wiring sources into the query engine
