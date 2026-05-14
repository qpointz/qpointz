package io.qpointz.mill.cloud.aws.blob

import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.descriptor.StorageDescriptor
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
 *   auth:
 *     accessKeyId: AKIA...
 *     secretAccessKey: wJal...
 * ```
 *
 * @property bucket   S3 bucket name (required, must not be blank)
 * @property prefix   key prefix for blob listing (optional, defaults to empty → whole bucket)
 * @property region   AWS region override (optional; SDK default region chain if null)
 * @property endpoint custom endpoint URI override — for MinIO or other S3-compatible stores
 * @property auth     authentication configuration (null → ambient credentials)
 */
@JsonTypeName("s3")
data class S3StorageDescriptor(
    val bucket: String,
    val prefix: String = "",
    val region: String? = null,
    val endpoint: String? = null,
    val auth: S3AuthDescriptor? = null
) : StorageDescriptor, Verifiable {

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
