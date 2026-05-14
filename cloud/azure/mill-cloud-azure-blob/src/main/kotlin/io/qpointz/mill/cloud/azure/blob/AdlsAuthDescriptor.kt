package io.qpointz.mill.cloud.azure.blob

/**
 * Authentication configuration for Azure Data Lake Storage / Blob Storage.
 *
 * Exactly one of the following mutually exclusive auth strategies must be provided:
 * - [connectionString] alone
 * - [accountName] + [accountKey] pair (shared key)
 * - All blank / omitted → ambient credentials ([com.azure.identity.DefaultAzureCredential])
 *
 * When [preferAmbientCredentials] is `true`, explicit credentials are ignored and
 * `DefaultAzureCredential` is used regardless.
 *
 * @property connectionString full Azure Storage connection string
 * @property accountName      storage account name for shared-key auth
 * @property accountKey       storage account key for shared-key auth
 * @property preferAmbientCredentials when `true`, always use `DefaultAzureCredential`
 */
data class AdlsAuthDescriptor(
    val connectionString: String? = null,
    val accountName: String? = null,
    val accountKey: String? = null,
    val preferAmbientCredentials: Boolean = false
)
