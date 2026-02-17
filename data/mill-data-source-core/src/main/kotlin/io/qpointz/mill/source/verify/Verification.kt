package io.qpointz.mill.source.verify

import io.qpointz.mill.source.RecordSchema

/**
 * Severity of a verification issue.
 */
enum class Severity {
    /** Fatal problem — source cannot be used. */
    ERROR,
    /** Potential problem — source may work but behavior could be unexpected. */
    WARNING,
    /** Informational — nothing wrong, but worth noting. */
    INFO
}

/**
 * Phase of the verification pipeline where the issue was detected.
 */
enum class Phase {
    /** Static descriptor validation (no I/O). */
    DESCRIPTOR,
    /** Storage access and blob discovery. */
    STORAGE,
    /** Reader materialization (format handler, table mapper). */
    READER,
    /** Table mapping evaluation. */
    TABLE_MAPPING,
    /** Format handler instantiation or config. */
    FORMAT,
    /** Schema inference from blob content. */
    SCHEMA,
    /** Cross-reader table name collision analysis. */
    CONFLICT
}

/**
 * A single issue found during verification.
 *
 * @property severity how serious this issue is
 * @property phase    pipeline phase where it was detected
 * @property message  human-readable description
 * @property context  optional key-value metadata (reader index, table name, blob path, etc.)
 */
data class VerificationIssue(
    val severity: Severity,
    val phase: Phase,
    val message: String,
    val context: Map<String, String> = emptyMap()
)

/**
 * Summary of a discovered table.
 *
 * @property name       final table name (after label suffix and conflict resolution)
 * @property blobCount  number of blobs backing this table
 * @property readerType format type of the reader that discovered this table
 * @property readerLabel label of the reader (if any)
 * @property schema     inferred schema (null if inference failed)
 * @property resolution how a collision was resolved (null if no collision)
 */
data class TableSummary(
    val name: String,
    val blobCount: Int,
    val readerType: String,
    val readerLabel: String? = null,
    val schema: RecordSchema? = null,
    val resolution: String? = null
)

/**
 * Result of verifying a source or component.
 *
 * Reports are composable via the [plus] operator — parent objects
 * aggregate reports from their children.
 *
 * @property issues all issues found during verification
 * @property tables discovered table summaries (populated during deep verification)
 */
data class VerificationReport(
    val issues: List<VerificationIssue> = emptyList(),
    val tables: List<TableSummary> = emptyList()
) {

    /** `true` if no ERROR-level issues were found. */
    val isValid: Boolean get() = issues.none { it.severity == Severity.ERROR }

    /** All ERROR-level issues. */
    val errors: List<VerificationIssue> get() = issues.filter { it.severity == Severity.ERROR }

    /** All WARNING-level issues. */
    val warnings: List<VerificationIssue> get() = issues.filter { it.severity == Severity.WARNING }

    /** All INFO-level issues. */
    val infos: List<VerificationIssue> get() = issues.filter { it.severity == Severity.INFO }

    /** Combines two reports (issues and tables are concatenated). */
    operator fun plus(other: VerificationReport): VerificationReport =
        VerificationReport(issues + other.issues, tables + other.tables)

    companion object {
        /** Empty report with no issues and no tables. */
        val EMPTY = VerificationReport()

        /** Creates a report with a single issue. */
        fun of(issue: VerificationIssue) = VerificationReport(issues = listOf(issue))

        /** Creates a report from a list of issues. */
        fun of(issues: List<VerificationIssue>) = VerificationReport(issues = issues)
    }
}
