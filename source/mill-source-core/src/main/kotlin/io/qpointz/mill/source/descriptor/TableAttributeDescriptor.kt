package io.qpointz.mill.source.descriptor

import io.qpointz.mill.source.verify.*
import io.qpointz.mill.types.sql.DatabaseType
import java.time.format.DateTimeFormatter

/**
 * How the attribute value is obtained.
 */
enum class AttributeSource {
    /** Extract from blob path via regex named group. */
    REGEX,
    /** Inject a constant value into every record. */
    CONSTANT
}

/**
 * Supported primitive types for attribute values.
 */
enum class AttributeType {
    STRING, INT, LONG, FLOAT, DOUBLE, BOOL, DATE, TIMESTAMP;

    /**
     * Converts this attribute type to a mill-core [DatabaseType].
     * All attribute columns are nullable (extraction may fail).
     */
    fun toDatabaseType(): DatabaseType = when (this) {
        STRING -> DatabaseType.string(true, -1)
        INT -> DatabaseType.i32(true)
        LONG -> DatabaseType.i64(true)
        FLOAT -> DatabaseType.fp32(true, -1, -1)
        DOUBLE -> DatabaseType.fp64(true, -1, -1)
        BOOL -> DatabaseType.bool(true)
        DATE -> DatabaseType.date(true)
        TIMESTAMP -> DatabaseType.string(true, -1) // no dedicated timestamp in DatabaseType yet
    }

    companion object {
        fun fromString(value: String): AttributeType =
            when (value.lowercase()) {
                "string" -> STRING
                "int" -> INT
                "long" -> LONG
                "float" -> FLOAT
                "double" -> DOUBLE
                "bool", "boolean" -> BOOL
                "date" -> DATE
                "timestamp" -> TIMESTAMP
                else -> throw IllegalArgumentException(
                    "Unknown attribute type: '$value'. Expected: string, int, long, float, double, bool, date, timestamp"
                )
            }
    }
}

/**
 * Describes an extra column extracted from the blob path or injected as a constant.
 *
 * YAML examples:
 * ```yaml
 * # Regex extraction
 * - name: year
 *   source: regex
 *   pattern: ".*_(?<year>\\d{4})\\d{4}\\.csv$"
 *   group: year
 *   type: int
 *
 * # Constant injection
 * - name: pipeline
 *   source: constant
 *   value: "raw-ingest"
 *
 * # Date with format
 * - name: file_date
 *   source: regex
 *   pattern: ".*_(?<date>\\d{8})\\.csv$"
 *   group: date
 *   type: date
 *   format: ddMMyyyy
 * ```
 *
 * @property name    column name in the output schema
 * @property source  how the value is obtained (regex or constant)
 * @property type    optional type hint (default: string)
 * @property format  date/timestamp format pattern (required when type is date or timestamp)
 * @property pattern regex pattern (required when source is regex)
 * @property group   named capture group (required when source is regex)
 * @property value   constant value (required when source is constant)
 */
data class TableAttributeDescriptor(
    val name: String,
    val source: AttributeSource,
    val type: AttributeType = AttributeType.STRING,
    val format: String? = null,
    val pattern: String? = null,
    val group: String? = null,
    val value: String? = null
) : Verifiable {

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()
        val ctx = mapOf("attributeName" to name)

        if (name.isBlank()) {
            issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                "Table attribute 'name' must not be blank", ctx)
        }

        when (source) {
            AttributeSource.REGEX -> {
                if (pattern.isNullOrBlank()) {
                    issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                        "Attribute '$name': regex source requires 'pattern'", ctx)
                } else {
                    try {
                        Regex(pattern)
                        val groups = extractNamedGroups(pattern)
                        if (group.isNullOrBlank()) {
                            issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                                "Attribute '$name': regex source requires 'group'", ctx)
                        } else if (group !in groups) {
                            issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                                "Attribute '$name': pattern does not contain named group '$group'. Found: $groups", ctx)
                        }
                    } catch (e: Exception) {
                        issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                            "Attribute '$name': invalid regex pattern: ${e.message}", ctx)
                    }
                }
            }
            AttributeSource.CONSTANT -> {
                if (value == null) {
                    issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                        "Attribute '$name': constant source requires 'value'", ctx)
                }
            }
        }

        if (type == AttributeType.DATE || type == AttributeType.TIMESTAMP) {
            if (format.isNullOrBlank()) {
                issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                    "Attribute '$name': type '${type.name.lowercase()}' requires 'format'", ctx)
            } else {
                try {
                    DateTimeFormatter.ofPattern(format)
                } catch (e: Exception) {
                    issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                        "Attribute '$name': invalid date format '$format': ${e.message}", ctx)
                }
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
