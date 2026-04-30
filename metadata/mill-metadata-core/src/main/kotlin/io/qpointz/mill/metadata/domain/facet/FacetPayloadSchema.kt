package io.qpointz.mill.metadata.domain.facet

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import java.io.Serializable

/**
 * Typed, ordered schema tree for facet payload definitions.
 *
 * This is a metadata-specific schema contract (not shared with AI manifests).
 * Object properties are represented as an ordered [fields] list to guarantee UI order.
 *
 * All schema nodes used for UI rendering must provide [title] and [description].
 *
 * Null-valued optional properties are omitted from JSON so API payloads match sparse YAML.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FacetPayloadSchema(
    val type: FacetSchemaType,
    val title: String,
    val description: String,
    val fields: List<FacetPayloadField>? = null,
    val items: FacetPayloadSchema? = null,
    val values: List<FacetEnumValue>? = null,
    val format: String? = null,
    val required: List<String>? = null
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

/** Enum option entry with prompt-friendly description. */
data class FacetEnumValue(
    val value: String,
    val description: String
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

/**
 * Ordered object field entry.
 *
 * @param stereotype Optional UI-only presentation hints as ordered tags (e.g. `table`, `hyperlink`).
 *   Serialized as a comma‑separated string when [schema] is not [FacetSchemaType.ARRAY], and as a JSON
 *   string array when the value schema is [FacetSchemaType.ARRAY]. Ignored by validation; clients interpret tags.
 *   Null or empty after normalization means no hint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(using = FacetPayloadFieldSerializer::class)
@JsonDeserialize(using = FacetPayloadFieldDeserializer::class)
data class FacetPayloadField(
    val name: String,
    val schema: FacetPayloadSchema,
    val required: Boolean = true,
    val stereotype: List<String>? = null
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 3L
    }
}

/** Supported V1 schema node types. */
enum class FacetSchemaType {
    OBJECT,
    ARRAY,
    STRING,
    NUMBER,
    BOOLEAN,
    ENUM
}

