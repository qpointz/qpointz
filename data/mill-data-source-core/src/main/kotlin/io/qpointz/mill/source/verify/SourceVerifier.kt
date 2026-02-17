package io.qpointz.mill.source.verify

import io.qpointz.mill.source.descriptor.SourceDescriptor
import io.qpointz.mill.source.factory.MaterializedSource
import io.qpointz.mill.source.factory.SourceMaterializer

/**
 * Convenience entry point for source verification.
 *
 * Supports three modes:
 * - **Static only** — validates the descriptor without I/O
 * - **Deep only** — probes an already-materialized source
 * - **Full** — static validation + materialization + deep verification
 *
 * ```kotlin
 * // Full verification
 * val report = SourceVerifier.verify(descriptor)
 * if (!report.isValid) {
 *     report.errors.forEach { println("ERROR: ${it.message}") }
 * }
 *
 * // Static only (fast, no I/O)
 * val staticReport = SourceVerifier.verifyDescriptor(descriptor)
 *
 * // Deep only (already materialized)
 * val deepReport = SourceVerifier.verify(materializedSource)
 * ```
 */
object SourceVerifier {

    /**
     * Static validation only — checks descriptor config without I/O.
     *
     * @param descriptor the source descriptor to validate
     * @return verification report with static issues only
     */
    fun verifyDescriptor(descriptor: SourceDescriptor): VerificationReport =
        descriptor.verify()

    /**
     * Deep verification only — probes an already-materialized source.
     *
     * @param source the materialized source to verify
     * @return verification report with deep issues and table summaries
     */
    fun verify(source: MaterializedSource): VerificationReport =
        source.verify()

    /**
     * Full verification: static + materialization + deep.
     *
     * First runs static descriptor checks, then attempts to materialize
     * the source, and if successful runs deep verification. Materialization
     * failures are caught and reported as errors.
     *
     * @param descriptor   the source descriptor to verify
     * @param materializer the materializer to use (default: SPI-based)
     * @return combined report from all phases
     */
    fun verify(
        descriptor: SourceDescriptor,
        materializer: SourceMaterializer = SourceMaterializer()
    ): VerificationReport {
        // Static phase
        var report = descriptor.verify()

        // Materialization + deep phase
        try {
            materializer.materialize(descriptor).use { materialized ->
                report += materialized.verify()
            }
        } catch (e: Exception) {
            report += VerificationReport.of(
                VerificationIssue(
                    severity = Severity.ERROR,
                    phase = Phase.READER,
                    message = "Materialization failed: ${e.message}"
                )
            )
        }

        return report
    }
}
