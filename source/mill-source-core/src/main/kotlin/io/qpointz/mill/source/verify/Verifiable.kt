package io.qpointz.mill.source.verify

/**
 * A component that can verify its own configuration or state.
 *
 * Implemented at every level of the source hierarchy:
 * - **Descriptors** (static checks, no I/O): validates config values
 * - **Materialized objects** (deep checks): probes storage, schemas, conflicts
 *
 * Implementations must **never throw** — all issues are collected
 * into the returned [VerificationReport].
 *
 * Parents aggregate child reports:
 * ```kotlin
 * override fun verify(): VerificationReport {
 *     var report = VerificationReport.EMPTY
 *     // own checks...
 *     report += child.verify()
 *     return report
 * }
 * ```
 */
interface Verifiable {

    /**
     * Verifies this component and returns a report of all issues found.
     *
     * @return a [VerificationReport] — never throws
     */
    fun verify(): VerificationReport
}
