package io.qpointz.mill.metadata.domain.facet

import io.qpointz.mill.UrnSlug
import io.qpointz.mill.metadata.domain.MetadataUrns
import io.qpointz.mill.metadata.domain.facet.exceptions.FacetTypeManifestInvalidException

/**
 * Normalizes a [FacetTypeManifest] to URN form and performs strict structural checks required by WI-094.
 *
 * This is intentionally strict: unknown field-name aliases are not supported.
 */
object FacetTypeManifestNormalizer {
    private val allowedStringFormats = setOf("date", "date-time", "email", "uri")

    /**
     * Normalizes identifiers and validates required titles/descriptions on all schema nodes.
     *
     * @throws FacetTypeManifestInvalidException when the manifest is structurally invalid
     */
    @JvmStatic
    fun normalizeStrict(input: FacetTypeManifest): FacetTypeManifest {
        if (input.typeKey.isBlank()) throw FacetTypeManifestInvalidException("typeKey must not be blank")
        if (input.title.isBlank()) throw FacetTypeManifestInvalidException("title must not be blank")
        if (input.description.isBlank()) throw FacetTypeManifestInvalidException("description must not be blank")
        val normTypeKey = UrnSlug.normalise(input.typeKey, MetadataUrns.FACET_TYPE_PREFIX)
        val normApplicableTo = input.applicableTo
            ?.map { UrnSlug.normalise(it, MetadataUrns.ENTITY_TYPE_PREFIX) }
            ?.distinct()

        validateSchemaNode(path = "\$.payload", node = input.payload)

        return input.copy(
            typeKey = normTypeKey,
            category = input.category.trim().ifBlank { "general" },
            applicableTo = normApplicableTo
        )
    }

    private fun validateSchemaNode(path: String, node: FacetPayloadSchema) {
        if (node.title.isBlank()) throw FacetTypeManifestInvalidException("$path.title must not be blank")
        if (node.description.isBlank()) throw FacetTypeManifestInvalidException("$path.description must not be blank")

        when (node.type) {
            FacetSchemaType.OBJECT -> {
                if (node.format != null) throw FacetTypeManifestInvalidException("$path.format is only allowed for string schema")
                val fields = node.fields ?: throw FacetTypeManifestInvalidException("$path.fields is required for object schema")
                val names = mutableSetOf<String>()
                for ((idx, f) in fields.withIndex()) {
                    val fPath = "$path.fields[$idx]"
                    if (f.name.isBlank()) throw FacetTypeManifestInvalidException("$fPath.name must not be blank")
                    if (!names.add(f.name)) throw FacetTypeManifestInvalidException("$path has duplicate field name '${f.name}'")
                    validateSchemaNode("$fPath.schema", f.schema)
                }
                val required = node.required ?: emptyList()
                for (req in required) {
                    if (req !in names) {
                        throw FacetTypeManifestInvalidException("$path.required contains unknown field '$req'")
                    }
                }
            }
            FacetSchemaType.ARRAY -> {
                if (node.format != null) throw FacetTypeManifestInvalidException("$path.format is only allowed for string schema")
                val items = node.items ?: throw FacetTypeManifestInvalidException("$path.items is required for array schema")
                validateSchemaNode("$path.items", items)
            }
            FacetSchemaType.ENUM -> {
                if (node.format != null) throw FacetTypeManifestInvalidException("$path.format is only allowed for string schema")
                val values = node.values ?: throw FacetTypeManifestInvalidException("$path.values is required for enum schema")
                if (values.isEmpty()) throw FacetTypeManifestInvalidException("$path.values must not be empty")
                val seen = mutableSetOf<String>()
                for ((idx, entry) in values.withIndex()) {
                    val ePath = "$path.values[$idx]"
                    if (entry.value.isBlank()) throw FacetTypeManifestInvalidException("$ePath.value must not be blank")
                    if (entry.description.isBlank()) throw FacetTypeManifestInvalidException("$ePath.description must not be blank")
                    if (!seen.add(entry.value)) {
                        throw FacetTypeManifestInvalidException("$path has duplicate enum value '${entry.value}'")
                    }
                }
            }
            FacetSchemaType.STRING,
            FacetSchemaType.NUMBER,
            FacetSchemaType.BOOLEAN -> {
                if (node.type != FacetSchemaType.STRING && node.format != null) {
                    throw FacetTypeManifestInvalidException("$path.format is only allowed for string schema")
                }
                if (node.type == FacetSchemaType.STRING && node.format != null && node.format !in allowedStringFormats) {
                    throw FacetTypeManifestInvalidException(
                        "$path.format must be one of ${allowedStringFormats.joinToString(", ")}"
                    )
                }
            }
        }
    }
}

