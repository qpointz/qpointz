package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.qpointz.mill.source.verify.*

/**
 * Describes a strategy for mapping blobs to logical table names.
 *
 * Implementations are discovered via SPI (`ServiceLoader`) and registered
 * with Jackson for polymorphic deserialization. Each subtype must be
 * annotated with [JsonTypeName] to provide its discriminator value.
 *
 * The `type` property in YAML/JSON selects the concrete implementation:
 * ```yaml
 * tableMapping:
 *   type: regex
 *   pattern: ".*(?<table>[^/]+)\\.csv$"
 * ```
 *
 * @see RegexTableMappingDescriptor
 * @see DirectoryTableMappingDescriptor
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
interface TableMappingDescriptor

/**
 * Maps blobs to tables using a regex with a named capture group.
 *
 * @property pattern        regex pattern string with a named group for the table name
 * @property tableNameGroup name of the capture group (default `"table"`)
 */
@JsonTypeName("regex")
data class RegexTableMappingDescriptor(
    val pattern: String,
    val tableNameGroup: String = "table"
) : TableMappingDescriptor, Verifiable {

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        if (pattern.isBlank()) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.TABLE_MAPPING,
                message = "Regex table mapping 'pattern' must not be blank"
            )
        } else {
            try {
                Regex(pattern) // validate it compiles
                val groupNames = extractNamedGroups(pattern)
                if (tableNameGroup !in groupNames) {
                    issues += VerificationIssue(
                        severity = Severity.ERROR,
                        phase = Phase.TABLE_MAPPING,
                        message = "Regex pattern does not contain named group '${tableNameGroup}'. " +
                            "Found groups: ${groupNames.ifEmpty { "(none)" }}",
                        context = mapOf("pattern" to pattern, "expectedGroup" to tableNameGroup)
                    )
                }
            } catch (e: Exception) {
                issues += VerificationIssue(
                    severity = Severity.ERROR,
                    phase = Phase.TABLE_MAPPING,
                    message = "Invalid regex pattern: ${e.message}",
                    context = mapOf("pattern" to pattern)
                )
            }
        }

        return VerificationReport(issues = issues)
    }
}

/**
 * Extracts named group names from a regex pattern string.
 */
private fun extractNamedGroups(pattern: String): Set<String> {
    val groupPattern = Regex("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>")
    return groupPattern.findAll(pattern).map { it.groupValues[1] }.toSet()
}

/**
 * Maps blobs to tables based on parent directory names.
 *
 * @property depth how many levels up from the file to look (1 = immediate parent)
 */
@JsonTypeName("directory")
data class DirectoryTableMappingDescriptor(
    val depth: Int = 1
) : TableMappingDescriptor, Verifiable {

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        if (depth < 1) {
            issues += VerificationIssue(
                severity = Severity.ERROR,
                phase = Phase.TABLE_MAPPING,
                message = "Directory table mapping 'depth' must be >= 1, got $depth",
                context = mapOf("depth" to depth.toString())
            )
        }

        return VerificationReport(issues = issues)
    }
}
