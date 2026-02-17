package io.qpointz.mill.source.descriptor

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ObjectNode
import io.qpointz.mill.source.verify.*

/**
 * Declarative, serializable description of a data source.
 *
 * ```yaml
 * name: warehouse
 * storage:
 *   type: local
 *   rootPath: /data/warehouse
 * table:
 *   mapping:
 *     type: directory
 *     depth: 1
 *   attributes:
 *     - name: pipeline
 *       source: constant
 *       value: "raw-ingest"
 * conflicts: reject
 * readers:
 *   - type: csv
 *     format:
 *       delimiter: ","
 *   - type: parquet
 * ```
 *
 * @property name      logical name of the source (becomes the schema name)
 * @property storage   storage backend configuration
 * @property table     shared default table config (mapping + attributes)
 * @property conflicts conflict resolution strategy (default: reject)
 * @property readers   one or more reader configurations
 */
@JsonDeserialize(using = SourceDescriptorDeserializer::class)
data class SourceDescriptor(
    val name: String,
    val storage: StorageDescriptor,
    val table: TableDescriptor? = null,
    val conflicts: ConflictResolution = ConflictResolution.DEFAULT,
    val readers: List<ReaderDescriptor>
) : Verifiable {

    init {
        require(readers.isNotEmpty()) { "At least one reader is required" }
    }

    override fun verify(): VerificationReport {
        val issues = mutableListOf<VerificationIssue>()

        if (name.isBlank()) {
            issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                "Source 'name' must not be blank")
        }

        if (readers.isEmpty()) {
            issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                "Source must have at least one reader")
        }

        // Duplicate labels
        val labels = readers.mapNotNull { it.label }
        val duplicateLabels = labels.groupBy { it }.filter { it.value.size > 1 }.keys
        for (label in duplicateLabels) {
            issues += VerificationIssue(Severity.WARNING, Phase.DESCRIPTOR,
                "Duplicate reader label '$label' — table names may collide",
                mapOf("label" to label))
        }

        // Readers without table mapping when no source-level default
        val defaultMapping = table?.mapping
        for ((index, reader) in readers.withIndex()) {
            val readerMapping = reader.table?.mapping
            if (readerMapping == null && defaultMapping == null) {
                issues += VerificationIssue(Severity.ERROR, Phase.DESCRIPTOR,
                    "Reader[$index] (type='${reader.type}') has no table mapping " +
                        "and no source-level default is defined",
                    mapOf("readerIndex" to index.toString(), "readerType" to reader.type))
            }
        }

        var report = VerificationReport(issues = issues)

        // Storage
        if (storage is Verifiable) {
            report += (storage as Verifiable).verify()
        }

        // Shared table config
        if (table != null) {
            report += table.verify()
        }

        // Each reader
        for (reader in readers) {
            report += reader.verify()
        }

        return report
    }
}

/**
 * Custom deserializer for [SourceDescriptor].
 */
class SourceDescriptorDeserializer : JsonDeserializer<SourceDescriptor>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SourceDescriptor {
        val codec = p.codec
        val node = codec.readTree<JsonNode>(p) as ObjectNode

        val name = node.get("name")?.asText()
            ?: throw ctxt.weirdStringException("", SourceDescriptor::class.java, "'name' is required")

        val storageNode = node.get("storage")
            ?: throw ctxt.weirdStringException("", SourceDescriptor::class.java, "'storage' is required")
        val storage = deserializeChild<StorageDescriptor>(storageNode, codec, ctxt)

        val table = if (node.has("table")) {
            deserializeChild<TableDescriptor>(node.get("table"), codec, ctxt)
        } else {
            null
        }

        val conflicts = if (node.has("conflicts")) {
            deserializeChild<ConflictResolution>(node.get("conflicts"), codec, ctxt)
        } else {
            ConflictResolution.DEFAULT
        }

        val readersNode = node.get("readers")
            ?: throw ctxt.weirdStringException("", SourceDescriptor::class.java, "'readers' is required")
        if (!readersNode.isArray) {
            throw ctxt.weirdStringException("", SourceDescriptor::class.java, "'readers' must be an array")
        }
        val readers = readersNode.map { readerNode ->
            deserializeChild<ReaderDescriptor>(readerNode, codec, ctxt)
        }

        return SourceDescriptor(
            name = name,
            storage = storage,
            table = table,
            conflicts = conflicts,
            readers = readers
        )
    }

    private inline fun <reified T> deserializeChild(
        node: JsonNode,
        codec: com.fasterxml.jackson.core.ObjectCodec,
        ctxt: DeserializationContext
    ): T {
        val childParser = node.traverse(codec)
        childParser.nextToken()
        return ctxt.readValue(childParser, T::class.java)
    }
}
