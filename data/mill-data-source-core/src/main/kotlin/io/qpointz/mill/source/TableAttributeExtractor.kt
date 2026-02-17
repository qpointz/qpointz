package io.qpointz.mill.source

import io.qpointz.mill.source.descriptor.AttributeSource
import io.qpointz.mill.source.descriptor.AttributeType
import io.qpointz.mill.source.descriptor.TableAttributeDescriptor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Extracts attribute values from blob paths and produces extra schema fields.
 *
 * For each [TableAttributeDescriptor]:
 * - **REGEX**: applies the regex to the blob URI path and extracts the named group
 * - **CONSTANT**: returns the same value for every blob
 *
 * Values are coerced to the declared [AttributeType]. If coercion fails,
 * the value falls back to `null`.
 *
 * @property attributes the attribute descriptors
 */
class TableAttributeExtractor(
    val attributes: List<TableAttributeDescriptor>
) {

    private data class CompiledRegexAttr(
        val descriptor: TableAttributeDescriptor,
        val regex: Regex
    )

    private val compiledRegex: List<CompiledRegexAttr> by lazy {
        attributes
            .filter { it.source == AttributeSource.REGEX && !it.pattern.isNullOrBlank() }
            .map { CompiledRegexAttr(it, Regex(it.pattern!!)) }
    }

    /**
     * Returns the extra [SchemaField]s for these attributes.
     *
     * @param startIndex the field index offset (appended after format fields)
     */
    fun schemaFields(startIndex: Int): List<SchemaField> =
        attributes.mapIndexed { i, attr ->
            SchemaField(
                name = attr.name,
                index = startIndex + i,
                type = attr.type.toDatabaseType()
            )
        }

    /**
     * Extracts attribute values from the given [blob] path.
     *
     * @return map of attribute name to coerced value (null if extraction/coercion failed)
     */
    fun extract(blob: BlobPath): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val pathStr = blob.uri.path ?: ""

        for (attr in attributes) {
            result[attr.name] = when (attr.source) {
                AttributeSource.REGEX -> extractRegex(attr, pathStr)
                AttributeSource.CONSTANT -> coerce(attr.value, attr.type, attr.format)
            }
        }

        return result
    }

    private fun extractRegex(attr: TableAttributeDescriptor, pathStr: String): Any? {
        val compiled = compiledRegex.firstOrNull { it.descriptor === attr }
            ?: return null
        val match = compiled.regex.find(pathStr) ?: return null
        val rawValue = try {
            match.groups[attr.group!!]?.value
        } catch (_: Exception) {
            null
        } ?: return null
        return coerce(rawValue, attr.type, attr.format)
    }

    companion object {

        /**
         * Coerces a string value to the target [AttributeType].
         * Returns `null` if coercion fails.
         */
        fun coerce(value: String?, type: AttributeType, format: String?): Any? {
            if (value == null) return null
            return try {
                when (type) {
                    AttributeType.STRING -> value
                    AttributeType.INT -> value.toInt()
                    AttributeType.LONG -> value.toLong()
                    AttributeType.FLOAT -> value.toFloat()
                    AttributeType.DOUBLE -> value.toDouble()
                    AttributeType.BOOL -> value.toBooleanStrict()
                    AttributeType.DATE -> {
                        val fmt = DateTimeFormatter.ofPattern(format ?: "yyyy-MM-dd")
                        LocalDate.parse(value, fmt)
                    }
                    AttributeType.TIMESTAMP -> {
                        val fmt = DateTimeFormatter.ofPattern(format ?: "yyyy-MM-dd'T'HH:mm:ss")
                        LocalDateTime.parse(value, fmt)
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
