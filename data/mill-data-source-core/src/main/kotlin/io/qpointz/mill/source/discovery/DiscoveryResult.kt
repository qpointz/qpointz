package io.qpointz.mill.source.discovery

import io.qpointz.mill.source.BlobPath
import io.qpointz.mill.source.Record
import io.qpointz.mill.source.RecordSchema
import io.qpointz.mill.source.verify.Severity
import io.qpointz.mill.source.verify.VerificationIssue

/**
 * A single table discovered during source discovery.
 *
 * @property name        logical table name (after label suffix and conflict resolution)
 * @property schema      inferred schema, or `null` if inference failed for all blobs
 * @property blobPaths   blobs that back this table
 * @property readerType  format type of the reader that discovered this table (e.g. "csv", "parquet")
 * @property readerLabel label of the reader, or `null`
 * @property sampleRecords optional preview rows (empty if not requested or unavailable)
 */
data class DiscoveredTable(
    val name: String,
    val schema: RecordSchema?,
    val blobPaths: List<BlobPath>,
    val readerType: String,
    val readerLabel: String? = null,
    val sampleRecords: List<Record> = emptyList()
)

/**
 * Result of running source discovery.
 *
 * Contains all findings: discovered tables, issues encountered, and
 * blob statistics. Discovery never throws — all errors are captured
 * as [VerificationIssue] entries with [Severity.ERROR].
 *
 * @property tables            successfully discovered tables
 * @property issues            all issues encountered during discovery
 * @property blobCount         total blobs found in storage (0 if storage access failed)
 * @property unmappedBlobCount blobs that did not match any reader's table mapping
 */
data class DiscoveryResult(
    val tables: List<DiscoveredTable> = emptyList(),
    val issues: List<VerificationIssue> = emptyList(),
    val blobCount: Int = 0,
    val unmappedBlobCount: Int = 0
) {

    /** `true` if no ERROR-level issues were found. */
    val isSuccessful: Boolean get() = issues.none { it.severity == Severity.ERROR }

    /** All ERROR-level issues. */
    val errors: List<VerificationIssue> get() = issues.filter { it.severity == Severity.ERROR }

    /** All WARNING-level issues. */
    val warnings: List<VerificationIssue> get() = issues.filter { it.severity == Severity.WARNING }

    /** All INFO-level issues. */
    val infos: List<VerificationIssue> get() = issues.filter { it.severity == Severity.INFO }

    /** Names of all discovered tables. */
    val tableNames: Set<String> get() = tables.map { it.name }.toSet()

    companion object {
        /** Empty result with no findings. */
        val EMPTY = DiscoveryResult()

        /** Creates a failed result with a single error. */
        fun failed(issue: VerificationIssue) = DiscoveryResult(issues = listOf(issue))
    }
}

/**
 * Configuration for source discovery.
 *
 * @property maxSampleRecords maximum number of preview rows per table (0 = no samples)
 */
data class DiscoveryOptions(
    val maxSampleRecords: Int = 0
)
