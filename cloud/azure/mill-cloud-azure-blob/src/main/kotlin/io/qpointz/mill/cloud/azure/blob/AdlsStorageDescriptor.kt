package io.qpointz.mill.cloud.azure.blob

import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.StorageDescriptor
import io.qpointz.mill.source.verify.*

/**
 * Describes an Azure Data Lake Storage (ADLS) / Blob Storage backend.
 *
 * YAML example:
 * ```yaml
 * storage:
 *   type: adls
 *   accountUrl: https://myaccount.blob.core.windows.net
 *   filesystem: my-container
 *   prefix: data/
 *   auth:
 *     connectionString: "DefaultEndpointsProtocol=https;..."
 * ```
 *
 * @property accountUrl  base URL of the storage account (e.g. `https://account.blob.core.windows.net`)
 * @property filesystem  blob container name (ADLS "filesystem")
 * @property prefix      optional blob name prefix to restrict listing scope
 * @property endpoint    optional custom endpoint override (e.g. Azurite)
 * @property auth        authentication configuration; `null` implies ambient credentials
 */
@JsonTypeName("adls")
data class AdlsStorageDescriptor(
    val accountUrl: String = "",
    val filesystem: String = "",
    val prefix: String? = null,
    val endpoint: String? = null,
    val auth: AdlsAuthDescriptor? = null
) : StorageDescriptor, Verifiable {

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        if (accountUrl.isBlank()) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.DESCRIPTOR,
                message = "Storage 'accountUrl' must not be blank"
            )
        }

        if (filesystem.isBlank()) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.DESCRIPTOR,
                message = "Storage 'filesystem' (container name) must not be blank"
            )
        }

        auth?.let { a ->
            val hasCs = !a.connectionString.isNullOrBlank()
            val hasName = !a.accountName.isNullOrBlank()
            val hasKey = !a.accountKey.isNullOrBlank()

            if (hasCs && (hasName || hasKey)) {
                issues += VerificationIssue(
                    severity = Severity.ERROR,
                    phase = Phase.DESCRIPTOR,
                    message = "Mutually exclusive: provide either 'connectionString' or 'accountName'+'accountKey', not both"
                )
            }

            if (hasName != hasKey) {
                issues += VerificationIssue(
                    severity = Severity.ERROR,
                    phase = Phase.DESCRIPTOR,
                    message = "Both 'accountName' and 'accountKey' must be provided together"
                )
            }
        }

        return VerificationReport(issues = issues)
    }
}
