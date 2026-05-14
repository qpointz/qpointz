# Azure Blob Storage

The `adls` storage type reads files from an Azure Blob Storage container (or Azure Data Lake Storage Gen2). Readers, table mapping, and all other source configuration options work exactly the same as with [local storage](local.md) — only the `storage` block changes.

---

## Configuration

```yaml
storage:
  type: adls
  container: my-data-container
  prefix: warehouse/parquet/
  endpoint: https://myaccount.blob.core.windows.net
```

| Property    | Required | Default | Description |
|-------------|----------|---------|-------------|
| `container` | yes      | —       | Azure Blob Storage container name. |
| `prefix`    | no       | —       | Blob name prefix to limit the scan scope. |
| `endpoint`  | no       | —       | Azure Blob Storage endpoint URL. Required unless using a connection string. Use for Azurite emulator in local dev. |

---

## Authentication

When `storage.auth` is omitted, Mill uses **`DefaultAzureCredential`** — managed identity, environment credentials, Azure CLI login, etc. This is the recommended approach for production.

For explicit credentials, provide a connection string:

```yaml
storage:
  type: adls
  container: my-data-container
  auth:
    connectionString: ${AZURE_STORAGE_CONNECTION_STRING}
```

Or use account key authentication:

```yaml
storage:
  type: adls
  container: my-data-container
  endpoint: https://myaccount.blob.core.windows.net
  auth:
    accountName: ${AZURE_STORAGE_ACCOUNT}
    accountKey: ${AZURE_STORAGE_ACCOUNT_KEY}
```

| Auth property        | Description |
|----------------------|-------------|
| `connectionString`   | Azure Storage connection string (includes account, key, and endpoint). |
| `accountName`        | Storage account name. |
| `accountKey`         | Storage account key. |
| `preferAmbientCredentials` | When `true`, forces `DefaultAzureCredential` resolution even if explicit credentials are present. |

Do not commit connection strings or account keys to version control. Use environment variable expansion (`${VAR}`) or a secret store.

---

## Local Development with Azurite

For local development and CI, use [Azurite](https://learn.microsoft.com/en-us/azure/storage/common/storage-use-azurite) as an Azure Blob Storage emulator:

```yaml
storage:
  type: adls
  container: test-container
  endpoint: http://127.0.0.1:10000/devstoreaccount1
  auth:
    connectionString: "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;"
```

---

## Example

Complete source descriptor reading Parquet files from Azure:

```yaml
name: warehouse
storage:
  type: adls
  container: analytics
  prefix: warehouse/
  endpoint: https://myaccount.blob.core.windows.net
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
- [Local storage](local.md) — for files on disk
- [Flow Backend](../../backends/flow.md) — wiring sources into the query engine
