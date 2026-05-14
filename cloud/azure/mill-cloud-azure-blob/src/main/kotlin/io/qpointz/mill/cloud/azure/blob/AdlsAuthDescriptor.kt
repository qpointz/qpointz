package io.qpointz.mill.cloud.azure.blob

/**
 * Authentication configuration for Azure Data Lake Storage / Blob Storage.
 *
 * Credential selection applies when [AdlsStorageDescriptor.connectionString] is not set:
 *
 * 1. [preferAmbientCredentials] `true` — [com.azure.identity.DefaultAzureCredential] (ignores other explicit fields).
 * 2. [sasToken] — shared-access signature; combined with [AdlsStorageDescriptor.endpoint] (or SAS embedded in that URL).
 * 3. [accountKey] — shared key; [accountName] may be omitted when the account is derivable from a public
 *    `*.blob.core.windows.net` / `*.dfs.core.windows.net` host on [AdlsStorageDescriptor.endpoint].
 * 4. All omitted — ambient credentials.
 *
 * @property accountName      storage account name for shared-key auth (optional if derivable from [AdlsStorageDescriptor.endpoint])
 * @property accountKey       storage account key for shared-key auth
 * @property sasToken         SAS query string (with or without leading `?`); must not duplicate a SAS query on [AdlsStorageDescriptor.endpoint]
 * @property preferAmbientCredentials when `true`, always use `DefaultAzureCredential`
 */
data class AdlsAuthDescriptor(
    val accountName: String? = null,
    val accountKey: String? = null,
    val sasToken: String? = null,
    val preferAmbientCredentials: Boolean = false
)
