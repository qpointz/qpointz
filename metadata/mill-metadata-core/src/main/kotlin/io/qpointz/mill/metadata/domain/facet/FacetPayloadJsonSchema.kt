package io.qpointz.mill.metadata.domain.facet

/**
 * Generates a JSON Schema projection for facet payload schemas.
 *
 * Mill's [FacetPayloadSchema] remains the source of truth. The generated schema is intended for
 * external shape validation and model/tool context; Mill-specific policy stays in `x-mill-*`
 * annotations.
 */
object FacetPayloadJsonSchema {

    const val DRAFT_07_SCHEMA: String = "http://json-schema.org/draft-07/schema#"

    @JvmStatic
    fun forManifest(manifest: FacetTypeManifest): Map<String, Any?> {
        val root = forPayload(manifest.payload).toMutableMap()
        root["\$schema"] = DRAFT_07_SCHEMA
        root["\$id"] = "${manifest.typeKey}/schema"
        root["x-mill-facetTypeUrn"] = manifest.typeKey
        root["x-mill-targetCardinality"] = manifest.targetCardinality.name
        manifest.applicableTo?.let { root["x-mill-applicableTo"] = it }
        manifest.category?.let { root["x-mill-category"] = it }
        manifest.schemaVersion?.let { root["x-mill-schemaVersion"] = it }
        return root
    }

    @JvmStatic
    fun forPayload(schema: FacetPayloadSchema): Map<String, Any?> {
        val out = linkedMapOf<String, Any?>(
            "title" to schema.title,
            "description" to schema.description
        )
        schema.default?.let { out["default"] = it }

        when (schema.type) {
            FacetSchemaType.OBJECT -> {
                out["type"] = "object"
                val properties = linkedMapOf<String, Any?>()
                val required = mutableListOf<String>()
                for (field in schema.fields.orEmpty()) {
                    properties[field.name] = forField(field)
                    if (field.required) {
                        required.add(field.name)
                    }
                }
                out["properties"] = properties
                if (required.isNotEmpty()) {
                    out["required"] = required
                }
                out["additionalProperties"] = true
            }
            FacetSchemaType.ARRAY -> {
                out["type"] = "array"
                out["items"] = schema.items?.let { forPayload(it) } ?: emptyMap<String, Any?>()
            }
            FacetSchemaType.STRING -> {
                out["type"] = "string"
                schema.format?.let { out["format"] = it }
            }
            FacetSchemaType.NUMBER -> out["type"] = "number"
            FacetSchemaType.BOOLEAN -> out["type"] = "boolean"
            FacetSchemaType.ENUM -> {
                out["type"] = "string"
                out["enum"] = schema.values.orEmpty().map { it.value }
                if (!schema.values.isNullOrEmpty()) {
                    out["x-mill-enumDescriptions"] = schema.values.associate { it.value to it.description }
                }
            }
        }

        return out
    }

    private fun forField(field: FacetPayloadField): Map<String, Any?> {
        val out = forPayload(field.schema).toMutableMap()
        field.stereotype?.takeIf { it.isNotEmpty() }?.let { out["x-mill-stereotype"] = it }
        return out
    }
}
