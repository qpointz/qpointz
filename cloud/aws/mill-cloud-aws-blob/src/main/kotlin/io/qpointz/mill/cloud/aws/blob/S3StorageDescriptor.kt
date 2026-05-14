package io.qpointz.mill.cloud.aws.blob

import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.StorageDescriptor
import io.qpointz.mill.source.descriptor.StorageFacetContributor
import io.qpointz.mill.source.descriptor.StorageFacetRedactMode
import io.qpointz.mill.source.verify.*

/**
 * Describes an AWS S3 storage backend for blob discovery and I/O.
 *
 * Discriminator value is `"s3"` (frozen — must never change).
 *
 * Example YAML:
 * ```yaml
 * storage:
 *   type: s3
 *   bucket: my-data-lake
 *   prefix: raw/airlines/
 *   region: eu-west-1
 *   requesterPays: false
 *   auth:
 *     accessKey: AKIA...
 *     secretKey: wJal...
 * ```
 *
 * @property bucket   S3 bucket name (required, must not be blank)
 * @property prefix   key prefix for blob listing (optional, defaults to empty → whole bucket)
 * @property region   AWS region override (optional; SDK default region chain if null)
 * @property endpoint custom endpoint URI override — for MinIO or other S3-compatible stores
 * @property requesterPays when `true`, sends `x-amz-request-payer: requester` on list, head, and
 *   get calls — required for successful reads against many **Requester Pays** buckets when the caller
 *   is not the bucket owner (otherwise S3 returns **403** even if the principal can list keys).
 * @property auth     authentication configuration (null → ambient credentials)
 */
@JsonTypeName("s3")
data class S3StorageDescriptor(
    val bucket: String,
    val prefix: String = "",
    val region: String? = null,
    val endpoint: String? = null,
    val requesterPays: Boolean = false,
    val auth: S3AuthDescriptor? = null
) : StorageDescriptor, StorageFacetContributor, Verifiable {

    override fun storageFacetParams(mode: StorageFacetRedactMode): Map<String, Any?> {
        return when (mode) {
            StorageFacetRedactMode.NONE -> buildMap {
                put("bucket", bucket)
                if (prefix.isNotBlank()) put("prefix", prefix)
                region?.let { put("region", it) }
                endpoint?.let { put("endpoint", it) }
                if (requesterPays) put("requesterPays", true)
                auth?.let { a ->
                    put("auth", buildMap {
                        a.accessKey?.let { put("accessKey", it) }
                        a.secretKey?.let { put("secretKey", it) }
                        a.sessionToken?.let { put("sessionToken", it) }
                        if (a.preferAmbientCredentials) put("preferAmbientCredentials", true)
                    })
                }
            }

            StorageFacetRedactMode.BASIC -> buildMap {
                put("bucket", bucket)
                if (prefix.isNotBlank()) put("prefix", prefix)
                region?.let { put("region", it) }
                endpoint?.let { put("endpoint", it) }
                if (requesterPays) put("requesterPays", true)
                auth?.let { a ->
                    val hasSecrets = !a.accessKey.isNullOrBlank() ||
                        !a.secretKey.isNullOrBlank() ||
                        !a.sessionToken.isNullOrBlank()
                    if (hasSecrets) put("authConfigured", true)
                    if (a.preferAmbientCredentials) put("preferAmbientCredentials", true)
                }
            }

            StorageFacetRedactMode.SAFE -> buildMap {
                put("bucket", bucket)
                if (prefix.isNotBlank()) put("prefix", prefix)
                region?.let { put("region", it) }
                if (requesterPays) put("requesterPays", true)
                auth?.let { a ->
                    val hasSecrets = !a.accessKey.isNullOrBlank() ||
                        !a.secretKey.isNullOrBlank()
                    if (hasSecrets) put("authConfigured", true)
                }
            }
        }
    }

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        if (bucket.isBlank()) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.STORAGE,
                message = "Storage 'bucket' must not be blank"
            )
        }

        auth?.verify()?.let { issues.addAll(it) }

        return VerificationReport(issues = issues)
    }
}
