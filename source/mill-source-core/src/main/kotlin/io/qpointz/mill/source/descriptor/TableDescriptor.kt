package io.qpointz.mill.source.descriptor

import io.qpointz.mill.source.verify.*

/**
 * Groups table-level configuration: mapping strategy and extracted attributes.
 *
 * Used at both source level (shared defaults) and reader level (override).
 * When a reader defines its own `table`, it replaces the source-level
 * `table` entirely — no partial merging.
 *
 * YAML example:
 * ```yaml
 * table:
 *   mapping:
 *     type: regex
 *     pattern: ".*(?<table>[^/]+)\\.csv$"
 *   attributes:
 *     - name: pipeline
 *       source: constant
 *       value: "raw-ingest"
 * ```
 *
 * @property mapping    table mapping strategy (how blobs become tables)
 * @property attributes extra columns extracted from blob paths or constants
 */
data class TableDescriptor(
    val mapping: TableMappingDescriptor? = null,
    val attributes: List<TableAttributeDescriptor> = emptyList()
) : Verifiable {

    override fun verify(): VerificationReport {
        var report = VerificationReport.EMPTY

        if (mapping == null) {
            report += VerificationReport.of(
                VerificationIssue(Severity.ERROR, Phase.TABLE_MAPPING,
                    "Table 'mapping' is required")
            )
        } else if (mapping is Verifiable) {
            report += (mapping as Verifiable).verify()
        }

        for (attr in attributes) {
            report += attr.verify()
        }

        // Check for duplicate attribute names
        val duplicates = attributes.groupBy { it.name }.filter { it.value.size > 1 }.keys
        for (dup in duplicates) {
            report += VerificationReport.of(
                VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                    "Duplicate table attribute name '$dup'",
                    mapOf("attributeName" to dup))
            )
        }

        return report
    }
}
