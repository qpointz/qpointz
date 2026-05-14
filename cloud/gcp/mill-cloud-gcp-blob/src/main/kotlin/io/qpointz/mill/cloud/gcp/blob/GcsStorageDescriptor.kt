package io.qpointz.mill.cloud.gcp.blob

import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.StorageDescriptor
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
) : StorageDescriptor, Verifiable {

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
        }

        return VerificationReport(issues = issues)
    }
}
