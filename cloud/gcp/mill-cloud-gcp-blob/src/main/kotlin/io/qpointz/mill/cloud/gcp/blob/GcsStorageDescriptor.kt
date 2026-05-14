package io.qpointz.mill.cloud.gcp.blob

import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.StorageDescriptor
import io.qpointz.mill.source.descriptor.StorageFacetContributor
import io.qpointz.mill.source.descriptor.StorageFacetRedactMode
import io.qpointz.mill.source.verify.*

/**
 * Describes a Google Cloud Storage backend for blob discovery and I/O.
 *
 * The `type` discriminator is `"gcs"`:
 * ```yaml
 * storage:
 *   type: gcs
 *   bucket: my-data-lake
 *   prefix: warehouse/
 * ```
 *
 * @property bucket    GCS bucket name (required, must not be blank)
 * @property prefix    optional key prefix used to scope blob listing
 * @property projectId optional Google Cloud project ID
 * @property endpoint  optional custom GCS endpoint (e.g. for emulators)
 * @property auth      authentication configuration; omit for ADC
 */
@JsonTypeName("gcs")
data class GcsStorageDescriptor(
    val bucket: String,
    val prefix: String? = null,
    val projectId: String? = null,
    val endpoint: String? = null,
    val auth: GcsAuthDescriptor? = null
) : StorageDescriptor, StorageFacetContributor, Verifiable {

    override fun storageFacetParams(mode: StorageFacetRedactMode): Map<String, Any?> {
        return when (mode) {
            StorageFacetRedactMode.NONE -> buildMap {
                put("bucket", bucket)
                prefix?.let { put("prefix", it) }
                projectId?.let { put("projectId", it) }
                endpoint?.let { put("endpoint", it) }
                auth?.let { a ->
                    put("auth", buildMap {
                        a.accessToken?.let { put("accessToken", it) }
                        a.serviceAccountJson?.let { put("serviceAccountJson", it) }
                        a.serviceAccountJsonPath?.let { put("serviceAccountJsonPath", it) }
                        if (a.preferAmbientCredentials) put("preferAmbientCredentials", true)
                    })
                }
            }

            StorageFacetRedactMode.BASIC -> buildMap {
                put("bucket", bucket)
                prefix?.let { put("prefix", it) }
                projectId?.let { put("projectId", it) }
                endpoint?.let { put("endpoint", it) }
                auth?.let { a ->
                    val hasSecrets = a.activeDelegatedBundles().isNotEmpty()
                    if (hasSecrets) put("authConfigured", true)
                    if (a.preferAmbientCredentials) put("preferAmbientCredentials", true)
                }
            }

            StorageFacetRedactMode.SAFE -> buildMap {
                put("bucket", bucket)
                prefix?.let { put("prefix", it) }
                projectId?.let { put("projectId", it) }
                auth?.let { a ->
                    if (a.activeDelegatedBundles().isNotEmpty()) put("authConfigured", true)
                }
            }
        }
    }

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        if (bucket.isBlank()) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.DESCRIPTOR,
                message = "Storage 'bucket' must not be blank"
            )
        }

        val auth = this.auth
        if (auth != null) {
            val active = auth.activeDelegatedBundles()
            if (active.size > 1) {
                issues += VerificationIssue(
                    severity = Severity.ERROR,
                    phase = Phase.DESCRIPTOR,
                    message = "Only one delegated auth bundle may be set, but found: ${active.joinToString()}"
                )
            }

            auth.serviceAccountJson?.trim()?.takeIf { it.isNotEmpty() }?.let { json ->
                if (!json.startsWith("{")) {
                    issues += VerificationIssue(
                        severity = Severity.ERROR,
                        phase = Phase.DESCRIPTOR,
                        message = "GCS auth 'serviceAccountJson' must be inline service-account key JSON " +
                            "(starts with '{'). For a filesystem path use 'serviceAccountJsonPath' instead."
                    )
                }
            }
        }

        return VerificationReport(issues = issues)
    }
}
