package io.qpointz.mill.metadata.domain.facet

import java.io.Serializable

/**
 * Typed, ordered schema tree for facet payload definitions.
 *
 * This is a metadata-specific schema contract (not shared with AI manifests).
 * Object properties are represented as an ordered [fields] list to guarantee UI order.
 *
 * All schema nodes used for UI rendering must provide [title] and [description].
 */
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

/** Ordered object field entry. */
data class FacetPayloadField(
    val name: String,
    val schema: FacetPayloadSchema,
    val required: Boolean = true
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
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

