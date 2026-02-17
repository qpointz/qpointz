package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.verify.*

/**
 * Describes a storage backend for blob discovery and I/O.
 *
 * Implementations are discovered via SPI (`ServiceLoader`) and registered
 * with Jackson for polymorphic deserialization. Each subtype must be
 * annotated with [JsonTypeName] to provide its discriminator value.
 *
 * The `type` property in YAML/JSON selects the concrete implementation:
 * ```yaml
 * storage:
 *   type: local
 *   rootPath: /data/airlines
 * ```
 *
 * @see LocalStorageDescriptor
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
interface StorageDescriptor

/**
 * Describes a local filesystem storage backend.
 *
 * @property rootPath absolute or relative path to the root directory
 */
@JsonTypeName("local")
data class LocalStorageDescriptor(
    val rootPath: String
) : StorageDescriptor, Verifiable {

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        if (rootPath.isBlank()) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.STORAGE,
                message = "Storage 'rootPath' must not be blank"
            )
        }

        return VerificationReport(issues = issues)
    }
}
