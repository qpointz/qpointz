package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ObjectNode
import io.qpointz.mill.source.verify.*

/**
 * Describes a single reader within a source.
 *
 * ```yaml
 * readers:
 *   - type: csv
 *     label: raw
 *     format:
 *       delimiter: ","
 *     table:
 *       mapping:
 *         type: regex
 *         pattern: ".*(?<table>[^/]+)\\.csv$"
 *       attributes:
 *         - name: year
 *           source: regex
 *           pattern: ".*_(?<year>\\d{4})\\d{4}\\.csv$"
 *           group: year
 *           type: int
 * ```
 *
 * @property type   format type identifier (e.g. "csv", "parquet") — required
 * @property label  optional suffix appended to table names
 * @property format format-specific options, deserialized as [FormatDescriptor]
 * @property table  optional table config override (replaces source-level entirely)
 */
@JsonDeserialize(using = ReaderDescriptorDeserializer::class)
data class ReaderDescriptor(
    val type: String,
    val label: String? = null,
    val format: FormatDescriptor,
    val table: TableDescriptor? = null
) : Verifiable {

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        if (type.isBlank()) {
            issues += VerificationIssue(Severity.ERROR, Phase.READER,
                "Reader 'type' must not be blank")
        }

        var report = VerificationReport(issues = issues)

        if (format is Verifiable) {
            report += (format as Verifiable).verify()
        }

        if (table != null) {
            report += table.verify()
        }

        return report
    }
}

/**
 * Custom deserializer for [ReaderDescriptor].
 *
 * Lifts the reader's `type` into the `format` sub-tree so users don't
 * repeat the type discriminator inside the format block.
 */
class ReaderDescriptorDeserializer : JsonDeserializer<ReaderDescriptor>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ReaderDescriptor {
        val codec = p.codec
        val node = codec.readTree<JsonNode>(p) as ObjectNode

        val type = node.get("type")?.asText()
            ?: throw ctxt.weirdStringException("", ReaderDescriptor::class.java, "Reader 'type' is required")

        val label = node.get("label")?.asText()

        // Build format node: take 'format' sub-object or create empty, inject 'type'
        val formatNode = if (node.has("format") && node.get("format").isObject) {
            (node.get("format") as ObjectNode).deepCopy()
        } else {
            codec.createObjectNode() as ObjectNode
        }
        formatNode.put("type", type)

        val formatParser = formatNode.traverse(codec)
        formatParser.nextToken()
        val format = ctxt.readValue(formatParser, FormatDescriptor::class.java)

        val table = if (node.has("table") && node.get("table").isObject) {
            val tableParser = node.get("table").traverse(codec)
            tableParser.nextToken()
            ctxt.readValue(tableParser, TableDescriptor::class.java)
        } else {
            null
        }

        return ReaderDescriptor(
            type = type,
            label = label,
            format = format,
            table = table
        )
    }
}
