package io.qpointz.mill.cloud.azure.blob

import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.StorageDescriptor
import io.qpointz.mill.source.descriptor.StorageFacetContributor
import io.qpointz.mill.source.descriptor.StorageFacetRedactMode
import io.qpointz.mill.source.verify.*

/**
 * Describes an Azure Data Lake Storage (ADLS) Gen2–compatible blob backend (Blob SDK).
 *
 * ```yaml
 * storage:
 *   type: adls
 *   endpoint: https://myaccount.blob.core.windows.net
 *   container: my-container
 *   prefix: data/
 *   connectionString: "DefaultEndpointsProtocol=https;..."   # optional; alternative to endpoint+auth
 *   auth:
 *     accountKey: ${AZURE_STORAGE_ACCOUNT_KEY}                # account name derived from endpoint host
 * ```
 *
 * @property endpoint blob service base URL (same concept as Azure docs / SDK `endpoint`).
 *   May include a SAS query (`?sv=...`); omit when [connectionString] alone is sufficient.
 * @property container blob container name (blob API); Gen2 calls this resource a filesystem.
 * @property connectionString optional full connection string at storage level
 * @property prefix optional blob name prefix to restrict listing scope
 * @property auth authentication details when not using [connectionString] alone
 */
@JsonTypeName("adls")
data class AdlsStorageDescriptor(
    val endpoint: String = "",
    val container: String = "",
    val connectionString: String? = null,
    val prefix: String? = null,
    val auth: AdlsAuthDescriptor? = null
) : StorageDescriptor, StorageFacetContributor, Verifiable {

    override fun storageFacetParams(mode: StorageFacetRedactMode): Map<String, Any?> {
        return when (mode) {
            StorageFacetRedactMode.NONE -> buildMap {
                put("endpoint", endpoint)
                put("container", container)
                connectionString?.let { put("connectionString", it) }
                prefix?.let { put("prefix", it) }
                auth?.let { a ->
                    put("auth", buildMap {
                        a.accountName?.let { put("accountName", it) }
                        a.accountKey?.let { put("accountKey", it) }
                        a.sasToken?.let { put("sasToken", it) }
                        if (a.preferAmbientCredentials) put("preferAmbientCredentials", true)
                    })
                }
            }

            StorageFacetRedactMode.BASIC -> buildMap {
                val sanitizedEndpoint = sanitizeEndpoint(endpoint)
                if (sanitizedEndpoint.isNotBlank()) put("endpoint", sanitizedEndpoint)
                put("container", container)
                prefix?.let { put("prefix", it) }

                val csConfigured = !connectionString.isNullOrBlank()
                if (csConfigured) put("connectionStringConfigured", true)

                auth?.let { a ->
                    val hasSecrets = !a.accountKey.isNullOrBlank() ||
                        !a.sasToken.isNullOrBlank()
                    if (hasSecrets) put("authConfigured", true)
                    if (a.preferAmbientCredentials) put("preferAmbientCredentials", true)
                    if (!a.accountName.isNullOrBlank()) put("accountName", a.accountName)
                }
            }

            StorageFacetRedactMode.SAFE -> buildMap {
                put("container", container)
                prefix?.let { put("prefix", it) }
                if (!connectionString.isNullOrBlank()) put("connectionStringConfigured", true)
                auth?.let { a ->
                    val hasSecrets = !a.accountKey.isNullOrBlank() || !a.sasToken.isNullOrBlank()
                    if (hasSecrets) put("authConfigured", true)
                }
            }
        }
    }

    private fun sanitizeEndpoint(url: String): String {
        val q = url.indexOf('?')
        if (q < 0) return url
        val query = url.substring(q + 1)
        val looksLikeSas = query.contains("sig=", ignoreCase = true) ||
            (query.contains("se=", ignoreCase = true) && query.contains("sv=", ignoreCase = true))
        return if (looksLikeSas) url.substring(0, q) else url
    }

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        val topCs = connectionString?.takeIf { it.isNotBlank() }
        val endpointTrimmed = endpoint.trim()

        if (topCs == null && endpointTrimmed.isBlank()) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.DESCRIPTOR,
                message = "Storage requires a non-blank 'endpoint' (blob service URL) unless 'connectionString' is set"
            )
        }

        if (container.isBlank()) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.DESCRIPTOR,
                message = "Storage 'container' must not be blank"
            )
        }

        auth?.let { a ->
            if (a.preferAmbientCredentials) {
                val explicit =
                    listOf(
                        !a.accountName.isNullOrBlank(),
                        !a.accountKey.isNullOrBlank(),
                        !a.sasToken.isNullOrBlank()
                    ).count { it }
                if (explicit > 0) {
                    issues += VerificationIssue(
                        severity = Severity.ERROR,
                        phase = Phase.DESCRIPTOR,
                        message = "When 'auth.preferAmbientCredentials' is true, omit keys, SAS, and account name under 'auth'"
                    )
                }
            }

            if (topCs != null) {
                val strayAuth = !a.accountName.isNullOrBlank() || !a.accountKey.isNullOrBlank() ||
                    !a.sasToken.isNullOrBlank()
                if (strayAuth) {
                    issues += VerificationIssue(
                        severity = Severity.ERROR,
                        phase = Phase.DESCRIPTOR,
                        message = "When 'connectionString' is set, omit 'auth.accountName', 'auth.accountKey', and 'auth.sasToken'"
                    )
                }
            }

            val hasName = !a.accountName.isNullOrBlank()
            val hasKey = !a.accountKey.isNullOrBlank()
            val hasSas = !a.sasToken.isNullOrBlank()

            if (hasKey && hasSas) {
                issues += VerificationIssue(
                    severity = Severity.ERROR,
                    phase = Phase.DESCRIPTOR,
                    message = "Mutually exclusive: provide either shared 'accountKey' or 'sasToken', not both"
                )
            }

            if (hasName && !hasKey) {
                issues += VerificationIssue(
                    severity = Severity.ERROR,
                    phase = Phase.DESCRIPTOR,
                    message = "When 'auth.accountName' is set, 'auth.accountKey' must also be set"
                )
            }

            if (hasKey && topCs == null) {
                val derivable = AdlsBlobEndpointSupport.storageAccountFromBlobOrDfsHost(endpointTrimmed) != null
                if (!hasName && !derivable) {
                    issues += VerificationIssue(
                        severity = Severity.ERROR,
                        phase = Phase.DESCRIPTOR,
                        message = "Provide 'auth.accountName' or use an 'endpoint' host like https://{account}.blob.core.windows.net so the account name can be inferred"
                    )
                }
            }
        }

        if (endpointTrimmed.isNotBlank()) {
            val hasKey = auth?.accountKey?.isNotBlank() == true
            val hasName = auth?.accountName?.isNotBlank() == true
            AdlsBlobEndpointSupport.resolveServiceUrlAndSas(endpointTrimmed, auth?.sasToken)
                .onFailure { e ->
                    issues += VerificationIssue(
                        severity = Severity.ERROR,
                        phase = Phase.DESCRIPTOR,
                        message = e.message ?: "Invalid SAS configuration for 'endpoint' / 'auth.sasToken'"
                    )
                }
                .onSuccess { (_, sas) ->
                    if (sas != null && (hasKey || hasName)) {
                        issues += VerificationIssue(
                            severity = Severity.ERROR,
                            phase = Phase.DESCRIPTOR,
                            message = "Do not combine SAS authentication with shared-key 'accountName' / 'accountKey'"
                        )
                    }
                }
        }

        return VerificationReport(issues = issues)
    }
}
